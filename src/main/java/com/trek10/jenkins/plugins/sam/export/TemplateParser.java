package com.trek10.jenkins.plugins.sam.export;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.io.InputStream;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;

/**
 * @author Trek10, Inc.
 */
public class TemplateParser {
    
    public Map<String, Object> parse(InputStream template) {
        Yaml yaml = new Yaml(new IntrinsicsConstructor());
        return yaml.load(template);
    }
 
    /**
     * Allows snakeyaml to parse YAML templates that contain short forms of
     * CloudFormation intrinsic functions.
     *
     */
    private class IntrinsicsConstructor extends Constructor {
        public IntrinsicsConstructor() {
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
                Object val;
                String key = node.getTag().getValue().substring(1);
                String prefix = attachFnPrefix ? "Fn::" : "";
                Map<String, Object> result = new HashMap<String, Object>();

                if (node instanceof ScalarNode) {
                    val = (String) constructScalar((ScalarNode) node);
                    if (forceSequenceValue) {
                        val = Arrays.asList(((String) val).split("\\."));
                    }
                } else if (node instanceof SequenceNode) {
                    val = constructSequence((SequenceNode) node);
                } else {
                    val = constructMapping((MappingNode) node);
                }

                result.put(prefix + key, val);
                return result;
            }
        }
    }
}
