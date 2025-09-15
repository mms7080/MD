package com.example.demo.folio.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping; 
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/uploads") 
public class UploadController {

    // POST /api/uploads/images 경로로 매핑됨
    @PostMapping("/images")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String fileId = UUID.randomUUID().toString();
        String fileUrl = "https://picsum.photos/seed/" + fileId + "/200";

        Map<String, String> response = Map.of(
            "fileId", fileId,
            "url", fileUrl
        );

        return ResponseEntity.ok(response);
    }
}