package com.equals.desafio.service;

import com.equals.desafio.domain.Sale;
import com.equals.desafio.parser.SaleParser;
import com.equals.desafio.repository.SaleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class SaleImportServiceTest {

    @Test
    void deveImportarSomenteLinhasTipo1_eSalvarNoRepositorio() {

        SaleParser parser = mock(SaleParser.class);
        SaleRepository repository = mock(SaleRepository.class);

        SaleImportService service = new SaleImportService(parser, repository);

        String conteudo = "0HEADER QUALQUER\n" +
                "\n" +
                "1LINHA_DETALHE_1\n" +
                "2TRAILER QUALQUER\n" +
                "1LINHA_DETALHE_2\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "teste.txt",
                "text/plain",
                conteudo.getBytes(StandardCharsets.UTF_8));

        Sale sale1 = new Sale();
        sale1.setEstablishmentCode("1234567891");
        sale1.setEventDate(LocalDate.of(2018, 9, 25));
        sale1.setEventTime(LocalTime.of(13, 17, 36));
        sale1.setBrand("MASTERCARD");
        sale1.setTotalAmount(new BigDecimal("1.00"));
        sale1.setTransactionCode("A".repeat(32));
        sale1.setNetAmount(new BigDecimal("0.98"));

        Sale sale2 = new Sale();
        sale2.setEstablishmentCode("1234567891");
        sale2.setEventDate(LocalDate.of(2018, 9, 25));
        sale2.setEventTime(LocalTime.of(13, 18, 34));
        sale2.setBrand("MASTERCARD");
        sale2.setTotalAmount(new BigDecimal("1.01"));
        sale2.setTransactionCode("B".repeat(32));
        sale2.setNetAmount(new BigDecimal("1.00"));

        when(parser.parse(anyString())).thenAnswer(invocation -> {
            String arg = invocation.getArgument(0, String.class);
            String trimmed = arg.trim();

            if (trimmed.equals("1LINHA_DETALHE_1"))
                return sale1;
            if (trimmed.equals("1LINHA_DETALHE_2"))
                return sale2;

            return null;
        });

        SaleImportService.ImportResult result = service.importFile(file);

        assertEquals(5, result.totalLines());
        assertEquals(2, result.detailLines());
        assertEquals(2, result.saved());
        assertEquals(3, result.ignored());
        assertEquals(0, result.invalid());

        verify(parser, times(2)).parse(anyString());

        verify(repository).saveAll(argThat(iterable -> {
            if (iterable == null) return false;

            List<Sale> list = new ArrayList<>();
            iterable.forEach(list::add);

            return list.size() == 2
            && "A".repeat(32).equals(list.get(0).getTransactionCode())
            && "B".repeat(32).equals(list.get(1).getTransactionCode());
        }));
    }

    @Test
    void deveFalharQuandoArquivoVazio() {
        SaleParser parser = mock(SaleParser.class);
        SaleRepository repository = mock(SaleRepository.class);
        SaleImportService service = new SaleImportService(parser, repository);

        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "vazio.txt",
                "text/plain",
                new byte[0]);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.importFile(emptyFile));
        assertTrue(ex.getMessage().toLowerCase().contains("arquivo vazio"));
        verify(repository, never()).saveAll(any());
    }
}