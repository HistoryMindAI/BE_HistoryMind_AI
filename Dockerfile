# ====== STAGE 1: BUILD ======
FROM eclipse-temurin:21-jdk AS build

WORKDIR /build

# copy maven wrapper & config
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# FIX QUYỀN CHO mvnw
RUN chmod +x mvnw

# download deps (cache)
RUN ./mvnw dependency:go-offline

# copy source & build
COPY src ./src
RUN ./mvnw clean package -DskipTests


# ====== STAGE 2: RUN ======
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
