package org.example.pdf_converter.service;


import jakarta.xml.bind.JAXBException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.example.pdf_converter.Logic.DocxTemplateProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

@Service
public class File_Service {


    @Autowired
    private DocxTemplateProcessor docxTemplateProcessor;


    public byte[] convert(MultipartFile file, Map<String, String> data) throws IOException, Docx4JException, JAXBException, InterruptedException {


        File inputFile = File.createTempFile("input", ".docx");
        file.transferTo(inputFile);


        // Step 2: Load DOCX using docx4j
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(inputFile);

        docxTemplateProcessor.process(wordMLPackage, data);


        // Step 5: Save updated DOCX
        File updatedFile = File.createTempFile("updated", ".docx");
        wordMLPackage.save(updatedFile);

        // Step 6: Convert to PDF using LibreOffice
        File outputDir = new File("D:/pdf-output");
        outputDir.mkdirs();


        ProcessBuilder processBuilder = new ProcessBuilder(
                "C:\\Program Files\\LibreOffice\\program\\soffice.exe", // Windows path
                "--headless",
                "--convert-to", "pdf",
                "--outdir", outputDir.getAbsolutePath(),
                updatedFile.getAbsolutePath()
        );

        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("PDF conversion failed");
        }

        // Step 7: Read PDF
        String pdfName = updatedFile.getName().replace(".docx", ".pdf");
        File pdfFile = new File(outputDir, pdfName);

        byte[] pdfBytes = Files.readAllBytes(pdfFile.toPath());


        return pdfBytes;
    }


}

