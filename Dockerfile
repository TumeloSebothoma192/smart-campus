FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
COPY web ./web
RUN mvn -B clean package

FROM tomcat:10.1-jdk17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/CampusServiceManagementSystemBackend.war /usr/local/tomcat/webapps/CampusServiceManagementSystemBackend.war
COPY docker-entrypoint.sh /usr/local/bin/smart-campus-entrypoint.sh
RUN chmod +x /usr/local/bin/smart-campus-entrypoint.sh

EXPOSE 8080
CMD ["/usr/local/bin/smart-campus-entrypoint.sh"]
