package com.etshost.msu.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.etshost.msu.configs.DoConfig;

@Service
public class ImageStorageServiceImpl implements ImageStorageService {
	
	@Autowired
	DoConfig s3Config;
	
	@Value("${do.space.endpoint}")
	private String doEndpoint;
	
	@Value("${do.space.bucket}")
	private String doSpaceBucket;

	@Value("${do.space.prefix}")
	String FOLDER = "files/";

	public String saveImageToServer(MultipartFile multipartFile, String key) throws IOException {
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(multipartFile.getInputStream().available());
		if (multipartFile.getContentType() != null && !"".equals(multipartFile.getContentType())) {
			metadata.setContentType(multipartFile.getContentType());
		}
        AmazonS3 s3 = s3Config.getS3();
		s3.putObject(new PutObjectRequest(doSpaceBucket, FOLDER + key, multipartFile.getInputStream(), metadata)
            .withCannedAcl(CannedAccessControlList.PublicRead));
        
        return s3.getUrl(doSpaceBucket, FOLDER + key).toString();
	}

}