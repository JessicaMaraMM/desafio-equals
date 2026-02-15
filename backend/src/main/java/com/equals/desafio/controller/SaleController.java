package com.equals.desafio.controller;

import com.equals.desafio.domain.Sale;
import com.equals.desafio.service.SaleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sales")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @PostMapping
    public Sale create(@RequestBody Sale sale) {
        return saleService.save(sale);
    }

    @GetMapping
    public List<Sale> list() {
        return saleService.findAll();
    }
}
