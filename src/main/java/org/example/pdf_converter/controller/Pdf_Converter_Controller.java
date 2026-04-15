package org.example.pdf_converter.controller;

import org.example.pdf_converter.service.File_Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/pdf")
public class Pdf_Converter_Controller
{
    @Autowired
    File_Service file_service;

    @PostMapping("/upload")
    public ResponseEntity<byte[]> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam Map<String, String> data) throws Exception {

        byte[] output = file_service.convert(file, data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=output.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(output);
    }
}







