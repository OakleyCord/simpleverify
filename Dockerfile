FROM gradle:jdk17-alpine as gradlebuild
COPY . /build
WORKDIR /build
RUN gradle build


FROM openjdk:17-alpine
RUN mkdir /app
# TODO: somehow include a version env variable when building 
COPY --from=gradlebuild /build/build/libs/simpleverify-1.0-SNAPSHOT-all.jar /app
WORKDIR /app
ENTRYPOINT ["java", "-jar", "simpleverify-1.0-SNAPSHOT-all.jar"]
