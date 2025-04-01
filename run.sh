#!/bin/bash

export DB_USERNAME=mssadmin
export DB_PASSWORD=Pass@123
export DB_URL=jdbc:sqlserver://192.168.0.117:1433;databaseName=lms;encrypt=false;trustServerCertificate=true;
export LOG_FOLDER=logs
export LOG_MAX_FILE_SIZE=5MB
export LOG_MAX_HISTORY=1
export LOG_TOTAL_SIZE_CAP=1GB
export MINIO_ACCESS_KEY=kU9N9IDRsj2wjm4pxI9u
export MINIO_BUCKET_NAME=lms-bucket
export MINIO_ENDPOINT=http://localhost:9000
export MINIO_ROOT_PASSWORD=4PRiDaXkFJSeDi5t5Da4Etu5q8N6FMqP
export MINIO_ROOT_USERNAME=admin
export MINIO_SECRET_KEY=OlNi9H0nvqkw1XWsFjhW7mC2WCMiU11xqCkHXOVX
export SECURITY_PASSWORD=pass
export SECURITY_USERNAME=mss
export SERVER_PORT=8081
export spring.profiles.active=dev

java "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:7770" -jar target/lms-0.0.1-SNAPSHOT.jar
