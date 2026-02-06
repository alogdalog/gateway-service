# ---------- build stage ----------
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Gradle Wrapper & 설정 먼저 복사 (캐시 최적화)
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew
RUN ./gradlew --no-daemon dependencies

# 소스 복사 후 빌드
COPY src src
RUN ./gradlew --no-daemon clean bootJar

# ---------- run stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# non-root 실행 (권장)
RUN useradd -ms /bin/bash appuser
USER appuser

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
