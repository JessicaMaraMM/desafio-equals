package com.equals.desafio.parser;

import com.equals.desafio.domain.Sale;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class SaleParserTest {

    private final SaleParser parser = new SaleParser();

    @Test
    void deveParsearLinhaDetalheTipo1_comCamposPrincipais() throws Exception {
        String line = readDetailLineByContains(
                "processoSeletivoEquals.txt",
                "10203040506070809010203040506070" // pedaço do transactionCode
        );

        Sale sale = parser.parse(line);

        assertNotNull(sale);
        assertEquals("1234567891", sale.getEstablishmentCode());
        assertEquals(LocalDate.of(2018, 9, 25), sale.getEventDate());
        assertEquals(LocalTime.of(13, 18, 34), sale.getEventTime());
        assertEquals("MASTERCARD", sale.getBrand());

        assertNotNull(sale.getTotalAmount());
        assertTrue(sale.getTotalAmount().compareTo(BigDecimal.ZERO) > 0);

        assertNotNull(sale.getTransactionCode());
        assertEquals(32, sale.getTransactionCode().length());
    }

    @Test
    void deveFalharQuandoLinhaNaoEhTipo1() {
        String headerLine = "0" + "X".repeat(529);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> parser.parse(headerLine));
        assertTrue(ex.getMessage().toLowerCase().contains("tipo 1"));
    }

    private String readDetailLineByContains(String resourceName, String mustContain) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName);
        assertNotNull(is, "Arquivo não encontrado em src/test/resources: " + resourceName);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty() && line.charAt(0) == '1' && line.contains(mustContain)) {
                    return line;
                }
            }
        }
        throw new IllegalArgumentException(
                "Nenhuma linha tipo '1' contendo '" + mustContain + "' encontrada em: " + resourceName);
    }
}