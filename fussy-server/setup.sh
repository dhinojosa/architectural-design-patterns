#!/usr/bin/env sh

javac Main.java

version=1.0.4

docker buildx create --use
docker buildx build --platform linux/amd64,linux/arm64 -t dhinojosa/fussy-server:${version} . --push
docker buildx build --load -t dhinojosa/fussy-server:${version} .
