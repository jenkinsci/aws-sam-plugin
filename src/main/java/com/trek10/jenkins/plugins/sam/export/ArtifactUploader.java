package com.trek10.jenkins.plugins.sam.export;

import com.trek10.jenkins.plugins.sam.model.SamPluginException;
import com.trek10.jenkins.plugins.sam.model.UploaderConfig;
import com.trek10.jenkins.plugins.sam.util.ZipHelper;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.SSEAwsKeyManagementParams;

import hudson.FilePath;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Uploads local artifacts to the deployment S3 bucket.
 * 
 * @author Trek10, Inc.
 */
public class ArtifactUploader {

    private final UploaderConfig config;

    private final AmazonS3 s3Client;

    private final PrintStream logger;

    private ArtifactUploader(AmazonS3 s3Client, UploaderConfig config, PrintStream logger) {
        this.s3Client = s3Client;
        this.config = config;
        this.logger = logger;
    }

    public static ArtifactUploader build(AmazonS3 s3Client, UploaderConfig config, PrintStream logger) {
        return new ArtifactUploader(s3Client, config, logger);
    }

    public String upload(FilePath artifactsFilePath) {
        return upload(artifactsFilePath, null);
    }

    public String upload(FilePath artifactsFilePath, String extension) {
        try {
            if (artifactsFilePath.isDirectory()) {
                // If path points to a folder, zip its contents before uploading
                FilePath zipFile = artifactsFilePath.getParent().createTempFile(".sam", null);
                ZipHelper.zipDirectoryContents(artifactsFilePath, zipFile);
                String s3URI = uploadToS3(zipFile, extension);
                zipFile.delete();
                return s3URI;
            }
            return uploadToS3(artifactsFilePath, extension);
        } catch (IOException | InterruptedException e) {
            throw new SamPluginException("Artifact file cannot be uploaded to S3", e);
        }
    }

    public String buildS3PathStyleURI(String artifactsS3Url) {
        AmazonS3URI s3URI = new AmazonS3URI(artifactsS3Url);
        String region = s3Client.getRegionName();
        String versionId = s3URI.getVersionId();
        String base = "https://s3.amazonaws.com";
        if (region != "us-east-1") {
            base = String.format("https://s3-%s.amazonaws.com", region);
        }
        String result = String.format("%s/%s/%s", base, s3URI.getBucket(), s3URI.getKey());
        if (versionId != null) {
            result = String.format("%s?versionId=%s", result, versionId);
        }
        return result;
    }

    private String uploadToS3(FilePath file, String extension) throws IOException, InterruptedException {
        String objectKey = getChecksum(file);
        if (!StringUtils.isEmpty(config.getS3Prefix())) {
            objectKey = String.format("%s/%s", config.getS3Prefix(), objectKey);
        }
        if (extension != null) {
            objectKey = String.format("%s.%s", objectKey, extension);
        }
        if (doesObjectExist(objectKey)) {
            logger.println("Skipping upload for " + objectKey + ". Object already exists.");
            return buildS3URI(objectKey);
        }
        logger.println("Uploading: " + objectKey);
        ObjectMetadata objMetadata = new ObjectMetadata();
        objMetadata.setContentLength(file.length());
        InputStream reader = file.read();
        PutObjectRequest putObjectRequest = new PutObjectRequest(config.getS3Bucket(), objectKey, reader, objMetadata);
        if (StringUtils.isEmpty(config.getKmsKeyId())) {
            objMetadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
        } else {
            putObjectRequest.setSSEAwsKeyManagementParams(new SSEAwsKeyManagementParams(config.getKmsKeyId()));
        }
        s3Client.putObject(putObjectRequest);
        reader.close();
        return buildS3URI(objectKey);
    }

    private String buildS3URI(String key) {
        return String.format("s3://%s/%s", config.getS3Bucket(), key);
    }

    private boolean doesObjectExist(String key) {
        try {
            s3Client.getObjectMetadata(config.getS3Bucket(), key);
            return true;
        } catch (AmazonS3Exception e) {
            return false;
        }
    }

    private String getChecksum(FilePath file) {
        try {
            InputStream reader = file.read();
            String checksum = DigestUtils.md5Hex(reader);
            reader.close();
            return checksum;
        } catch (IOException | InterruptedException e) {
            throw new SamPluginException("Cannot generate a file cheksum", e);
        }
    }

}
