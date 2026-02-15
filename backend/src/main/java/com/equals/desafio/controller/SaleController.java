package com.equals.desafio.controller;

import com.equals.desafio.domain.Sale;
import com.equals.desafio.repository.SaleRepository;
import org.springframework.web.bind.annotation.*;

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
    public List<Sale> list() {
        return saleRepository.findAll();
    }
}
