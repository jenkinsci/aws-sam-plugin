// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT

package com.trek10.jenkins.plugins.sam.export;

import java.util.Map;
import java.io.InputStream;

import org.yaml.snakeyaml.Yaml;

import com.trek10.jenkins.plugins.sam.util.IntrinsicsYamlConstructor;

/**
 * @author Trek10, Inc.
 */
public class TemplateParser {

    public Map<String, Object> parse(InputStream template) {
        Yaml yaml = new Yaml(new IntrinsicsYamlConstructor());
        return yaml.load(template);
    }
}
