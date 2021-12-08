// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT

package com.amazonaws.jenkins.plugins.sam.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;

/**
 * Allows snakeyaml to parse YAML templates that contain short forms of
 * CloudFormation intrinsic functions.
 *
 * @author Trek10, Inc.
 */
public class IntrinsicsYamlConstructor extends SafeConstructor {
    public IntrinsicsYamlConstructor() {
        addIntrinsic("And");
        addIntrinsic("Base64");
        addIntrinsic("Cidr");
        addIntrinsic("Condition", false);
        addIntrinsic("Equals");
        addIntrinsic("FindInMap");
        addIntrinsic("GetAtt", true, true);
        addIntrinsic("GetAZs");
        addIntrinsic("If");
        addIntrinsic("ImportValue");
        addIntrinsic("Join");
        addIntrinsic("Not");
        addIntrinsic("Or");
        addIntrinsic("Ref", false);
        addIntrinsic("Select");
        addIntrinsic("Split");
        addIntrinsic("Sub");
    }

    private void addIntrinsic(String tag) {
        addIntrinsic(tag, true);
    }

    private void addIntrinsic(String tag, boolean attachFnPrefix) {
        addIntrinsic(tag, attachFnPrefix, false);
    }

    private void addIntrinsic(String tag, boolean attachFnPrefix, boolean forceSequenceValue) {
        this.yamlConstructors.put(new Tag("!" + tag), new ConstructFunction(attachFnPrefix, forceSequenceValue));
    }

    private class ConstructFunction extends AbstractConstruct {
        private final boolean attachFnPrefix;
        private final boolean forceSequenceValue;

        public ConstructFunction(boolean attachFnPrefix, boolean forceSequenceValue) {
            this.attachFnPrefix = attachFnPrefix;
            this.forceSequenceValue = forceSequenceValue;
        }

        public Object construct(Node node) {
            String key = node.getTag().getValue().substring(1);
            String prefix = attachFnPrefix ? "Fn::" : "";
            Map<String, Object> result = new HashMap<String, Object>();

            result.put(prefix + key, constructIntrinsicValueObject(node));
            return result;
        }

        protected Object constructIntrinsicValueObject(Node node) {
            if (node instanceof ScalarNode) {
                Object val = constructScalar((ScalarNode) node);
                if (forceSequenceValue) {
                    String strVal = (String) val;
                    int firstDotIndex = strVal.indexOf(".");
                    val = Arrays.asList(strVal.substring(0, firstDotIndex), strVal.substring(firstDotIndex + 1));
                }
                return val;
            } else if (node instanceof SequenceNode) {
                return constructSequence((SequenceNode) node);
            } else if (node instanceof MappingNode) {
                return constructMapping((MappingNode) node);
            }
            throw new YAMLException("Intrisic function arguments cannot be parsed.");
        }
    }
}
