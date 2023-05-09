#!/bin/bash
search_dir=/run/secrets
for entry in "$search_dir"/*
do
    export "$(basename $entry)=$(cat $entry)"
done

/usr/local/tomcat/bin/catalina.sh run