package com.equals.desafio.service;

import com.equals.desafio.domain.Sale;
import com.equals.desafio.parser.SaleLayout;
import com.equals.desafio.parser.SaleParser;
import com.equals.desafio.repository.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class SaleImportService {

    private static final int MAX_ERRORS_RETURNED = 10;

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
        int ignored = 0;
        int invalid = 0;

        List<Sale> toSave = new ArrayList<>();
        List<ImportError> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                totalLines++;

                if (line.trim().isEmpty()) {
                    ignored++;
                    continue;
                }

                char recordType = line.charAt(0);
                if (recordType != '1') {
                    ignored++;
                    continue;
                }

                detailLines++;

                try {
                    String normalized = padRight(line, SaleLayout.DETAIL_MIN_LENGTH);

                    Sale sale = saleParser.parse(normalized);
                    validateSale(sale);
                    toSave.add(sale);
                } catch (Exception e) {
                    invalid++;
                    addError(errors, lineNumber, e.getMessage());
                }
            }

            saleRepository.saveAll(toSave);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler/importar o arquivo: " + e.getMessage(), e);
        }

        return new ImportResult(totalLines, detailLines, toSave.size(), ignored, invalid, errors);
    }

    private void addError(List<ImportError> errors, int lineNumber, String reason) {

        if (errors.size() >= MAX_ERRORS_RETURNED) {
            return;
        }

        String safeReason = (reason == null || reason.isBlank())
                ? "Erro desconhecido."
                : reason;

        errors.add(new ImportError(lineNumber, safeReason));
    }

    private void validateSale(Sale sale) {
        if (sale == null) {
            throw new IllegalArgumentException("Venda nula após parse.");
        }

        if (sale.getEstablishmentCode() == null || sale.getEstablishmentCode().trim().isEmpty()) {
            throw new IllegalArgumentException("establishmentCode obrigatório.");
        }

        if (sale.getEventDate() == null) {
            throw new IllegalArgumentException("eventDate obrigatório.");
        }

        if (sale.getTotalAmount() == null) {
            throw new IllegalArgumentException("totalAmount obrigatório.");
        }

        if (sale.getTotalAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("totalAmount não pode ser negativo.");
        }

        if (sale.getTransactionCode() == null || sale.getTransactionCode().trim().isEmpty()) {
            throw new IllegalArgumentException("transactionCode obrigatório.");
        }

        if (sale.getTransactionCode().trim().length() != 32) {
            throw new IllegalArgumentException("transactionCode deve ter 32 caracteres.");
        }

        if (sale.getNetAmount() != null && sale.getNetAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("netAmount não pode ser negativo.");
        }
    }

    private String padRight(String s, int size) {
        if (s == null) {
            return "";
        }
        if (s.length() >= size) {
            return s;
        }
        return String.format("%-" + size + "s", s);
    }

    public record ImportError(int line, String reason) {
    }

    public record ImportResult(
            int totalLines,
            int detailLines,
            int saved,
            int ignored,
            int invalid,
            List<ImportError> errors) {
    }
}