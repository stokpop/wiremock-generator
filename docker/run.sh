#!/bin/bash
docker run --rm -d -p 4757:8080 --name wiremock-gen docker.io/stokpop/wiremock-gen:0.0.1
