package hr.ogcs.rextruderservice.controller;

import hr.ogcs.rextruderservice.service.RScriptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@Slf4j
public class RScriptController {

    @Autowired
    private RScriptService rScriptService;

    @PostMapping("/extractors")
    public ResponseEntity<byte[]> uploadAndDownload(@RequestParam("file") MultipartFile file) {
        byte[] wordBytes = rScriptService.uploadAndExecuteRScript(file);

        // Make response as a Word file
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "generatedDocument.docx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(wordBytes);
    }

}
