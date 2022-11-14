# wiremock-generator
Docker image to easily generate wiremock stubs

# Run

    docker run --rm -d -p 4757:8080 --name wiremock-gen stokpop/wiremock-gen:0.0.1

Run without `-d` to see logging and stop with `ctrl-c`.

# Create mappers

Use curl or postman to create mapper files based on an openapi spec in yaml or json.
Try this example, which works from the project root directory where the `example/` dir is:

    curl -F "file=@example/afterburner-api-docs.json" http://localhost:4757/upload

You get a reply like the following:

    {"projectId":"wiremock-gen.1668434485709"}
    
You can use that `projectId` to download the resulting zip file:

    curl -sS --remote-header-name -O localhost:4757/download/wiremock-gen.1668434485709

Unzip the file and place the json in a `__files` directory of Wiremock:

    unzip wiremock-gen.1668434485709.zip

Run wiremock and test the calls.

    java -jar wiremock-jre8-standalone-2.33.2.jar --port 8484

Try it with a delay:

    time curl -v -X GET localhost:8484/delay

# References

* afterburner: https://github.com/perfana/afterburner
* jmeter-generator: https://github.com/stokpop/jmeter-generator
* openapi-generator: https://github.com/stokpop/openapi-generator

