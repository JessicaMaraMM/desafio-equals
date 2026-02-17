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

    public Sale parse(String line) {

        if (line == null) {
            throw new IllegalArgumentException("Linha inválida: null");
        }

        if (line.length() < SaleLayout.DETAIL_MIN_LENGTH) {
            line = padRight(line, SaleLayout.DETAIL_MIN_LENGTH);
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
                : LocalTime.parse(eventTimeStr.trim(), TIME_HHMMSS);

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
        if (raw == null) {
            return BigDecimal.ZERO;
        }

        String digits = raw.trim();

        if (digits.isEmpty()) {
            return BigDecimal.ZERO;
        }

        digits = digits.replaceAll("[^0-9]", "");

        if (digits.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal(digits).movePointLeft(2);
    }

    private String padRight(String s, int n) {
        if (s.length() >= n)
            return s;
        StringBuilder sb = new StringBuilder(n);
        sb.append(s);
        while (sb.length() < n)
            sb.append(' ');
        return sb.toString();
    }
}