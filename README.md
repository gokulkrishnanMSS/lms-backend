# LMS Backend
API server for the Line Management System. This application is developed using Spring boot.


## Requirements
1. Java >= 17
2. MSSQL Server 2022
3. Minio
4. Intellij IDE

## Create Configuration in Intellij IDE
1. Add new Application configuration.
2. Set Name as 'dev'.
3. Select Java version 17.
4. Select class name `com.inspeedia.toyotsu.lms.LmsApplication`.
5. Paste the below in the Environment variables.
    ```
    DB_USERNAME=<SQL_SERVER_USER>
    DB_PASSWORD=<SQL_SERVER_PASSWORD>
    DB_URL=jdbc:sqlserver://<SQL_SERVER_IP>:1433;databaseName=lms;encrypt=false;trustServerCertificate=true;
    LOG_FOLDER=logs
    LOG_MAX_FILE_SIZE=5MB
    LOG_MAX_HISTORY=1
    LOG_TOTAL_SIZE_CAP=1GB
    MINIO_ACCESS_KEY=<MINIO_ACCESS_KEY>
    MINIO_BUCKET_NAME=<MINIO_BUCKET_NAME>
    MINIO_ENDPOINT=http://localhost:9000
    MINIO_SECRET_KEY=<MINIO_SECRET_KEY>
    SECURITY_PASSWORD=pass
    SECURITY_USERNAME=mss
    SERVER_PORT=8081
    spring.profiles.active=dev
    APP_INI=app.ini
    DEPARTMENT=TLS
    EXPORT_FOLDER=C:\Dev\Mohan\Projects\MSS\Toyutsu\LMS\Test\export
    ```
6. Click Apply.
7. Click OK.
