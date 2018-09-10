// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT

package com.trek10.jenkins.plugins.sam.export;

/**
 * @author Trek10, Inc.
 */
public enum BucketProperties {
    BUCKET("Bucket", "Key", "Version"),
    S3_BUCKET("S3Bucket", "S3Key", "S3ObjectVersion"),
    S3_BUCKET_NO_VERSION("S3Bucket", "S3Key", null);

    private final String bucketNameProperty;
    private final String objectKeyProperty;
    private final String versionProperty;

    private BucketProperties(String bucketNameProperty, String objectKeyProperty, String versionProperty) {
        this.bucketNameProperty = bucketNameProperty;
        this.objectKeyProperty = objectKeyProperty;
        this.versionProperty = versionProperty;
    }

    public String getBucketNameProperty() {
        return bucketNameProperty;
    }

    public String getObjectKeyProperty() {
        return objectKeyProperty;
    }

    public String getVersionProperty() {
        return versionProperty;
    }
}
