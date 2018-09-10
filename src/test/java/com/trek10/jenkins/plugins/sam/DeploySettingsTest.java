package com.trek10.jenkins.plugins.sam;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Tag;
import com.trek10.jenkins.plugins.sam.model.UploaderConfig;

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
        assertEquals(tags.size(), 2);
        assertEquals(tags.get(0), new Tag().withKey("key1").withValue("value1"));
        assertEquals(tags.get(1), new Tag().withKey("key2").withValue("value2"));
    }

    @Test
    public void testBuildTagsEmpty() {
        List<Tag> tags = settings.buildTags();
        assertEquals(tags.size(), 0);
    }

    @Test
    public void testBuildTemplateParameters() {
        List<KeyValuePairBean> parameterList = new ArrayList<KeyValuePairBean>();
        parameterList.add(new KeyValuePairBean("key1", "value1"));
        parameterList.add(new KeyValuePairBean("key2", "value2"));
        settings.setParameters(parameterList);
        List<Parameter> parameters = settings.buildTemplateParameters();
        assertEquals(parameters.size(), 2);
        assertEquals(parameters.get(0), new Parameter().withParameterKey("key1").withParameterValue("value1"));
        assertEquals(parameters.get(1), new Parameter().withParameterKey("key2").withParameterValue("value2"));
    }

    @Test
    public void testBuildTemplateParametersEmpty() {
        List<Parameter> parameters = settings.buildTemplateParameters();
        assertEquals(parameters.size(), 0);
    }

    @Test
    public void testBuildUploaderConfig() {
        settings.setS3Prefix("some-prefix");
        settings.setKmsKeyId("some-key");
        UploaderConfig config = settings.buildUploaderConfig();
        assertEquals(config.getS3Bucket(), "some-bucket");
        assertEquals(config.getS3Prefix(), "some-prefix");
        assertEquals(config.getKmsKeyId(), "some-key");
    }

}
