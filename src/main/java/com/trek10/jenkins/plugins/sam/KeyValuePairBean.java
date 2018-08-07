package com.trek10.jenkins.plugins.sam;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.apache.commons.lang.StringUtils;

/**
 * @author Trek10, Inc.
 */
public class KeyValuePairBean extends AbstractDescribableImpl<KeyValuePairBean> {

    private String key;

    private String value;

    @DataBoundConstructor
    public KeyValuePairBean(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
    
    @Extension
    public static class DescriptorImpl extends Descriptor<KeyValuePairBean> {

        public String getDisplayName() {
            return "Key Value pair";
        }

        public FormValidation doCheckKey(@QueryParameter String value) {
            if(StringUtils.isEmpty(value)){
                return FormValidation.error("Please fill in key.");
            }
            if (value.length() > 128) {
                return FormValidation.error("The maximum length is 128 characters.");
            }
            if (!StringUtils.isAlphanumeric(value)) {
                return FormValidation.error("The key can contain only alphanumeric characters.");
            }
            return FormValidation.ok();
        }
        
        public FormValidation doCheckValue(@QueryParameter String value) {
            if(StringUtils.isEmpty(value)){
                return FormValidation.error("Please fill in value.");
            }
            if (value.length() > 256) {
                return FormValidation.error("The maximum length is 256 characters.");
            }
            return FormValidation.ok();
        }
    }

}
