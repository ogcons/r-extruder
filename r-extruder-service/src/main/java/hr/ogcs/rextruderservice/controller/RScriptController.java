package hr.ogcs.rextruderservice.controller;

import hr.ogcs.rextruderservice.service.RScriptService;
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
public class RScriptController {

    @Autowired
    private RScriptService rScriptService;

    @GetMapping("/extractors/{fileName}")
    public ResponseEntity<Resource> getRScript(@PathVariable String fileName) {
        try {
            byte[] fileContent = rScriptService.getRScriptContent(fileName);

            ByteArrayResource resource = new ByteArrayResource(fileContent);

            return ResponseEntity.ok()
                    .contentLength(fileContent.length)
                    .header("Content-Disposition", "attachment; filename=" + fileName)
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/extractors")
    public ResponseEntity<List<String>> getAllRScripts() {
        List<String> scriptNames = rScriptService.getAllRScriptNames();
        return ResponseEntity.ok(scriptNames);
    }

    @PostMapping("/extractors")
    public ResponseEntity<byte[]> uploadAndDownload(@RequestParam("file") MultipartFile file) {
        try {
            byte[] wordBytes = rScriptService.uploadAndExecuteRScript(file);

            // Make response as word file
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "generatedDocument.docx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(wordBytes);

        } catch (IOException | InterruptedException | InvalidFormatException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null); // Internal Server Error
        }
    }
}
