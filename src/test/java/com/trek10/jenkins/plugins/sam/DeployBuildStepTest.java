package com.trek10.jenkins.plugins.sam;

import hudson.FilePath;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.SingleFileSCM;

import com.trek10.jenkins.plugins.sam.export.ArtifactExporter;
import com.trek10.jenkins.plugins.sam.export.ArtifactUploader;
import com.trek10.jenkins.plugins.sam.model.ChangeSetNoChangesException;
import com.trek10.jenkins.plugins.sam.model.UploaderConfig;
import com.trek10.jenkins.plugins.sam.service.AmazonCloudFormationBuilder;
import com.trek10.jenkins.plugins.sam.service.AmazonS3Builder;
import com.trek10.jenkins.plugins.sam.service.CloudFormationService;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.CreateChangeSetResult;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Tag;
import com.amazonaws.services.s3.AmazonS3;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;
import org.junit.Rule;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

/**
 * @author Trek10, Inc.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ AmazonCloudFormationBuilder.class, AmazonS3Builder.class, ArtifactExporter.class,
        ArtifactUploader.class, CloudFormationService.class })
@PowerMockIgnore({ "javax.crypto.*" })
public class DeployBuildStepTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Mock
    private ArtifactExporter exporter;

    @Mock
    private ArtifactUploader uploader;

    @Mock
    private CloudFormationService cloudFormationService;

    @Mock
    private AmazonS3 s3Client;

    private FreeStyleProject project;

    private DeploySettings settings;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(AmazonCloudFormationBuilder.class);
        PowerMockito.mockStatic(AmazonS3Builder.class);
        PowerMockito.mockStatic(CloudFormationService.class);
        PowerMockito.mockStatic(ArtifactExporter.class);
        PowerMockito.mockStatic(ArtifactUploader.class);

        HashMap<String, Object> fakeOutputTemplate = new HashMap<String, Object>();
        fakeOutputTemplate.put("some", "value");

        when(CloudFormationService.build(any(AmazonCloudFormation.class), any(PrintStream.class)))
                .thenReturn(cloudFormationService);
        when(AmazonS3Builder.build(any(DeploySettings.class))).thenReturn(s3Client);
        when(ArtifactUploader.build(any(AmazonS3.class), any(UploaderConfig.class), any(PrintStream.class)))
                .thenReturn(uploader);
        when(ArtifactExporter.build(any(FilePath.class), any(ArtifactUploader.class))).thenReturn(exporter);
        when(exporter.export()).thenReturn(fakeOutputTemplate);
        when(s3Client.doesBucketExistV2(anyString())).thenReturn(true);
        when(cloudFormationService.createChangeSet(anyString(), anyString(), anyString(), any(Collection.class),
                any(Collection.class), anyString())).thenReturn(new CreateChangeSetResult().withId("some-change-set"));

        project = j.createFreeStyleProject();
        project.setScm(new SingleFileSCM("template.yml", "source_template_content"));
        settings = new DeploySettings("some-creds", "us-west-1", "some-bucket", "some-stack", "template.yml");
    }

    @Test
    public void testPerform() throws IOException, InterruptedException, ExecutionException {
        project.getBuildersList().add(new DeployBuildStep(settings));
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        assertEquals(Result.SUCCESS, build.getResult());
        PowerMockito.verifyStatic();
        AmazonCloudFormationBuilder.build(settings);
        PowerMockito.verifyStatic();
        AmazonS3Builder.build(settings);
        verify(cloudFormationService, times(1)).validateTemplate("source_template_content");
        verify(cloudFormationService, times(1)).createChangeSet("some-stack", "jenkins-build-" + build.getId(),
                "{some: value}\n", new ArrayList<Parameter>(), new ArrayList<Tag>(), null);
        verify(cloudFormationService, times(1)).executeChangeSet("some-change-set");
        verify(s3Client, never()).createBucket(anyString());
    }

    @Test
    public void testPerformCreateBucket() throws IOException, InterruptedException, ExecutionException {
        when(s3Client.doesBucketExistV2(anyString())).thenReturn(false);
        project.getBuildersList().add(new DeployBuildStep(settings));
        project.scheduleBuild2(0).get();

        verify(s3Client, times(1)).createBucket("some-bucket");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPerformNoChangesException() throws IOException, InterruptedException, ExecutionException {
        when(cloudFormationService.createChangeSet(anyString(), anyString(), anyString(), any(Collection.class),
                any(Collection.class), anyString())).thenThrow(new ChangeSetNoChangesException(null));
        project.getBuildersList().add(new DeployBuildStep(settings));
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        assertEquals(Result.SUCCESS, build.getResult());
    }

}
