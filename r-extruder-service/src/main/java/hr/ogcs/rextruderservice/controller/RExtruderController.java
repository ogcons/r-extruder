package hr.ogcs.rextruderservice.controller;

import hr.ogcs.rextruderservice.service.RScriptService;
import hr.ogcs.rextruderservice.service.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api")
@Slf4j
public class RExtruderController {
    private final RScriptService rScriptService;
    private final S3Service s3Service;

    public RExtruderController(RScriptService rScriptService, S3Service s3Service) {
        this.rScriptService = rScriptService;
        this.s3Service = s3Service;
    }

    @PostMapping("/extractors")
    public ResponseEntity<String> createAndUpload(@RequestParam("file") MultipartFile file) throws InterruptedException {
        try {
            byte[] wordBytes = rScriptService.createPlotFromRScript(file);

            String s3ObjectKey = s3Service.uploadFileToS3(wordBytes, Objects.requireNonNull(file.getOriginalFilename()));

            return ResponseEntity.ok("Word document uploaded to S3 with key:" + s3ObjectKey);
        } catch (IOException e) {
            log.error("Error during combined operation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to perform the combined operation");
        }
    }

    @GetMapping("/extractors/s3/{fileName}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable String fileName) {
        try {
            byte[] documentBytes = s3Service.downloadFileFromS3(fileName);

            ByteArrayResource resource = new ByteArrayResource(documentBytes);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(documentBytes.length)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/extractors/s3/")
    public ResponseEntity<List<String>> listDocuments() {
        try {
            List<String> documentList = s3Service.listFilesOfBucket();
            return new ResponseEntity<>(documentList, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
