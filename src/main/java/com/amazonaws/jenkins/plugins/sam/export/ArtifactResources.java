// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT

package com.amazonaws.jenkins.plugins.sam.export;

/**
 * Defines CloudFormation resource types that can contain local artifacts.
 * Describes how these types should be updated during artifact export.
 * 
 * @author Trek10, Inc.
 */
public enum ArtifactResources {

    SERVERLESS_FUNCTION("AWS::Serverless::Function", "CodeUri", true, true),
    SERVERLESS_API("AWS::Serverless::Api", "DefinitionUri", false),
    APP_SYNC_GRAPHQL_SCHEMA("AWS::AppSync::GraphQLSchema", "DefinitionS3Location"),
    API_GATEWAY_REST_API("AWS::ApiGateway::RestApi", "BodyS3Location", false, false, BucketProperties.BUCKET),
    LAMBDA_FUNCTION("AWS::Lambda::Function", "Code", true, true, BucketProperties.S3_BUCKET),
    ELASTIC_BEANSTALK_APPLICATION_VERSION(
            "AWS::ElasticBeanstalk::ApplicationVersion",
            "SourceBundle",
            true,
            false,
            BucketProperties.S3_BUCKET_NO_VERSION),
    CLOUD_FORMATION_STACK("AWS::CloudFormation::Stack", "TemplateURL"),
    SERVERLESS_LAYER_VERSION("AWS::Serverless::LayerVersion", "ContentUri", true, true),
    LAMBDA_LAYER_VERSION("AWS::Lambda::LayerVersion", "Content", true, true, BucketProperties.S3_BUCKET);

    private final String type;
    private final String artifactsPathProperty;
    private final boolean workspacePackageAllowed;
    private final boolean forceZip;
    private final BucketProperties bucketProperties;

    private ArtifactResources(String type, String artifactsPathPropery) {
        this(type, artifactsPathPropery, true);
    }

    private ArtifactResources(String type, String artifactsPathProperty, boolean workspacePackageAllowed) {
        this(type, artifactsPathProperty, workspacePackageAllowed, false);
    }

    private ArtifactResources(String type, String artifactsPathProperty, boolean workspacePackageAllowed,
            boolean forceZip) {
        this(type, artifactsPathProperty, workspacePackageAllowed, forceZip, null);
    }

    private ArtifactResources(String type, String artifactsPathProperty, boolean workspacePackageAllowed,
            boolean forceZip, BucketProperties bucketProperties) {
        this.type = type;
        this.artifactsPathProperty = artifactsPathProperty;
        this.workspacePackageAllowed = workspacePackageAllowed;
        this.forceZip = forceZip;
        this.bucketProperties = bucketProperties;
    }

    public String getType() {
        return type;
    }

    public String getArtifactsPathProperty() {
        return artifactsPathProperty;
    }

    public boolean isWorkspacePackageAllowed() {
        return workspacePackageAllowed;
    }

    public boolean isForceZip() {
        return forceZip;
    }

    public BucketProperties getBucketProperties() {
        return bucketProperties;
    }

    public boolean hasBucketProperties() {
        return bucketProperties != null;
    }

    /**
     * Returns a resource enum corresponding to the given resource type.
     *
     * @param resourceType
     *            The type of the resource, ex.: AWS::Serverless::Function.
     * @return Resource enum
     */
    public static ArtifactResources fromType(String resourceType) {
        for (ArtifactResources resource : ArtifactResources.values()) {
            if (resource.getType().equals(resourceType)) {
                return resource;
            }
        }
        throw new IllegalArgumentException("Cannot create enum from " + resourceType + " value!");
    }
}
