package hr.ogcs.rextruderservice.controller;

import hr.ogcs.rextruderservice.service.S3Service;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping("/api")
public class S3Controller {

    @Autowired
    private S3Service s3Service;

    @PostMapping("/extractors/s3")
    public ResponseEntity<String> uploadToS3(@RequestPart("file") MultipartFile file) {
        try {
            // Use the instance of S3Service to invoke the non-static method
            String s3ObjectKey = s3Service.uploadWordToS3(file);
            return ResponseEntity.ok("Word document uploaded to S3 with key: " + s3ObjectKey);
        } catch (IOException | InterruptedException | InvalidFormatException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to upload Word document to S3");
        }
    }

    @GetMapping("/extractors/s3/{fileName}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable String fileName) {
        try {
            // Use the S3 service to retrieve the Word document from S3
            byte[] documentBytes = s3Service.downloadWordFromS3(fileName);

            // Create a ByteArrayResource from the document bytes
            ByteArrayResource resource = new ByteArrayResource(documentBytes);

            // Set the response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            // Return the ResponseEntity with the document bytes and headers
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(documentBytes.length)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (IOException e) {
            // Handle exceptions and return an appropriate response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/extractors/s3/")
    public ResponseEntity<List<String>> listDocuments() {
        try {
            List<String> documentList = s3Service.listDocumentsInBucket();
            return new ResponseEntity<>(documentList, HttpStatus.OK);
        } catch (IOException e) {
            // Handle the exception and return an error response
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
