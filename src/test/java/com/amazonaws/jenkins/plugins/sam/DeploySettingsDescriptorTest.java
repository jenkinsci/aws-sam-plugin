// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT

package com.amazonaws.jenkins.plugins.sam;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.amazonaws.jenkins.plugins.sam.DeploySettings;

import hudson.util.FormValidation;

/**
 * @author Trek10, Inc.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeploySettingsDescriptorTest {

    private DeploySettings.DescriptorImpl descriptor;

    @Before
    public void setUp() {
        descriptor = new DeploySettings.DescriptorImpl();
    }

    @Test
    public void testDoCheckCredentialsId() throws IOException, ServletException {
        assertEquals(FormValidation.Kind.OK, descriptor.doCheckCredentialsId("someId").kind);
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckCredentialsId("").kind);
    }

    @Test
    public void testDoCheckKmsKeyId() throws IOException, ServletException {
        assertEquals(FormValidation.Kind.OK, descriptor.doCheckKmsKeyId("").kind);
        assertEquals(FormValidation.Kind.OK, descriptor.doCheckKmsKeyId("test-123").kind);
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckKmsKeyId("123-test").kind);
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckKmsKeyId("test_123").kind);
    }

    @Test
    public void testDoCheckRegion() throws IOException, ServletException {
        assertEquals(FormValidation.Kind.OK, descriptor.doCheckRegion("us-east-1").kind);
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckRegion("").kind);
    }

    @Test
    public void testDoCheckRoleArn() throws IOException, ServletException {
        assertEquals(FormValidation.Kind.OK, descriptor.doCheckRoleArn("").kind);
        assertEquals(FormValidation.Kind.OK, descriptor.doCheckRoleArn("abcde-1234-abcde-1234").kind);
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckRoleArn("abcde-1234").kind);
    }

    @Test
    public void testDoCheckS3Bucket() throws IOException, ServletException {
        assertEquals(FormValidation.Kind.OK, descriptor.doCheckS3Bucket("some-bucket").kind);
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckS3Bucket("").kind);
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckS3Bucket("??some-bucket").kind);
    }

    @Test
    public void testDoCheckStackName() throws IOException, ServletException {
        assertEquals(FormValidation.Kind.OK, descriptor.doCheckStackName("some-stack-123").kind);
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckStackName("").kind);
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckStackName("123-some-stack").kind);
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckStackName("some-stack_123").kind);
    }

    @Test
    public void testDoCheckTemplateFile() throws IOException, ServletException {
        assertEquals(FormValidation.Kind.OK, descriptor.doCheckTemplateFile("template.yaml").kind);
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckTemplateFile("").kind);
    }
}
