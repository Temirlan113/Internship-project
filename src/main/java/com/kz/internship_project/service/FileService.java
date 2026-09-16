package com.kz.internship_project.service;


import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;


public interface FileService {
    String uploadFile(MultipartFile file);

    InputStreamResource downloadFile(String objectKey);

    void deleteFile(String objectKey);
}
