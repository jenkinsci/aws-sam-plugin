package com.trek10.jenkins.plugins.sam.export;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.trek10.jenkins.plugins.sam.model.UploaderConfig;

import hudson.FilePath;

/**
 * @author Trek10, Inc.
 */
@RunWith(MockitoJUnitRunner.class)
public class ArtifactUploaderTest {

    @Mock
    private PrintStream logger;

    @Mock
    private AmazonS3 s3Client;

    private FilePath artifactFilePath;

    private FilePath artifactDirFilePath;

    private UploaderConfig config;

    private ArtifactUploader uploader;

    private final ArgumentCaptor<PutObjectRequest> putObjectRequestCaptor = ArgumentCaptor
            .forClass(PutObjectRequest.class);

    @Before
    public void setUp() {
        when(s3Client.getRegionName()).thenReturn("us-east-2");
        artifactFilePath = new FilePath(
                new File(getClass().getClassLoader().getResource("somefunc/index.js").getFile()));
        artifactDirFilePath = new FilePath(new File(getClass().getClassLoader().getResource("somefunc").getFile()));
        config = new UploaderConfig().withS3Bucket("some-bucket").withS3Prefix("test-prefix");
        uploader = ArtifactUploader.build(s3Client, config, logger);
    }

    @Test
    public void testUploadObjectExists() {
        String result = uploader.upload(artifactFilePath);
        assertEquals(result, "s3://some-bucket/test-prefix/42637f683b13b9beec74eab6d2a442cd");
        verify(s3Client, times(0)).putObject(any(PutObjectRequest.class));
    }

    @Test
    public void testUploadObjectDoesNotExist() {
        when(s3Client.getObjectMetadata("some-bucket", "test-prefix/42637f683b13b9beec74eab6d2a442cd"))
                .thenThrow(new AmazonS3Exception("error"));
        String result = uploader.upload(artifactFilePath);
        assertEquals(result, "s3://some-bucket/test-prefix/42637f683b13b9beec74eab6d2a442cd");
        verify(s3Client, times(1)).putObject(putObjectRequestCaptor.capture());
        PutObjectRequest request = putObjectRequestCaptor.getValue();
        assertEquals(request.getBucketName(), "some-bucket");
        assertEquals(request.getKey(), "test-prefix/42637f683b13b9beec74eab6d2a442cd");
        assertEquals(request.getMetadata().getContentLength(), 89);
        assertEquals(request.getMetadata().getSSEAlgorithm(), "AES256");
    }

    @Test
    public void testUploadObjectWithExtensionAndKms() {
        when(s3Client.getObjectMetadata("some-bucket", "42637f683b13b9beec74eab6d2a442cd.js"))
                .thenThrow(new AmazonS3Exception("error"));
        config.setS3Prefix(null);
        config.setKmsKeyId("some-kms");
        uploader = ArtifactUploader.build(s3Client, config, logger);
        String result = uploader.upload(artifactFilePath, "js");
        assertEquals(result, "s3://some-bucket/42637f683b13b9beec74eab6d2a442cd.js");
        verify(s3Client, times(1)).putObject(putObjectRequestCaptor.capture());
        PutObjectRequest request = putObjectRequestCaptor.getValue();
        assertEquals(request.getSSEAwsKeyManagementParams().getAwsKmsKeyId(), "some-kms");
    }

    @Test
    public void testUploadDirectory() {
        String result = uploader.upload(artifactDirFilePath);
        assertEquals(result.substring(0, 29), "s3://some-bucket/test-prefix/");
    }

    @Test
    public void testBuildS3PathStyleURI() {
        String result = uploader.buildS3PathStyleURI("s3://some-bucket/test-prefix/bb37adc7b7bd21341f12c0eca13e94c9");
        assertEquals(result,
                "https://s3-us-east-2.amazonaws.com/some-bucket/test-prefix/bb37adc7b7bd21341f12c0eca13e94c9");

        when(s3Client.getRegionName()).thenReturn("us-east-1");
        result = uploader.buildS3PathStyleURI("s3://some-bucket/test-prefix/bb37adc7b7bd21341f12c0eca13e94c9");
        assertEquals(result, "https://s3.amazonaws.com/some-bucket/test-prefix/bb37adc7b7bd21341f12c0eca13e94c9");
    }
}
