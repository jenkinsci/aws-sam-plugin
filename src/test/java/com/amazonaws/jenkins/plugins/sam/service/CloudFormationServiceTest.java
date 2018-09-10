// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT

package com.amazonaws.jenkins.plugins.sam.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.amazonaws.jenkins.plugins.sam.model.ChangeSetNoChangesException;
import com.amazonaws.jenkins.plugins.sam.model.SamPluginException;
import com.amazonaws.jenkins.plugins.sam.service.CloudFormationService;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.AmazonCloudFormationException;
import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.CreateChangeSetRequest;
import com.amazonaws.services.cloudformation.model.CreateChangeSetResult;
import com.amazonaws.services.cloudformation.model.DescribeChangeSetRequest;
import com.amazonaws.services.cloudformation.model.DescribeChangeSetResult;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.ExecuteChangeSetRequest;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackStatus;
import com.amazonaws.services.cloudformation.model.ValidateTemplateRequest;
import com.amazonaws.services.cloudformation.model.ValidateTemplateResult;
import com.amazonaws.services.cloudformation.waiters.AmazonCloudFormationWaiters;

/**
 * @author Trek10, Inc.
 */
@RunWith(MockitoJUnitRunner.class)
public class CloudFormationServiceTest {

    @Mock
    private AmazonCloudFormation cloudFormation;

    @Mock
    private PrintStream logger;

    private CloudFormationService cloudFormationService;

    @Before
    public void setUp() {
        cloudFormationService = CloudFormationService.build(cloudFormation, logger);
        when(cloudFormation.waiters()).thenReturn(new AmazonCloudFormationWaiters(cloudFormation));
    }

    @Test
    public void testValidateTemplate() {
        when(cloudFormation.validateTemplate(any(ValidateTemplateRequest.class)))
                .thenReturn(new ValidateTemplateResult());
        cloudFormationService.validateTemplate("test");
        verify(cloudFormation, only()).validateTemplate(new ValidateTemplateRequest().withTemplateBody("test"));
    }

    @Test
    public void testCreateChangeSetUpdateType() {
        CreateChangeSetResult result = new CreateChangeSetResult().withId("change-set-arn")
                .withStackId("some-stack-arn");
        DescribeChangeSetResult changeSetResult = new DescribeChangeSetResult().withStatus("CREATE_COMPLETE");
        CreateChangeSetRequest request = new CreateChangeSetRequest().withTemplateBody("template")
                .withStackName("some-stack").withChangeSetName("some-changeset")
                .withCapabilities(Capability.CAPABILITY_IAM, Capability.CAPABILITY_NAMED_IAM)
                .withChangeSetType("UPDATE").withRoleARN("some-role");

        when(cloudFormation.createChangeSet(any(CreateChangeSetRequest.class))).thenReturn(result);
        when(cloudFormation.describeChangeSet(any(DescribeChangeSetRequest.class))).thenReturn(changeSetResult);
        assertEquals(result, cloudFormationService.createChangeSet("some-stack", "some-changeset", "template", null, null, "some-role"));
        verify(cloudFormation, times(1)).createChangeSet(request);
    }

    @Test
    public void testCreateChangeSetCreateType() {
        CreateChangeSetResult result = new CreateChangeSetResult().withId("change-set-arn")
                .withStackId("some-stack-arn");
        DescribeChangeSetResult changeSetResult = new DescribeChangeSetResult().withStatus("CREATE_COMPLETE");
        CreateChangeSetRequest request = new CreateChangeSetRequest().withTemplateBody("template")
                .withStackName("some-stack").withChangeSetName("some-changeset")
                .withCapabilities(Capability.CAPABILITY_IAM, Capability.CAPABILITY_NAMED_IAM)
                .withChangeSetType("CREATE");

        when(cloudFormation.describeStacks(new DescribeStacksRequest().withStackName("some-stack")))
                .thenThrow(new AmazonCloudFormationException("error"));
        when(cloudFormation.createChangeSet(any(CreateChangeSetRequest.class))).thenReturn(result);
        when(cloudFormation.describeChangeSet(any(DescribeChangeSetRequest.class))).thenReturn(changeSetResult);
        assertEquals(result, cloudFormationService.createChangeSet("some-stack", "some-changeset", "template", null, null, null));
        verify(cloudFormation, times(1)).createChangeSet(request);
    }

    @Test
    public void testCreateChangeSetExceptions() {
        CreateChangeSetResult result = new CreateChangeSetResult().withId("change-set-arn")
                .withStackId("some-stack-arn");
        DescribeChangeSetResult changeSetResult = new DescribeChangeSetResult().withStatus("FAILED")
                .withStatusReason("The submitted information didn't contain changes.");

        when(cloudFormation.createChangeSet(any(CreateChangeSetRequest.class))).thenReturn(result);
        when(cloudFormation.describeChangeSet(any(DescribeChangeSetRequest.class))).thenReturn(changeSetResult);
        try {
            cloudFormationService.createChangeSet("some-stack", "some-changeset", "template", null, null, null);
        } catch (ChangeSetNoChangesException e) {
        }

        when(cloudFormation.describeChangeSet(any(DescribeChangeSetRequest.class)))
                .thenReturn(changeSetResult.withStatusReason("different reason"));
        try {
            cloudFormationService.createChangeSet("some-stack", "some-changeset", "template", null, null, null);
        } catch (SamPluginException e) {
            assertFalse(e instanceof ChangeSetNoChangesException);
        }
    }

    @Test
    public void testExecuteChangeSetNewStack() {
        ExecuteChangeSetRequest request = new ExecuteChangeSetRequest().withChangeSetName("some-change-set")
                .withStackName("some-stack");

        DescribeStacksResult initialResult = new DescribeStacksResult()
                .withStacks(new Stack().withStackStatus(StackStatus.CREATE_IN_PROGRESS));
        DescribeStacksResult finalResult = new DescribeStacksResult()
                .withStacks(new Stack().withStackStatus(StackStatus.CREATE_COMPLETE));

        when(cloudFormation.describeStacks(any(DescribeStacksRequest.class))).thenReturn(initialResult)
                .thenReturn(finalResult);

        cloudFormationService.executeChangeSet("some-change-set", "some-stack");
        verify(cloudFormation, times(1)).executeChangeSet(request);
    }

    @Test
    public void testExecuteChangeSetCurrentStack() {
        DescribeStacksResult initialResult = new DescribeStacksResult()
                .withStacks(new Stack().withStackStatus(StackStatus.UPDATE_IN_PROGRESS));
        DescribeStacksResult finalResult = new DescribeStacksResult()
                .withStacks(new Stack().withStackStatus(StackStatus.UPDATE_COMPLETE));

        when(cloudFormation.describeStacks(any(DescribeStacksRequest.class))).thenReturn(initialResult)
                .thenReturn(finalResult);

        cloudFormationService.executeChangeSet("some-change-set", "some-stack");
    }

    @Test
    public void testExecuteChangeSetWithoutStackName() {
        DescribeStacksResult initialResult = new DescribeStacksResult()
                .withStacks(new Stack().withStackStatus(StackStatus.UPDATE_IN_PROGRESS));
        DescribeStacksResult finalResult = new DescribeStacksResult()
                .withStacks(new Stack().withStackStatus(StackStatus.UPDATE_COMPLETE));

        when(cloudFormation.describeStacks(new DescribeStacksRequest().withStackName("some-stack"))).thenReturn(initialResult)
                .thenReturn(finalResult);
        when(cloudFormation.describeChangeSet(new DescribeChangeSetRequest().withChangeSetName("some-change-set")))
                .thenReturn(new DescribeChangeSetResult().withStackName("some-stack"));

        cloudFormationService.executeChangeSet("some-change-set", null);
    }
}
