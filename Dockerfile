FROM eclipse-temurin:17-jre

WORKDIR /app

# jar 복사
COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
