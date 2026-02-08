# ====== STAGE 1: BUILD ======
FROM eclipse-temurin:21-jdk AS build

WORKDIR /build

# copy maven wrapper & config
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw

# cache dependencies
RUN ./mvnw dependency:go-offline

# copy source & build
COPY src ./src
RUN ./mvnw clean package -DskipTests

# copy final jar (exclude .original)
RUN find target -name "*.jar" ! -name "*.original" -exec cp {} app.jar \;


# ====== STAGE 2: RUN ======
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /build/app.jar app.jar

EXPOSE 8080

CMD ["java","-jar","/app/app.jar"]
