package com.equals.desafio.parser;

import com.equals.desafio.domain.Sale;
import org.springframework.stereotype.Component;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component
public class SaleParser {

    private static final DateTimeFormatter DATE_YYYYMMDD = DateTimeFormatter.BASIC_ISO_DATE; // yyyyMMdd
    private static final DateTimeFormatter TIME_HHMMSS = DateTimeFormatter.ofPattern("HHmmss");

    public Sale parse(String line) {

        // 1) Validar tamanho mínimo (porque vamos recortar posições altas)
        if (line == null || line.length() < 291) {
            throw new IllegalArgumentException("Linha inválida: tamanho menor que o esperado (291). Tamanho atual: " +
                    (line == null ? 0 : line.length()));
        }

        // 2) Validar tipo do registro
        String recordType = cut(line, 1, 1);
        if (!"1".equals(recordType)) {
            throw new IllegalArgumentException("Linha não é detalhe (tipo 1). Tipo encontrado: " + recordType);
        }

        // 3) Recortar campos principais (usando o layout)
        String establishmentCode = cut(line, 2, 10).trim();          // 2..11
        String eventDateStr = cut(line, 20, 8);                      // 20..27 (yyyyMMdd)
        String eventTimeStr = cut(line, 28, 6);                      // 28..33 (HHmmss)
        String transactionCode = cut(line, 46, 32).trim();           // 46..77
        String totalAmountStr = cut(line, 98, 13);                   // 98..110 (centavos)
        String netAmountStr = cut(line, 243, 13);                    // 243..255 (centavos)
        String brand = cut(line, 262, 30).trim();                    // 262..291

        // 4) Converter tipos
        LocalDate eventDate = LocalDate.parse(eventDateStr, DATE_YYYYMMDD);

        // hora pode vir vazia em alguns layouts — por segurança:
        LocalTime eventTime = eventTimeStr.trim().isEmpty()
                ? null
                : LocalTime.parse(eventTimeStr, TIME_HHMMSS);

        BigDecimal totalAmount = parseMoney13(totalAmountStr);
        BigDecimal netAmount = parseMoney13(netAmountStr);

        // 5) Montar objeto Sale
        Sale sale = new Sale();
        sale.setEstablishmentCode(establishmentCode);
        sale.setEventDate(eventDate);
        sale.setEventTime(eventTime);
        sale.setBrand(brand);
        sale.setTotalAmount(totalAmount);

        // Se você ainda não adicionou netAmount na entidade, ignore por enquanto.
        // Quando adicionar, você coloca:
        // sale.setNetAmount(netAmount);

        // transactionCode também não está na sua entidade ainda; recomendo adicionar depois.
        // sale.setTransactionCode(transactionCode);

        return sale;
    }

    // ===== Helpers =====

    // Layout usa início 1-based. Java substring usa 0-based.
    private String cut(String line, int inicio1Based, int tamanho) {
        int start = inicio1Based - 1;
        int end = start + tamanho;
        return line.substring(start, end);
    }

    // Valores com 13 posições normalmente vêm em centavos (inteiro). Divide por 100.
    private BigDecimal parseMoney13(String raw) {
        String digits = raw.trim();
        if (digits.isEmpty()) return BigDecimal.ZERO;
        return new BigDecimal(digits).movePointLeft(2);
    }
}
