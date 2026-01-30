FROM gradle:8.6-jdk21 AS builder
FROM eclipse-temurin:21-jre-jammy

LABEL author="Joey Shi (shi.zhong@northeastern.edu)"
LABEL description="Calendar Manager CLI Application"
LABEL JAVA_VERSION="21"

RUN useradd -ms /bin/bash appuser
USER appuser
RUN chown -R appuser:appuser /home/appuser/
WORKDIR /home/appuser/calctl/
COPY app/build/libs/*-all.jar app.jar

WORKDIR /home/appuser/.calctl/
VOLUME ["/home/appuser/.calctl/"]

RUN chmod 755 .



ENTRYPOINT ["java", "-jar", "/home/appuser/calctl/app.jar"]