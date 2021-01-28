package com.server.bluedotproject.component;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3Uploader implements Uploader {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    public String bucket;

    @Value("${cloud.aws.s3.outputbucket}")
    public String outputBucket;

    public String upload(MultipartFile multipartFile, String dirName) throws IOException {
        File uploadFile = convert(multipartFile)
                .orElseThrow(() -> new IllegalArgumentException("MultipartFile 변경 실패"));
        return upload(uploadFile, dirName);
    }

    private String upload(File uploadFile, String dirName) {
        String fileName = dirName + "/" + uploadFile.getName();
        String uploadImageUrl = putS3(uploadFile, fileName);
        removeNewFile(uploadFile);
        return uploadImageUrl;
    }

    private String putS3(File uploadFile, String fileName) {
        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, uploadFile).withCannedAcl(CannedAccessControlList.PublicRead));
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

    public synchronized String getThumbnail(String fileName){
        String dirPlusFileName = "thumbnail/00001/videos/" +fileName + ".png";
        return amazonS3Client.getUrl(outputBucket, dirPlusFileName).toString();
    }

    public synchronized List<String> getConvertedVideos(String fileName){

        List<String> valueList = new ArrayList<>();

        String[] splitFileName = fileName.split("\\.");
        String fileName1080p = "videos/" + splitFileName[0]+"-1080p.mp4";
        String fileName720p = "videos/" + splitFileName[0]+"-720p.mp4";

        valueList.add(amazonS3Client.getUrl(outputBucket, fileName1080p).toString());
        valueList.add(amazonS3Client.getUrl(outputBucket, fileName720p).toString());

        return valueList;
    }

    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            log.info(String.format("파일 (%s) 이 삭제되었습니다.", targetFile));
        } else {
            log.info(String.format("파일 (%s) 이 삭제되지 못했습니다.", targetFile));
        }
    }

    private Optional<File> convert(MultipartFile file) throws IOException {
        File convertFile = new File("./src/main/resources/ " + file.getOriginalFilename());
        if (convertFile.createNewFile()) {
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes());
                log.info(String.format("파일 (%s)이 생성 되었습니다.", convertFile));
            }
            return Optional.of(convertFile);
        }
        return Optional.empty();
    }
}