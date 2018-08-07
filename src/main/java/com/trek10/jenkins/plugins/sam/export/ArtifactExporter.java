package com.trek10.jenkins.plugins.sam.export;

import hudson.FilePath;
import com.amazonaws.services.s3.AmazonS3URI;
import com.trek10.jenkins.plugins.sam.model.SamPluginException;
import com.trek10.jenkins.plugins.sam.util.ZipHelper;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Trek10, Inc.
 */
public class ArtifactExporter {

    private final Map<String, Object> template;

    private final FilePath templateDir;

    private final ArtifactUploader uploader;

    private ArtifactExporter(FilePath templatePath, ArtifactUploader uploader)
            throws IOException, InterruptedException {
        this.template = new TemplateParser().parse(templatePath.read());
        this.templateDir = templatePath.getParent();
        this.uploader = uploader;
    }

    public static ArtifactExporter build(FilePath templatePath, ArtifactUploader uploader)
            throws IOException, InterruptedException {
        return new ArtifactExporter(templatePath, uploader);
    }

    /**
     * Exports the local artifacts referenced by the given template to an S3 bucket.
     * 
     * @return The template with references to artifacts that have been exported to
     *         S3.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> export() {
        Map<String, Object> resources = (Map<String, Object>) template.get("Resources");
        if (resources == null) {
            return template;
        }
        for (Map.Entry<String, Object> entry : resources.entrySet()) {
            Map<String, Object> resource = (Map<String, Object>) entry.getValue();
            String resourceType = (String) resource.get("Type");
            Map<String, Object> resourceProperties = (Map<String, Object>) resource.get("Properties");
            try {
                ArtifactResources resourceDefinition = ArtifactResources.fromType(resourceType);
                buildResourceExporter(resourceDefinition).export(resourceProperties);
            } catch (IllegalArgumentException e) {
            }

        }

        return template;
    }

    private ResourceExporter buildResourceExporter(ArtifactResources resource) {
        if (resource == ArtifactResources.CLOUD_FORMATION_STACK) {
            return new CloudFormationStackResourceExporter(resource);
        }
        if (resource.hasBucketProperties()) {
            return new ResourceWithBucketExporter(resource);
        }
        return new ResourceExporter(resource);
    }

    private boolean isS3URI(String str) {
        try {
            new AmazonS3URI(str);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    private class ResourceExporter {

        protected final ArtifactResources resourceDefinition;

        private ResourceExporter(ArtifactResources resourceDefinition) {
            this.resourceDefinition = resourceDefinition;
        }

        private void export(Map<String, Object> resourceProperties) {
            Object artifactsPath = resourceProperties.get(resourceDefinition.getArtifactsPathProperty());
            if (artifactsPath == null && !resourceDefinition.isWorkspacePackageAllowed()) {
                return;
            }
            if (artifactsPath instanceof Map) { // not an artifacts path
                return;
            }

            String artifactsS3Url = uploadArtifacts((String) artifactsPath);
            updateArtifactsPath(resourceProperties, artifactsS3Url);
        }

        private String uploadArtifacts(String artifactsPath) {
            if (artifactsPath == null) {
                // Upload entire project
                return uploader.upload(templateDir);
            }
            // Do nothing if artifacts path is already an S3 URI
            if (isS3URI(artifactsPath)) {
                return artifactsPath;
            }

            FilePath artifactsFilePath = templateDir.child(artifactsPath);

            // Create temporary dir if artifact file must be zipped
            try {
                if (!artifactsFilePath.isDirectory() && !ZipHelper.isZipStream(artifactsFilePath.read())
                        && resourceDefinition.isForceZip()) {
                    FilePath tmpDir = templateDir.createTempDir(".sam", null);
                    artifactsFilePath.getParent().copyRecursiveTo(artifactsFilePath.getName(), tmpDir);
                    String s3URI = performUpload(tmpDir);
                    tmpDir.deleteRecursive();
                    return s3URI;
                }
            } catch (IOException | InterruptedException e) {
                throw new SamPluginException("Artifact file cannot be uploaded to S3", e);
            }

            return performUpload(artifactsFilePath);
        }

        protected String performUpload(FilePath artifactsFilePath) {
            return uploader.upload(artifactsFilePath);
        }

        /**
         * Sets the artifacts path property to S3 URL of the uploaded object.
         * 
         * @param resourceProperties
         * @param artifactsS3Url
         */
        protected void updateArtifactsPath(Map<String, Object> resourceProperties, String artifactsS3Url) {
            resourceProperties.put(resourceDefinition.getArtifactsPathProperty(), artifactsS3Url);
        }
    }

    private class ResourceWithBucketExporter extends ResourceExporter {
        private ResourceWithBucketExporter(ArtifactResources resource) {
            super(resource);
        }

        /**
         * Sets the artifacts path property to a Map representing the S3 URI of the
         * uploaded object.
         * 
         * @param resourceProperties
         * @param artifactsS3Url
         */
        protected void updateArtifactsPath(Map<String, Object> resourceProperties, String artifactsS3Url) {
            AmazonS3URI s3URI = new AmazonS3URI(artifactsS3Url);
            BucketProperties bucketProperties = resourceDefinition.getBucketProperties();
            String versionProperty = bucketProperties.getVersionProperty();
            String versionId = s3URI.getVersionId();

            Map<String, String> uriMap = new HashMap<String, String>();
            uriMap.put(bucketProperties.getBucketNameProperty(), s3URI.getBucket());
            uriMap.put(bucketProperties.getObjectKeyProperty(), s3URI.getKey());
            if (versionProperty != null && versionId != null) {
                uriMap.put(versionProperty, versionId);
            }

            resourceProperties.put(resourceDefinition.getArtifactsPathProperty(), uriMap);
        }
    }

    private class CloudFormationStackResourceExporter extends ResourceExporter {
        private CloudFormationStackResourceExporter(ArtifactResources resource) {
            super(resource);
        }

        /**
         * Recursively export nested template's resources, upload new template file to
         * S3.
         */
        protected String performUpload(FilePath artifactsFilePath) {
            try {
                ArtifactExporter exporter = new ArtifactExporter(artifactsFilePath, uploader);
                Map<String, Object> nestedTemplate = exporter.export();
                FilePath templateFile = artifactsFilePath.getParent().createTempFile(".sam", null);
                Yaml yaml = new Yaml();
                OutputStreamWriter writer = new OutputStreamWriter(templateFile.write());
                yaml.dump(nestedTemplate, writer);
                writer.close();
                String s3URI = uploader.upload(templateFile, "template");
                templateFile.delete();
                return s3URI;
            } catch (IOException | InterruptedException e) {
                throw new SamPluginException("Nested template cannot be exported", e);
            }
        }

        /**
         * Sets the artifacts path property to S3 URL (path-style format) of the
         * uploaded template.
         * 
         * @param resourceProperties
         * @param artifactsS3Url
         */
        protected void updateArtifactsPath(Map<String, Object> resourceProperties, String artifactsS3Url) {
            resourceProperties.put(resourceDefinition.getArtifactsPathProperty(),
                    uploader.buildS3PathStyleURI(artifactsS3Url));
        }
    }

}
