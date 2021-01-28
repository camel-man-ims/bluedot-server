package com.server.bluedotproject.component;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface Uploader {

    String upload(MultipartFile multipartFile, String dirName) throws IOException;

    String getThumbnail(String fileName);

    List<String> getConvertedVideos(String fileName);

}
