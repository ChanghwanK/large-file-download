package com.example.demolargesizefile.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
public class FileRestController {

    private static final String PATH = "test.zip";
    private static final int CHUNK_SIZE = 1024 * 1024; // 1MB


    @GetMapping(value = "/download")
    public ResponseEntity<StreamingResponseBody> download(@RequestParam(required = false) String fileName)
        throws FileNotFoundException {

        File file = new File(PATH); InputStream inputStream = new FileInputStream(file);

        // 파일 이름 지정 (파일 이름을 파라미터로 받거나, 기본 파일 이름 사용)
        String downloadFileName = Objects.requireNonNullElse(fileName, file.getName());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/octet-stream");
        headers.setContentDispositionFormData("attachment", "test2.zip");

        StreamingResponseBody responseBody = outputStream -> {
            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
        };

        return new ResponseEntity<>(responseBody, headers, HttpStatus.OK);
    }


    @GetMapping(value = "/buffer")
    public ResponseEntity<?> downloadBuffer(@RequestParam(required = false) String fileName) throws IOException {
        File file = new File(PATH);

        if (!file.exists()) {
            return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
        }

        InputStream inputStream = new FileInputStream(file);
        byte[] buffer = new byte[1024 * 1024];
        int bytesRead;

        try {
            inputStream.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error reading file", HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            inputStream.close();
        }

        // 파일 이름 지정 (파라미터로 받거나, 기본 파일 이름 사용)
        String downloadFileName = Objects.requireNonNullElse(fileName, file.getName());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", downloadFileName);

        ByteArrayResource resource = new ByteArrayResource(buffer);

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
}
