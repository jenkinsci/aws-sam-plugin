package com.trek10.jenkins.plugins.sam.model;

/**
 * @author Trek10, Inc.
 */
public class SamPluginException extends RuntimeException {

    private static final long serialVersionUID = 7007633779950523093L;

    public SamPluginException(String message, Throwable cause) {
        super(message, cause);
    }
}