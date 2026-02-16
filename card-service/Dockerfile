# ---- build stage ----
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY /pom.xml .
RUN mvn -B dependency:go-offline

COPY /src ./src
RUN mvn -B -DskipTests package

# ---- runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE ${SERVER_PORT}

ENTRYPOINT ["java","-jar","/app/app.jar"]