package com.ssafy.nyamnyam.common;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/** 업로드 파일을 프로젝트 ./uploads 에 저장하고 /uploads/{name} URL 반환 */
@Component
public class FileStorage {

    private final Path root = Paths.get("uploads");

    public String save(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            if (!Files.exists(root)) Files.createDirectories(root);
            String original = file.getOriginalFilename() == null ? "img" : file.getOriginalFilename();
            String ext = original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
            String name = UUID.randomUUID().toString().replace("-", "") + ext;
            Files.copy(file.getInputStream(), root.resolve(name));
            return "/uploads/" + name;
        } catch (Exception e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }
}
