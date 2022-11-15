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

Unzip the file and place the generated jsons in the `mappings` directory of Wiremock:

    unzip wiremock-gen.1668434485709.zip

Run wiremock and test the calls. You can run it once so the mappings folder will be created,
then put the mappings in, then restart wiremock to read the mappings files.

    java -jar wiremock-jre8-standalone-2.33.2.jar --port 8484

Try it with a delay:

    time curl -v -X GET localhost:8484/delay

# Use OpenAPI x-performance extensions

Delays can be added in the generated mappings by using OpenAPI extensions.

Use extension name `x-performance` and extension property `response-time-millis` with as value a number
to use for the call to this endpoint in the stub in milliseconds.

Example in Spring config using annotations:

```java
@Operation(summary = "The delay call does a simple java sleep in request thread for 'duration' milliseconds.",
            extensions=@Extension(name = "x-performance",
                    properties = { @ExtensionProperty(name = "response-time-millis", value = "100") })
)
@GetMapping(value = "/delay", produces = "application/json" )
public BurnerMessage delay(@RequestParam(value = "duration", defaultValue = "100") String duration) {
    return sleep(duration);
}
```

In the Swagger generated OpenAPI json this is added to the `responses` of the `/delay` endpoint:

```json
"responses":
{
    "200":
    {
        "description": "OK",
        "content":
        {
            "application/json":
            {
                "schema":
                {
                    "$ref": "#/components/schemas/BurnerMessage"
                }
            }
        }
    }
},
"x-performance":
{
    "response-time-millis": "100"
}
```

and the Wiremock generator adds a `fixedDelayMilliseconds`:

```json
"response" : {
  "status" : 200,
  "fixedDelayMilliseconds": 100,

  "body" : "{  \"durationInMillis\" : 0,  \"name\" : \"name\",  \"message\" : \"message\"}",
  "headers" : {
    "Content-Type" : "application/json"
  }
}
```
Edit the generated files to make it match expected values if needed.

# Next steps

* support for other delays than `fixedDelayMilliseconds`
* use separate body files

# References

* afterburner: https://github.com/perfana/afterburner
* jmeter-generator: https://github.com/stokpop/jmeter-generator
* openapi-generator: https://github.com/stokpop/openapi-generator

