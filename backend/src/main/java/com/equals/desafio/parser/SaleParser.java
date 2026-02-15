package com.equals.desafio.parser;

import com.equals.desafio.domain.Sale;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component
public class SaleParser {

    private static final DateTimeFormatter DATE_YYYYMMDD = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter TIME_HHMMSS = DateTimeFormatter.ofPattern("HHmmss");

    private static final int DETAIL_MIN_LENGTH = 530;

    public Sale parse(String line) {

        if (line == null || line.length() < DETAIL_MIN_LENGTH) {
            throw new IllegalArgumentException(
                    "Linha inválida. Tamanho esperado mínimo: " + DETAIL_MIN_LENGTH +
                            ". Tamanho recebido: " + (line == null ? 0 : line.length())
            );
        }

        String recordType = cut(line, 1, 1);
        if (!"1".equals(recordType)) {
            throw new IllegalArgumentException("Registro não é detalhe (tipo 1). Encontrado: " + recordType);
        }

        String establishmentCode = cut(line, 2, 10).trim();
        String eventDateStr = cut(line, 20, 8);
        String eventTimeStr = cut(line, 28, 6);
        String transactionCode = cut(line, 46, 32).trim();
        String totalAmountStr = cut(line, 98, 13);
        String netAmountStr = cut(line, 243, 13);
        String brand = cut(line, 262, 30).trim();

        LocalDate eventDate = LocalDate.parse(eventDateStr, DATE_YYYYMMDD);

        LocalTime eventTime = eventTimeStr.trim().isEmpty()
                ? null
                : LocalTime.parse(eventTimeStr, TIME_HHMMSS);

        BigDecimal totalAmount = parseMoney13(totalAmountStr);
        BigDecimal netAmount = parseMoney13(netAmountStr);

        Sale sale = new Sale();
        sale.setEstablishmentCode(establishmentCode);
        sale.setEventDate(eventDate);
        sale.setEventTime(eventTime);
        sale.setBrand(brand);
        sale.setTotalAmount(totalAmount);
        sale.setTransactionCode(transactionCode);
        sale.setNetAmount(netAmount);

        return sale;
    }

    private String cut(String line, int start1Based, int length) {
        int start = start1Based - 1;
        int end = start + length;
        return line.substring(start, end);
    }

    private BigDecimal parseMoney13(String raw) {
        String digits = raw.trim();
        if (digits.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(digits).movePointLeft(2);
    }
}
