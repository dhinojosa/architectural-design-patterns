#!/bin/zsh

docker run --rm -v "$(pwd)":/documents/ asciidoctor/docker-asciidoctor asciidoctor -b html lab_book.adoc

mv lab_book.html index.html
