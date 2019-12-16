// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT

package com.amazonaws.jenkins.plugins.sam.export;

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

import com.amazonaws.jenkins.plugins.sam.export.ArtifactUploader;
import com.amazonaws.jenkins.plugins.sam.model.UploaderConfig;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.PutObjectRequest;

import hudson.FilePath;
import org.apache.commons.codec.digest.DigestUtils;
import java.io.InputStream;
import java.io.IOException;

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

    private String checkSum;

    private long contentLength;

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

        try {
            // Since the file has line endings, we will calculate a different checkSum on different platforms
            InputStream reader = artifactFilePath.read();
            checkSum = DigestUtils.md5Hex(reader);
            reader.close();
            contentLength = artifactFilePath.length();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Could not read checksum of test artifact file", e);
        }
    }

    @Test
    public void testUploadObjectExists() {
        String result = uploader.upload(artifactFilePath);
        assertEquals(result, "s3://some-bucket/test-prefix/" + checkSum);
        verify(s3Client, times(0)).putObject(any(PutObjectRequest.class));
    }

    @Test
    public void testUploadObjectDoesNotExist() {
        when(s3Client.getObjectMetadata("some-bucket", "test-prefix/" + checkSum))
                .thenThrow(new AmazonS3Exception("error"));
        String result = uploader.upload(artifactFilePath);
        assertEquals(result, "s3://some-bucket/test-prefix/" + checkSum);
        verify(s3Client, times(1)).putObject(putObjectRequestCaptor.capture());
        PutObjectRequest request = putObjectRequestCaptor.getValue();
        assertEquals(request.getBucketName(), "some-bucket");
        assertEquals(request.getKey(), "test-prefix/" + checkSum);
        assertEquals(request.getMetadata().getContentLength(), contentLength);
        assertEquals(request.getMetadata().getSSEAlgorithm(), "AES256");
    }

    @Test
    public void testUploadObjectWithExtensionAndKms() {
        when(s3Client.getObjectMetadata("some-bucket", checkSum + ".js"))
                .thenThrow(new AmazonS3Exception("error"));
        config.setS3Prefix(null);
        config.setKmsKeyId("some-kms");
        uploader = ArtifactUploader.build(s3Client, config, logger);
        String result = uploader.upload(artifactFilePath, "js");
        assertEquals(result, "s3://some-bucket/" + checkSum + ".js");
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
