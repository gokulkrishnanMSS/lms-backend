@echo off
set DB_USERNAME=mssadmin
set DB_PASSWORD=Pass@123
set DB_URL=jdbc:sqlserver://192.168.0.117:1433;databaseName=lms;encrypt=false;trustServerCertificate=true;
set LOG_FOLDER=logs
set LOG_MAX_FILE_SIZE=5MB
set LOG_MAX_HISTORY=1
set LOG_TOTAL_SIZE_CAP=1GB
set MINIO_ACCESS_KEY=RvmmieFYU0RwXhgmBCz7
set MINIO_BUCKET_NAME=lms-bucket
set MINIO_ENDPOINT=http://192.168.0.117:9000
set MINIO_SECRET_KEY=Q0dOjFRfeAsxXCHA2J9XiwZHZEQnUHdjeeM17DSc
set SECURITY_PASSWORD=pass
set SECURITY_USERNAME=mss
set SERVER_PORT=8081
set spring.profiles.active=prod
java "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:7770" -jar target/lms-0.0.1-SNAPSHOT.jar
