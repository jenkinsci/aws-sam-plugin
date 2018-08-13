package com.trek10.jenkins.plugins.sam;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

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
        assertEquals(descriptor.doCheckCredentialsId("someId").kind, FormValidation.Kind.OK);
        assertEquals(descriptor.doCheckCredentialsId("").kind, FormValidation.Kind.ERROR);
    }

    @Test
    public void testDoCheckKmsKeyId() throws IOException, ServletException {
        assertEquals(descriptor.doCheckKmsKeyId("").kind, FormValidation.Kind.OK);
        assertEquals(descriptor.doCheckKmsKeyId("test-123").kind, FormValidation.Kind.OK);
        assertEquals(descriptor.doCheckKmsKeyId("123-test").kind, FormValidation.Kind.ERROR);
        assertEquals(descriptor.doCheckKmsKeyId("test_123").kind, FormValidation.Kind.ERROR);
    }

    @Test
    public void testDoCheckRegion() throws IOException, ServletException {
        assertEquals(descriptor.doCheckRegion("us-east-1").kind, FormValidation.Kind.OK);
        assertEquals(descriptor.doCheckRegion("").kind, FormValidation.Kind.ERROR);
    }

    @Test
    public void testDoCheckRoleArn() throws IOException, ServletException {
        assertEquals(descriptor.doCheckRoleArn("").kind, FormValidation.Kind.OK);
        assertEquals(descriptor.doCheckRoleArn("abcde-1234-abcde-1234").kind, FormValidation.Kind.OK);
        assertEquals(descriptor.doCheckRoleArn("abcde-1234").kind, FormValidation.Kind.ERROR);
    }

    @Test
    public void testDoCheckS3Bucket() throws IOException, ServletException {
        assertEquals(descriptor.doCheckS3Bucket("some-bucket").kind, FormValidation.Kind.OK);
        assertEquals(descriptor.doCheckS3Bucket("").kind, FormValidation.Kind.ERROR);
        assertEquals(descriptor.doCheckS3Bucket("??some-bucket").kind, FormValidation.Kind.ERROR);
    }

    @Test
    public void testDoCheckStackName() throws IOException, ServletException {
        assertEquals(descriptor.doCheckStackName("some-stack-123").kind, FormValidation.Kind.OK);
        assertEquals(descriptor.doCheckStackName("").kind, FormValidation.Kind.ERROR);
        assertEquals(descriptor.doCheckStackName("123-some-stack").kind, FormValidation.Kind.ERROR);
        assertEquals(descriptor.doCheckStackName("some-stack_123").kind, FormValidation.Kind.ERROR);
    }

    @Test
    public void testDoCheckTemplateFile() throws IOException, ServletException {
        assertEquals(descriptor.doCheckTemplateFile("template.yaml").kind, FormValidation.Kind.OK);
        assertEquals(descriptor.doCheckTemplateFile("").kind, FormValidation.Kind.ERROR);
    }
}
