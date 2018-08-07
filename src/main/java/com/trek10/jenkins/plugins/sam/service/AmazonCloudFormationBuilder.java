package com.trek10.jenkins.plugins.sam.service;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.cloudbees.jenkins.plugins.awscredentials.AWSCredentialsHelper;
import com.trek10.jenkins.plugins.sam.DeploySettings;

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
