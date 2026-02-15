package com.equals.desafio.controller;

import com.equals.desafio.domain.Sale;
import com.equals.desafio.repository.SaleRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/sales")
public class SaleController {

    private final SaleRepository saleRepository;

    public SaleController(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    @PostMapping
    public Sale create(@RequestBody Sale sale) {
        return saleRepository.save(sale);
    }

    @GetMapping
    public List<Sale> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        if (start != null && end != null) {
            return saleRepository.findByEventDateBetween(start, end);
        }
        if (start != null) {
            return saleRepository.findByEventDateGreaterThanEqual(start);
        }
        if (end != null) {
            return saleRepository.findByEventDateLessThanEqual(end);
        }
        return saleRepository.findAll();
    }
}
