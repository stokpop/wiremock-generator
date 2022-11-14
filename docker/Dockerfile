FROM bellsoft/liberica-openjdk-alpine-musl:17

LABEL maintainer=peter.paul.bakker@stokpop.nl

COPY app.jar /app.jar

# this is from https://github.com/stokpop/openapi-generator/tree/wiremock (the wiremock branch)
COPY openapi-generator-cli.jar /openapi-generator-cli.jar

CMD ["java", "-jar", "app.jar"]
