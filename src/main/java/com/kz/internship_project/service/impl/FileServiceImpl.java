package com.kz.internship_project.service.impl;

import com.kz.internship_project.exception.CustomFileNotFoundException;
import com.kz.internship_project.exception.CustomFileStorageException;
import com.kz.internship_project.service.FileService;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @Override
    public String uploadFile(MultipartFile file) {
        String objectKey = UUID.randomUUID() + "_" + file.getOriginalFilename();
        try {
            minioClient.putObject(
                    PutObjectArgs
                            .builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(file.getInputStream(), file.getSize(), -1L)
                            .contentType(file.getContentType())
                            .build()
            );
            return objectKey;
        } catch (Exception exception) {
            throw new CustomFileStorageException("Failed to upload file to MinIO: {}", exception);
        }
    }

    @Override
    public InputStreamResource downloadFile(String objectKey) {
        try {

            InputStream stream = minioClient.getObject(
                    GetObjectArgs
                            .builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
            return new InputStreamResource(stream);
        } catch (Exception exception) {
            throw new CustomFileNotFoundException("Файл не найден: ",exception);
        }
    }

    @Override
    public void deleteFile(String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception exception){
            throw new CustomFileStorageException("Не удалось удалить файл из MinIO: " + objectKey, exception);
        }
    }

    public String generatePresignedUploadUrl(String fileKey) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Http.Method.PUT)
                            .bucket(bucket)
                            .object(fileKey)
                            .expiry(15, TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e){
            throw new CustomFileStorageException("Ошибка при генерации ссылки MinIO: " + fileKey, e);
        }
    }

    @Override
    public boolean exists(String objectKey) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
