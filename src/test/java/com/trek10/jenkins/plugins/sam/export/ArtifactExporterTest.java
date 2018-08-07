package com.trek10.jenkins.plugins.sam.export;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.yaml.snakeyaml.Yaml;

import hudson.FilePath;

/**
 * @author Trek10, Inc.
 */
@RunWith(MockitoJUnitRunner.class)
public class ArtifactExporterTest {

    @Mock
    private ArtifactUploader uploader;

    private ArtifactExporter exporter;

    @Before
    public void setUp() throws IOException, InterruptedException {
        FilePath templateFilePath = new FilePath(
                new File(getClass().getClassLoader().getResource("template.yml").getFile()));
        exporter = ArtifactExporter.build(templateFilePath, uploader);
        when(uploader.upload(any(FilePath.class))).thenReturn("s3://some-bucket/abcd");
        when(uploader.buildS3PathStyleURI(anyString())).thenReturn("https://s3.amazonaws.com/abcd");
    }

    @Test
    public void testExport() throws IOException {
        Map<String, Object> result = exporter.export();
        Yaml yaml = new Yaml();
        Map<String, Object> expectedOutput = yaml
                .load(getClass().getClassLoader().getResource("expected_output.yml").openStream());
        assertEquals(result, expectedOutput);
    }

}
