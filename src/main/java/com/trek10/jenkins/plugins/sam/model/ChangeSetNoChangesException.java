// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT

package com.trek10.jenkins.plugins.sam.model;

/**
 * @author Trek10, Inc.
 */
public class ChangeSetNoChangesException extends SamPluginException {

    private static final long serialVersionUID = -152088040334871174L;

    public ChangeSetNoChangesException(Throwable cause) {
        super("The ChangeSet didn't contain changes", cause);
    }
}
