// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT

package com.trek10.jenkins.plugins.sam;

import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.yaml.snakeyaml.Yaml;

import jenkins.tasks.SimpleBuildStep;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;

import com.trek10.jenkins.plugins.sam.export.ArtifactExporter;
import com.trek10.jenkins.plugins.sam.export.ArtifactUploader;
import com.trek10.jenkins.plugins.sam.model.ChangeSetNoChangesException;
import com.trek10.jenkins.plugins.sam.service.AmazonS3Builder;
import com.trek10.jenkins.plugins.sam.service.AmazonCloudFormationBuilder;
import com.trek10.jenkins.plugins.sam.service.CloudFormationService;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.cloudformation.model.CreateChangeSetResult;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Jenkins build step that handles SAM package process and deploying
 * CloudFormation template.
 * 
 * @author Trek10, Inc.
 */
public class DeployBuildStep extends Builder implements SimpleBuildStep {

    private final DeploySettings settings;

    @DataBoundConstructor
    public DeployBuildStep(DeploySettings settings) {
        this.settings = settings;
    }

    public DeploySettings getSettings() {
        return settings;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException, AmazonServiceException {

        PrintStream logger = listener.getLogger();
        CloudFormationService cloudFormation = CloudFormationService.build(AmazonCloudFormationBuilder.build(settings),
                logger);
        AmazonS3 s3Client = AmazonS3Builder.build(settings);
        String template = workspace.child(settings.getTemplateFile()).readToString();
        String s3Bucket = settings.getS3Bucket();
        ArtifactUploader uploader = ArtifactUploader.build(s3Client, settings.buildUploaderConfig(), logger);
        ArtifactExporter exporter = ArtifactExporter.build(workspace.child(settings.getTemplateFile()), uploader);

        cloudFormation.validateTemplate(template);

        if (!s3Client.doesBucketExistV2(s3Bucket)) {
            logger.println("Bucket [" + s3Bucket + "] does not exist, creating...");
            s3Client.createBucket(s3Bucket);
        }

        Map<String, Object> outputTemplate = exporter.export();
        logger.println("Successfully packaged artifacts.");

        String outputTemplateFilepath = createOutputTemplateFile(outputTemplate, workspace, run.getId());
        logger.println("Output template: " + outputTemplateFilepath);

        try {
            CreateChangeSetResult changeSetResult = createChangeSet(cloudFormation, outputTemplate, run.getId());
            cloudFormation.executeChangeSet(changeSetResult.getId());
            logger.println("Application successfully deployed.");
        } catch (ChangeSetNoChangesException e) {
            logger.println(e.getMessage());
        }
    }

    @Symbol("samDeploy")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public String getDisplayName() {
            return "AWS SAM deploy application";
        }

        @Override
        public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
            return true;
        }

    }

    private CreateChangeSetResult createChangeSet(CloudFormationService cloudFormation,
            Map<String, Object> outputTemplate, String jobId) {
        Yaml yaml = new Yaml();
        String roleArn = settings.getRoleArn();

        return cloudFormation.createChangeSet(settings.getStackName(), "jenkins-build-" + jobId,
                yaml.dump(outputTemplate), settings.buildTemplateParameters(), settings.buildTags(),
                StringUtils.isEmpty(roleArn) ? null : roleArn);
    }

    private String createOutputTemplateFile(Map<String, Object> outputTemplate, FilePath workspace, String jobId)
            throws IOException, InterruptedException {
        Yaml yaml = new Yaml();
        String outputTemplateFile = settings.getOutputTemplateFile();

        if (StringUtils.isEmpty(outputTemplateFile)) {
            outputTemplateFile = String.format("template-%s.yaml", jobId);
        }
        OutputStreamWriter writer = new OutputStreamWriter(workspace.child(outputTemplateFile).write(),
                StandardCharsets.UTF_8);
        yaml.dump(outputTemplate, writer);
        return outputTemplateFile;
    }

}
