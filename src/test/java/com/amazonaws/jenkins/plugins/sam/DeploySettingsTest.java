// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT

package com.amazonaws.jenkins.plugins.sam;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.amazonaws.jenkins.plugins.sam.DeploySettings;
import com.amazonaws.jenkins.plugins.sam.KeyValuePairBean;
import com.amazonaws.jenkins.plugins.sam.model.UploaderConfig;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Tag;

/**
 * @author Trek10, Inc.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeploySettingsTest {

    private DeploySettings settings;

    @Before
    public void setUp() {
        settings = new DeploySettings("some-creds", "us-east-1", "some-bucket", "some-stack", "template.yaml");
    }

    @Test
    public void testBuildTags() {
        List<KeyValuePairBean> tagList = new ArrayList<KeyValuePairBean>();
        tagList.add(new KeyValuePairBean("key1", "value1"));
        tagList.add(new KeyValuePairBean("key2", "value2"));
        settings.setTags(tagList);
        List<Tag> tags = settings.buildTags();
        assertEquals(2, tags.size());
        assertEquals(new Tag().withKey("key1").withValue("value1"), tags.get(0));
        assertEquals(new Tag().withKey("key2").withValue("value2"), tags.get(1));
    }

    @Test
    public void testBuildTagsEmpty() {
        List<Tag> tags = settings.buildTags();
        assertEquals(0, tags.size());
    }

    @Test
    public void testBuildTemplateParameters() {
        List<KeyValuePairBean> parameterList = new ArrayList<KeyValuePairBean>();
        parameterList.add(new KeyValuePairBean("key1", "value1"));
        parameterList.add(new KeyValuePairBean("key2", "value2"));
        settings.setParameters(parameterList);
        List<Parameter> parameters = settings.buildTemplateParameters();
        assertEquals(2, parameters.size());
        assertEquals(new Parameter().withParameterKey("key1").withParameterValue("value1"), parameters.get(0));
        assertEquals(new Parameter().withParameterKey("key2").withParameterValue("value2"), parameters.get(1));
    }

    @Test
    public void testBuildTemplateParametersEmpty() {
        List<Parameter> parameters = settings.buildTemplateParameters();
        assertEquals(0, parameters.size());
    }

    @Test
    public void testBuildUploaderConfig() {
        settings.setS3Prefix("some-prefix");
        settings.setKmsKeyId("some-key");
        UploaderConfig config = settings.buildUploaderConfig();
        assertEquals("some-bucket", config.getS3Bucket());
        assertEquals("some-prefix", config.getS3Prefix());
        assertEquals("some-key", config.getKmsKeyId());
    }

}
