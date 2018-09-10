// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT

package com.amazonaws.jenkins.plugins.sam.service;

import com.amazonaws.jenkins.plugins.sam.model.ChangeSetNoChangesException;
import com.amazonaws.jenkins.plugins.sam.model.SamPluginException;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.AmazonCloudFormationException;
import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.CreateChangeSetResult;
import com.amazonaws.services.cloudformation.model.CreateChangeSetRequest;
import com.amazonaws.services.cloudformation.model.DescribeChangeSetRequest;
import com.amazonaws.services.cloudformation.model.DescribeChangeSetResult;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.ExecuteChangeSetRequest;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackStatus;
import com.amazonaws.services.cloudformation.model.Tag;
import com.amazonaws.services.cloudformation.model.ValidateTemplateRequest;
import com.amazonaws.waiters.Waiter;
import com.amazonaws.waiters.WaiterParameters;
import com.amazonaws.waiters.WaiterUnrecoverableException;

import java.util.Collection;
import java.io.PrintStream;

/**
 * @author Trek10, Inc.
 */
public class CloudFormationService {

    private AmazonCloudFormation client;

    private PrintStream logger;

    private CloudFormationService(AmazonCloudFormation client, PrintStream logger) {
        this.client = client;
        this.logger = logger;
    }
    
    public static CloudFormationService build(AmazonCloudFormation client, PrintStream logger) {
        return new CloudFormationService(client, logger);
    }

    public void validateTemplate(String template) throws AmazonCloudFormationException {
        logger.println("Validating template...");
        this.client.validateTemplate(new ValidateTemplateRequest().withTemplateBody(template));
    }

    public CreateChangeSetResult createChangeSet(String stackName, String changeSetName, String template, Collection<Parameter> parameters, Collection<Tag> tags, String roleArn) {
        logger.println("Creating ChangeSet...");
        boolean stackExists = stackExists(stackName);
        if (!stackExists) {
            logger.println("Stack [" + stackName + "] does not exist. Creating a new one...");
        }
        CreateChangeSetRequest request = new CreateChangeSetRequest().withTemplateBody(template)
                .withStackName(stackName).withChangeSetName(changeSetName)
                .withCapabilities(Capability.CAPABILITY_IAM, Capability.CAPABILITY_NAMED_IAM)
                .withChangeSetType(stackExists ? "UPDATE" : "CREATE").withParameters(parameters).withTags(tags);
        if (roleArn != null) {
            request.setRoleARN(roleArn);
        }
        CreateChangeSetResult result = this.client.createChangeSet(request);
        waitForChangeSet(result.getId());
        logger.println(String.format("ChangeSet created.%nChangeSet ARN:%n%s%nStack ARN:%n%s%n", result.getId(),
                result.getStackId()));
        return result;
    }
    
    public void executeChangeSet(String changeSetName) {
        executeChangeSet(changeSetName, null);
    }

    public void executeChangeSet(String changeSetName, String stackName) {
        logger.println("Executing ChangeSet...");
        ExecuteChangeSetRequest request = new ExecuteChangeSetRequest().withChangeSetName(changeSetName)
                .withStackName(stackName);
        this.client.executeChangeSet(request);
        if (stackName == null) {
            stackName = getStackNameFromChangeSet(changeSetName);
        }
        this.waitForStackUpdate(stackName);
    }

    private boolean stackExists(String stackName) {
        try {
            this.client.describeStacks(new DescribeStacksRequest().withStackName(stackName));
            return true;
        } catch (AmazonCloudFormationException e) {
            return false;
        }
    }

    private String getStackNameFromChangeSet(String changeSetName) {
        return this.client.describeChangeSet(new DescribeChangeSetRequest().withChangeSetName(changeSetName))
                .getStackName();
    }

    private void waitForChangeSet(String changeSetName) {
        Waiter<DescribeChangeSetRequest> waiter = this.client.waiters().changeSetCreateComplete();
        DescribeChangeSetRequest request = new DescribeChangeSetRequest().withChangeSetName(changeSetName);
        try {
            waiter.run(new WaiterParameters<>(request));
        } catch (WaiterUnrecoverableException e) {
            DescribeChangeSetResult result = this.client.describeChangeSet(request);
            if (result.getStatus().equals("FAILED") && result.getStatusReason().contains("didn't contain changes")) {
                throw new ChangeSetNoChangesException(e);
            }
            throw new SamPluginException("ChangeSet cannot be created", e);
        }
    }

    private void waitForStackUpdate(String stackName) {
        Stack stack = this.client.describeStacks(new DescribeStacksRequest().withStackName(stackName)).getStacks()
                .get(0);
        StackStatus status = StackStatus.fromValue(stack.getStackStatus());
        Waiter<DescribeStacksRequest> waiter;
        if (status == StackStatus.REVIEW_IN_PROGRESS || status == StackStatus.CREATE_IN_PROGRESS) {
            waiter = this.client.waiters().stackCreateComplete();
        } else {
            waiter = this.client.waiters().stackUpdateComplete();
        }
        waiter.run(new WaiterParameters<>(new DescribeStacksRequest().withStackName(stackName)));
    }
}
