# R-extruder


## Overview

This Spring Boot application allows users to post R scripts, which are then executed on the server. The result of the R script execution is a plot saved in a Word document, which is returned to the user. This project is built using Java 17, Gradle, and R 4.3.2.

![](docs/Diagram.svg)

## Prerequisites

Before running the application, ensure that the following prerequisites are met:

- [Java 17](https://www.oracle.com/java/technologies/javase-downloads.html)
- [Gradle](https://gradle.org/install/)
- [R 4.3.2](https://cran.r-project.org/)
- [Docker and Docker-Compose(Docker Desktop)](https://www.docker.com/products/docker-desktop/)
## For running Localstack and AWS CLI
Run this command in the root of your project:
```bash 
docker-compose up
```
## Configuration
Set Environment Variable R_EXECUTABLE to the binary of the R script.

For Windows Users(Default):
```
R_EXECUTABLE = C:\\Program Files\\R\\R-4.3.2\\bin\\Rscript.exe
```
Set Environment Variable WORKING_DIR to the desired working directory.

For Windows Users(Example):
```
WORKING_DIR = C:\\
```