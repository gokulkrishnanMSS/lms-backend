package com.inspeedia.toyotsu.lms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspeedia.toyotsu.lms.util.RandomIdGenerator;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class MinioService {
    private final RandomIdGenerator idGenerator;
    private final Logger log = LoggerFactory.getLogger(MinioService.class);
    private final MinioClient minioClient;
    private final String bucketName;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MinioService(
            RandomIdGenerator idGenerator,
            MinioClient minioClient,
            @Value("${minio.bucket-name}") String bucketName) {
        this.idGenerator = idGenerator;
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    public InputStream getFile(String filename) {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (found) {
                return minioClient.getObject(
                        GetObjectArgs.builder().bucket(bucketName)
                                .object(filename)
                                .build()
                );
            }
            log.error("bucket '{}' not found.", bucketName);
            return null;
        } catch (ErrorResponseException resEx) {
            log.error("{} key: {}", resEx.getMessage(), filename);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    public String deleteFile(String filename) {
        try {
            if (minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(filename)
                                .build()
                );
                String successMessage = "File '" + filename + "' deleted successfully.";
                log.info(successMessage);
                return successMessage;
            } else {
                String warningMessage = "Bucket '" + bucketName + "' does not exist. Cannot delete file: " + filename;
                log.warn(warningMessage);
                return warningMessage;
            }
        } catch (Exception ex) {
            String errorMessage = "Error deleting file: '" + filename + "'. Exception: " + ex.getMessage();
            log.error(errorMessage);
            return errorMessage;
        }
    }



    public String saveFile(MultipartFile file) {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
            String fileName = idGenerator.generateSecureRandomString();
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, inputStream.available(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return fileName;
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return "";
    }

    public Object updateFile(String fileName, Object data) {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (found) {
                String jsonString = objectMapper.writeValueAsString(data);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));

                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(fileName)
                                .stream(inputStream, inputStream.available(), -1)
                                .contentType("application/json")
                                .build()
                );
                return data;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public <T> T getObject(String fileName, Class<T> ofClass) {
        try {
            InputStream stream = getFile(fileName);
            if (stream == null)
                return null;

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(stream, ofClass);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            log.error(ex.getMessage());
        }
        return null;
    }
}

