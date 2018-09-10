// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT

package com.trek10.jenkins.plugins.sam.model;

/**
 * @author Trek10, Inc.
 */
public class UploaderConfig {

    private String kmsKeyId;

    private String s3Bucket;

    private String s3Prefix;

    public String getKmsKeyId() {
        return kmsKeyId;
    }

    public String getS3Prefix() {
        return s3Prefix;
    }

    public String getS3Bucket() {
        return s3Bucket;
    }

    public void setKmsKeyId(String kmsKeyId) {
        this.kmsKeyId = kmsKeyId;
    }

    public void setS3Bucket(String s3Bucket) {
        this.s3Bucket = s3Bucket;
    }

    public void setS3Prefix(String s3Prefix) {
        this.s3Prefix = s3Prefix;
    }

    public UploaderConfig withKmsKeyId(String kmsKeyId) {
        setKmsKeyId(kmsKeyId);
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
