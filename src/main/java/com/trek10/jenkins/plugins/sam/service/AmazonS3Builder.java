package com.trek10.jenkins.plugins.sam.service;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.cloudbees.jenkins.plugins.awscredentials.AWSCredentialsHelper;
import com.trek10.jenkins.plugins.sam.DeploySettings;

/**
 * @author Trek10, Inc.
 */
public class AmazonS3Builder {
    public static AmazonS3 build(DeploySettings settings) {
        return AmazonS3ClientBuilder.standard()
                .withCredentials(AWSCredentialsHelper.getCredentials(settings.getCredentialsId(), null))
                .withRegion(Regions.fromName(settings.getRegion())).build();
    }
}
