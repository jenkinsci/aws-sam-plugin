# AWS SAM plugin for Jenkins

## Status

[![codecov](https://codecov.io/gh/jenkinsci/aws-sam/branch/master/graph/badge.svg)](https://codecov.io/gh/jenkinsci/aws-sam)
[![License](https://img.shields.io/github/license/jenkinsci/aws-sam.svg)](LICENSE)
[![wiki](https://img.shields.io/badge/AWS%20SAM-WIKI-blue.svg?style=flat)](http://wiki.jenkins-ci.org/display/JENKINS/AWS+SAM)
[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/aws-sam/master)](https://ci.jenkins.io/job/Plugins/job/aws-sam/job/master/)

This plugin packages and deploys both CloudFormation and SAM templates with a security first mindset.

Github link: [https://github.com/trek10inc/jenkins-aws-sam-plugin](https://github.com/trek10inc/jenkins-aws-sam-plugin)  

## Features

This plugins adds Jenkins pipeline steps to interact with the AWS API.

* [samDeploy](#samdeploy)

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

### samDeploy

```
samDeploy([credentialsId: 'jenkins_sandbox', region: 'us-east-1', s3Bucket: 'sam-jenkins-plugin', stackName: 'sam-jenkins-plugin', parameters: [[key: 'Username', value: 'Modest']], templateFile: 'template.yml'])
```
