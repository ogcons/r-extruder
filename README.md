# R-extruder


## Overview

This Spring Boot application allows users to post R scripts, which are then executed on the server. The result of the R script execution is a plot saved in a Word document, which is returned to the user. This project is built using Java 21, Gradle, and R 4.3.2.

![](docs/Diagram.svg)

## Prerequisites

Before running the application, ensure that the following prerequisites are met:

- [Java 21](https://www.oracle.com/java/technologies/javase-downloads.html)
- [Gradle](https://gradle.org/install/)
- [Spring Boot](https://start.spring.io/)
- [R 4.3.2](https://cran.r-project.org/)
- [Docker and Docker-Compose(Docker Desktop)](https://www.docker.com/products/docker-desktop/)

## Configuration
Environment variables values can be found in Confluence dedicated page: [R-Extruder docs](https://confluence-ogcs.atlassian.net/wiki/spaces/BASF/pages/19595335/R-Extruder)

Set Environment Variable R_EXECUTABLE to the binary of the R script.

For Windows Users(Default):
```
R_EXECUTABLE = C:\\Program Files\\R\\R-4.3.2\\bin\\Rscript.exe
```
Set Environment Variable WORKING_DIR to the desired working directory.

For Windows Users(Example):
```
WORKING_DIR = C:\Users\username\Desktop
```

### Build image from Dockerfile

Set environment variable in system variables, but set them for Linux environment
```
docker build -t r-extruder-service . --build-arg R_EXECUTABLE=$env:R_EXECUTABLE --build-arg WORKING_DIR=$env:WORKING_DIR
```


### Start Localstack and AWS CLI
Run this command in the root of your project:
```bash 
docker-compose up
```

### Start backend
- Enter environment variables into your system environment
- Use these command in project directory:

```bash 
./gradlew clean build
cd build/libs
java -jar r-extruder-service-0.0.1-SNAPSHOT.jar
```