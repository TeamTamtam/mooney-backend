FROM eclipse-temurin:21-jre-slim

WORKDIR /app

COPY build/libs/*.jar app.jar

ENV TZ=Asia/Seoul

CMD ["java", "-jar", "app.jar"]
