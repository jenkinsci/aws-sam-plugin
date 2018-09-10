// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT

package com.trek10.jenkins.plugins.sam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Tag;
import com.amazonaws.services.s3.internal.BucketNameUtils;
import com.cloudbees.jenkins.plugins.awscredentials.AWSCredentialsHelper;
import com.trek10.jenkins.plugins.sam.model.UploaderConfig;
import com.trek10.jenkins.plugins.sam.util.BeanHelper;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

/**
 * Defines SAM package/deploy settings - DeployBuildStep input form.
 * 
 * @author Trek10, Inc.
 */
public class DeploySettings extends AbstractDescribableImpl<DeploySettings> {

    private final String credentialsId;

    private String kmsKeyId;

    @CheckForNull
    private String outputTemplateFile;

    @CheckForNull
    private List<KeyValuePairBean> parameters;

    private final String region;

    private String roleArn;

    private final String s3Bucket;

    private String s3Prefix;

    private final String stackName;

    @CheckForNull
    private List<KeyValuePairBean> tags;

    private final String templateFile;

    @DataBoundConstructor
    public DeploySettings(String credentialsId, String region, String s3Bucket, String stackName, String templateFile) {
        this.credentialsId = credentialsId;
        this.region = region;
        this.s3Bucket = s3Bucket;
        this.stackName = stackName;
        this.templateFile = templateFile;
    }

    public List<Tag> buildTags() {
        List<Tag> list = new ArrayList<Tag>();
        if (tags == null) {
            return list;
        }
        for (KeyValuePairBean tagVars : tags) {
            list.add(new Tag().withKey(tagVars.getKey()).withValue(tagVars.getValue()));
        }
        return list;
    }

    public List<Parameter> buildTemplateParameters() {
        List<Parameter> list = new ArrayList<Parameter>();
        if (parameters == null) {
            return list;
        }
        for (KeyValuePairBean parameterVars : parameters) {
            list.add(new Parameter().withParameterKey(parameterVars.getKey())
                    .withParameterValue(parameterVars.getValue()));
        }
        return list;
    }

    public UploaderConfig buildUploaderConfig() {
        return new UploaderConfig().withKmsKeyId(kmsKeyId).withS3Bucket(s3Bucket).withS3Prefix(s3Prefix);
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public String getKmsKeyId() {
        return kmsKeyId;
    }

    public String getOutputTemplateFile() {
        return outputTemplateFile;
    }

    public List<KeyValuePairBean> getParameters() {
        return parameters;
    }

    public String getRegion() {
        return region;
    }

    public String getS3Bucket() {
        return s3Bucket;
    }

    public String getStackName() {
        return stackName;
    }

    public List<KeyValuePairBean> getTags() {
        return tags;
    }

    public String getTemplateFile() {
        return templateFile;
    }

    public String getRoleArn() {
        return roleArn;
    }

    public String getS3Prefix() {
        return s3Prefix;
    }

    @DataBoundSetter
    public void setKmsKeyId(String kmsKeyId) {
        this.kmsKeyId = kmsKeyId;
    }

    @DataBoundSetter
    public void setOutputTemplateFile(String outputTemplateFile) {
        this.outputTemplateFile = outputTemplateFile;
    }

    @DataBoundSetter
    public void setParameters(List<KeyValuePairBean> parameters) {
        this.parameters = parameters;
    }

    @DataBoundSetter
    public void setTags(List<KeyValuePairBean> tags) {
        this.tags = tags;
    }

    @DataBoundSetter
    public void setRoleArn(String roleArn) {
        this.roleArn = roleArn;
    }

    @DataBoundSetter
    public void setS3Prefix(String s3Prefix) {
        this.s3Prefix = s3Prefix;
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<DeploySettings> {

        @Override
        public String getDisplayName() {
            return "AWS SAM deploy application";
        }

        public ListBoxModel doFillCredentialsIdItems() {
            return AWSCredentialsHelper.doFillCredentialsIdItems(null);
        }

        public ListBoxModel doFillRegionItems() {
            return BeanHelper.doFillRegionItems();
        }

        public FormValidation doCheckCredentialsId(@QueryParameter String value) throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Select AWS credentials.");
            return FormValidation.ok();
        }

        public FormValidation doCheckKmsKeyId(@QueryParameter String value) throws IOException, ServletException {
            if (value.length() > 0) {
                char[] chars = value.toCharArray();
                if (!Character.isLetter(chars[0])) {
                    return FormValidation.error("The key must start with an alphabetic character.");
                }
                for (final Character c : chars) {
                    if (!Character.isLetterOrDigit(c) && c != '-') {
                        return FormValidation.error(
                                "The key can contain only alphanumeric characters (case sensitive) and hyphens.");
                    }
                }
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckRegion(@QueryParameter String value) throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Select AWS region.");
            return FormValidation.ok();
        }

        public FormValidation doCheckRoleArn(@QueryParameter String value) throws IOException, ServletException {
            if (value.length() > 0) {
                if (value.length() < 20) {
                    return FormValidation.error("The minimum length is 20 characters.");
                }
                if (value.length() > 2048) {
                    return FormValidation.error("The maximum length is 2048 characters.");
                }
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckS3Bucket(@QueryParameter String value) throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Fill S3 bucket.");
            try {
                BucketNameUtils.validateBucketName(value);
            } catch (IllegalArgumentException e) {
                return FormValidation.error(e.getMessage());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckStackName(@QueryParameter String value) throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Fill CloudFormation Stack name.");
            if (value.length() > 128) {
                return FormValidation.error("The maximum length is 128 characters.");
            }
            char[] chars = value.toCharArray();
            if (!Character.isLetter(chars[0])) {
                return FormValidation.error("A stack name must start with an alphabetic character.");
            }
            for (final Character c : chars) {
                if (!Character.isLetterOrDigit(c) && c != '-') {
                    return FormValidation.error(
                            "A stack name can contain only alphanumeric characters (case sensitive) and hyphens.");
                }
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckTemplateFile(@QueryParameter String value) throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Fill template file path.");
            return FormValidation.ok();
        }
    }

}
