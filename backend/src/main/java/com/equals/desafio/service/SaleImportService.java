package com.equals.desafio.service;

import com.equals.desafio.domain.Sale;
import com.equals.desafio.parser.SaleParser;
import com.equals.desafio.repository.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
public class SaleImportService {

    private final SaleParser saleParser;
    private final SaleRepository saleRepository;

    public SaleImportService(SaleParser saleParser, SaleRepository saleRepository) {
        this.saleParser = saleParser;
        this.saleRepository = saleRepository;
    }

    public ImportResult importFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo vazio ou não enviado.");
        }

        int totalLines = 0;
        int detailLines = 0;
        int saved = 0;
        int ignored = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                totalLines++;

                // ignora linhas vazias
                if (line.trim().isEmpty()) {
                    ignored++;
                    continue;
                }

                // tipo do registro é o 1º caractere (header=0, detalhe=1, trailer=9)
                char recordType = line.charAt(0);

                if (recordType == '1') {
                    detailLines++;

                    Sale sale = saleParser.parse(line);
                    saleRepository.save(sale);
                    saved++;

                } else {
                    // header (0) e trailer (9) a gente ignora por enquanto
                    ignored++;
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Falha ao ler/importar o arquivo: " + e.getMessage(), e);
        }

        return new ImportResult(totalLines, detailLines, saved, ignored);
    }

    public record ImportResult(int totalLines, int detailLines, int saved, int ignored) {
    }
}