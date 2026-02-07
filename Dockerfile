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

# Find the correct jar (excluding .original) and copy it to a safe location outside target/ to avoid self-reference in find
RUN find target -name "*.jar" ! -name "*.original" -exec cp {} app.jar \;


# ====== STAGE 2: RUN ======
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the jar to target/app.jar to support both default ENTRYPOINT and user's custom start command (java -jar target/*.jar)
# Also create target directory explicitly
RUN mkdir -p target
COPY --from=build /build/app.jar target/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","target/app.jar"]
