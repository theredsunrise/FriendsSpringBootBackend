FROM eclipse-temurin:21-jre-alpine
WORKDIR /application

RUN apk add --no-cache curl
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY user-service-sync/target/*.jar app.jar

ENTRYPOINT ["java", "--enable-preview","-jar", "app.jar"]