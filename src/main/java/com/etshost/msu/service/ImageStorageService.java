package com.etshost.msu.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {

	String saveImageToServer(MultipartFile multipartFile, String key) throws IOException;
}