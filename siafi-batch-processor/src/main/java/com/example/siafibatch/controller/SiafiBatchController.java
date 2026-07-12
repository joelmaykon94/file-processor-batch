package com.example.siafibatch.controller;

import com.example.siafibatch.controller.dto.DhCargaDto;
import com.example.siafibatch.controller.dto.PfCargaDto;
import com.example.siafibatch.controller.dto.SiafiMapper;
import com.example.siafibatch.controller.dto.SiafiNotification;
import com.example.siafibatch.service.FileAutoConsumerService;
import com.example.siafibatch.service.SiafiBatchService;
import com.example.siafibatch.service.SiafiTemplateGenerator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/siafi")
@CrossOrigin(origins = "*")
public class SiafiBatchController {

    private final SiafiBatchService siafiBatchService;
    private final FileAutoConsumerService fileAutoConsumerService;
    private final SiafiTemplateGenerator templateGenerator;

    public SiafiBatchController(SiafiBatchService siafiBatchService,
                                FileAutoConsumerService fileAutoConsumerService,
                                SiafiTemplateGenerator templateGenerator) {
        this.siafiBatchService = siafiBatchService;
        this.fileAutoConsumerService = fileAutoConsumerService;
        this.templateGenerator = templateGenerator;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("active", true);
        status.put("incomingDir", fileAutoConsumerService.getIncomingDirPath());
        status.put("dhCount", siafiBatchService.getDhCount());
        status.put("pfCount", siafiBatchService.getPfCount());
        return ResponseEntity.ok(status);
    }

    @GetMapping("/dh-batches")
    public ResponseEntity<List<DhCargaDto>> getDhBatches() {
        List<DhCargaDto> dtos = siafiBatchService.getDhBatches().stream()
                .map(SiafiMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/pf-batches")
    public ResponseEntity<List<PfCargaDto>> getPfBatches() {
        List<PfCargaDto> dtos = siafiBatchService.getPfBatches().stream()
                .map(SiafiMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile multipartFile) {
        Map<String, String> response = new HashMap<>();
        if (multipartFile.isEmpty()) {
            response.put("error", "Empty file provided.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String originalFilename = multipartFile.getOriginalFilename();
            if (originalFilename == null) originalFilename = "uploaded_file.xml";

            String incomingPath = fileAutoConsumerService.getIncomingDirPath();
            File destFile = new File(incomingPath + File.separator + originalFilename);
            
            try (InputStream is = multipartFile.getInputStream();
                 FileOutputStream fos = new FileOutputStream(destFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            fileAutoConsumerService.processFile(destFile);

            response.put("message", "File uploaded and processed successfully: " + originalFilename);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Error uploading/processing file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, String>> clearData() {
        siafiBatchService.clearData();
        Map<String, String> response = new HashMap<>();
        response.put("message", "All database load history cleared successfully.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<SiafiNotification>> getNotifications() {
        return ResponseEntity.ok(siafiBatchService.getNotifications());
    }

    @PostMapping("/notifications/read")
    public ResponseEntity<Map<String, String>> markNotificationsAsRead() {
        siafiBatchService.markAllAsRead();
        Map<String, String> response = new HashMap<>();
        response.put("message", "All notifications marked as read.");
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // SAMPLES GENERATORS FOR TESTING
    // ==========================================

    @GetMapping("/samples/dh/xml")
    public ResponseEntity<String> getSampleDhXml() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=DH_Sample.xml")
                .contentType(MediaType.APPLICATION_XML)
                .body(templateGenerator.getSampleDhXml());
    }

    @GetMapping("/samples/pf/xml")
    public ResponseEntity<String> getSamplePfXml() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=PF_Sample.xml")
                .contentType(MediaType.APPLICATION_XML)
                .body(templateGenerator.getSamplePfXml());
    }

    @GetMapping("/samples/dh/csv")
    public ResponseEntity<String> getSampleDhCsv() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=DH_Sample.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(templateGenerator.getSampleDhCsv());
    }

    @GetMapping("/samples/pf/csv")
    public ResponseEntity<String> getSamplePfCsv() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=PF_Sample.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(templateGenerator.getSamplePfCsv());
    }
}
