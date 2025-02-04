package com.etshost.msu.bean;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;


/*
*<p>
* Trivial implementation of the {@link MultipartFile} interface to wrap a byte[] decoded
* from a BASE64 encoded String
*</p>
*/
public class BASE64DecodedMultipartFile implements MultipartFile {
    private final byte[] imgContent;
    private final String fileName;

    public BASE64DecodedMultipartFile(byte[] imgContent, String filename) {
        this.imgContent = imgContent;
        this.fileName = filename;
    }

    @Override
    public String getName() { 
        return this.fileName;
    }

    @Override
    public String getOriginalFilename() {
        return this.fileName;
    }

    @Override
    public String getContentType() {
        // TODO - implementation depends on your requirements
        return null;
    }

    @Override
    public boolean isEmpty() {
        return imgContent == null || imgContent.length == 0;
    }

    @Override
    public long getSize() {
        return imgContent.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return imgContent;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(imgContent);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException { 
        new FileOutputStream(dest).write(imgContent);
    }
}