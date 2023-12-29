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
$ docker compose up
```
## Configuration
Set Environment Variable R_EXECUTABLE to the binary of the R script.

For Windows Users:
R_EXECUTABLE = C:\\Program Files\\R\\R-4.3.2\\bin\\Rscript.exe