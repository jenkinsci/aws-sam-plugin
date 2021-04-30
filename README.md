# AWS SAM plugin for Jenkins

## Status

[![License](https://img.shields.io/github/license/jenkinsci/aws-sam-plugin.svg)](LICENSE)
[![Wiki](https://img.shields.io/badge/AWS%20SAM-plugin-blue.svg?style=flat)](https://plugins.jenkins.io/aws-sam/)
[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/aws-sam-plugin/master)](https://ci.jenkins.io/job/Plugins/job/aws-sam-plugin/job/master/)

This plugin packages and deploys both CloudFormation and SAM templates with a security first mindset.

## Features

This plugins adds Jenkins Pipeline steps to interact with the AWS API.

* [`samDeploy`](https://jenkins.io/doc/pipeline/steps/aws-sam/)

If you wish to use the AWS SAM CLI directly, you can use [AWS SAM build images](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-image-repositories.html) with Pipeline's [built-in Docker support](https://www.jenkins.io/doc/book/pipeline/docker/).

## Installation

1. Navigate to your Jenkins server
2. On the left, click `Manage Jenkins`
3. Scroll down to find `Manage Plugins` and click it
4. Look for the `AWS SAM` plugin


## IAM Setup

For deployment you'll need access to an S3 bucket (or permission to create one), 
CloudFormation and ChangeSet IAM lifecycle actions, as well as any IAM permissions
required to create the resources in your SAM (CloudFormation) Template.

### S3 Policy (you may want to limit `Resource` to specific S3 Buckets)

```yaml
---
Version: '2012-10-17'
Statement:
- Sid: SAMS3Actions
  Resource: '*'
  Effect: Allow
  Action:
  - s3:CreateBucket
  - s3:GetBucketLocation
  - s3:ListBucket
  - s3:PutObject
  - s3:PutObjectAcl
  - s3:PutObjectTagging
...
```

### CloudFormation Policy (you may want to limit `Resource` to specific stacks)
```yaml
---
Version: '2012-10-17'
Statement:
- Sid: SAMCloudFormationActions
  Resource: '*'
  Effect: Allow
  Action:
  - cloudformation:ValidateTemplate
  - cloudformation:DescribeStacks
  - cloudformation:CreateChangeSet
  - cloudformation:DescribeChangeSet
  - cloudformation:ExecuteChangeSet
...
```

## Usage / Steps

### Jenkins

[TBD]

### `samDeploy`

```groovy
samDeploy([credentialsId: 'jenkins_sandbox', region: 'us-east-1', s3Bucket: 'sam-jenkins-plugin', stackName: 'sam-jenkins-plugin', parameters: [[key: 'Username', value: 'Modest']], templateFile: 'template.yml'])
```
