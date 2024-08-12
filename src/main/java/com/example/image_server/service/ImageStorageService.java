package com.example.image_server.service;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import net.coobird.thumbnailator.name.Rename;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageStorageService {

    @Value("${images.upload-root}")
    private String imageRoot;


    // 이미지 저장
    public String store(MultipartFile file) throws IOException {
        String imageId = UUID.randomUUID().toString();

        // 코드 이미지로 전환
        BufferedImage original = ImageIO.read(file.getInputStream());

        File imageFile = new File(imageRoot+"/"+imageId+".jpg");

        // 이미지 전환
        // jpg 파일로 전환
        Thumbnails.of(original).scale(1.0d).outputFormat("jpg").toFile(imageFile);
        // 썸네일 파일, 정사각형으로 resize
        Thumbnails.of(imageFile).crop(Positions.CENTER).size(500, 500).outputFormat("jpg")
                .toFiles(Rename.SUFFIX_HYPHEN_THUMBNAIL);

        return imageId;
    }

    // 이미지 조회
    public Resource get(String imageId, Boolean isThumbnail) {
        /*
        이미지 예시
            imageId : abcd-1234
            images/abcd-1234.jpg [원본 파일]
            images/abcd-12340-thumbnail.jpg [리사이징된 썸네일 파일]
         */
        Path file = Paths.get(imageRoot).resolve(imageId + (isThumbnail ? "-thumbnail" : "") + ".jpg");

        try {
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                return null;
            }
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
