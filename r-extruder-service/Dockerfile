FROM ubuntu:24.04

# Install OpenJDK 21 and R on Ubuntu
RUN apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
        openjdk-21-jdk \
        sudo \
        r-base \
        r-base-dev \
        cmake \
        curl \
        tzdata \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* \

# Set the timezone to Europe/Zagreb
ENV TZ=Europe/Zagreb
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /r-extruder-service
COPY build/libs/r-extruder-service-0.0.1-SNAPSHOT.jar .

# Install R packages
RUN R -e "install.packages('mkin', dependencies=TRUE)"
RUN R -e "install.packages('remotes', dependencies=TRUE)"
RUN R -e "remotes::install_github('Miiso9/KineticEval')"

ARG R_EXECUTABLE
ENV R_EXECUTABLE $R_EXECUTABLE
ARG WORKING_DIR
ENV WORKING_DIR $WORKING_DIR

EXPOSE 8080
ENTRYPOINT exec java -jar r-extruder-service-0.0.1-SNAPSHOT.jar