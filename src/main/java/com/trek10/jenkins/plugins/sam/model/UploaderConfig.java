package com.trek10.jenkins.plugins.sam.model;

import java.util.Map;

import javax.annotation.Nonnull;

/**
 * @author Trek10, Inc.
 */
public class UploaderConfig {

    @Nonnull
    private boolean forceUpload = false;

    private String kmsKeyId;

    private Map<String, String> metadata;

    private String s3Bucket;

    private String s3Prefix;

    public String getKmsKeyId() {
        return kmsKeyId;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public String getS3Prefix() {
        return s3Prefix;
    }

    public String getS3Bucket() {
        return s3Bucket;
    }

    public boolean isForceUpload() {
        return forceUpload;
    }

    public void setForceUpload(boolean forceUpload) {
        this.forceUpload = forceUpload;
    }

    public void setKmsKeyId(String kmsKeyId) {
        this.kmsKeyId = kmsKeyId;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public void setS3Bucket(String s3Bucket) {
        this.s3Bucket = s3Bucket;
    }

    public void setS3Prefix(String s3Prefix) {
        this.s3Prefix = s3Prefix;
    }

    public UploaderConfig withForceUpload(boolean forceUpload) {
        setForceUpload(forceUpload);
        return this;
    }

    public UploaderConfig withKmsKeyId(String kmsKeyId) {
        setKmsKeyId(kmsKeyId);
        return this;
    }

    public UploaderConfig withMetadata(Map<String, String> metadata) {
        setMetadata(metadata);
        return this;
    }

    public UploaderConfig withS3Bucket(String s3Bucket) {
        setS3Bucket(s3Bucket);
        return this;
    }

    public UploaderConfig withS3Prefix(String s3Prefix) {
        setS3Prefix(s3Prefix);
        return this;
    }

}
