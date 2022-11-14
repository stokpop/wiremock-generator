#!/bin/bash
docker run --rm -d -p 4756:8080 --name wiremock-gen docker.io/stokpop/wiremock-gen:0.0.1
