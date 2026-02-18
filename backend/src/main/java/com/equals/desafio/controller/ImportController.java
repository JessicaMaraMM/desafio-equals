package com.equals.desafio.controller;

import com.equals.desafio.service.SaleImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/imports")
public class ImportController {

    private final SaleImportService importService;

    public ImportController(SaleImportService importService) {
        this.importService = importService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> importFile(@RequestParam("file") MultipartFile file) {
        var result = importService.importFile(file);
        return ResponseEntity.ok(result);
    }
}
