# R-extruder


## Overview

This Spring Boot application allows users to post R scripts, which are then executed on the server. The result of the R script execution is a plot saved in a Word document, which is returned to the user. This project is built using Java 17, Gradle, and R 4.3.2.

![](docs/Diagram.svg)

## Prerequisites

Before running the application, ensure that the following prerequisites are met:

- [Java 17](https://www.oracle.com/java/technologies/javase-downloads.html)
- [Gradle](https://gradle.org/install/)
- [R 4.3.2](https://cran.r-project.org/)
- [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html)

## For running Localstack and AWS CLI

Starting Localstack:

```bash
$ docker pull localstack/localstack
$ docker run -d --name localstack -p 4566:4566 -e SERVICES=s3 localstack/localstack
```
Open Command Prompt and configure AWS:
```bash
$ aws configure

AWS Access Key ID: "your-access-key"
AWS Secret Access Key: "your-secret-key"
Default region name: "your-region (recommended: eu-central-1)" 
Default output format: json
```
Create a bucket:
```bash
$ aws --endpoint-url=http://localhost:4566 s3 mb s3://word-bucket
```
## Set environment variables:
Add the following Environment variables for S3:
```bash
ACCESS_KEY="your-access-key"
SECRET_KEY="your-secret-key"
REGION="your-region"
HOST=localstack
ENDPOINT=http://localhost:4566
BUCKET=word-bucket
```









