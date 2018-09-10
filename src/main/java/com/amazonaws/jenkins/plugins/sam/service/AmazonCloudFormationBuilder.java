// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT

package com.amazonaws.jenkins.plugins.sam.service;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.jenkins.plugins.sam.DeploySettings;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.cloudbees.jenkins.plugins.awscredentials.AWSCredentialsHelper;

/**
 * @author Trek10, Inc.
 */
public class AmazonCloudFormationBuilder {
    public static AmazonCloudFormation build(DeploySettings settings) {
        AWSCredentialsProvider credentials = AWSCredentialsHelper.getCredentials(settings.getCredentialsId(), null);
        return AmazonCloudFormationClientBuilder.standard().withRegion(Regions.fromName(settings.getRegion()))
                .withCredentials(credentials).build();
    }
}
