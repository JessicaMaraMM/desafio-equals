package com.equals.desafio.controller;

import com.equals.desafio.domain.Sale;
import com.equals.desafio.parser.SaleParser;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestParserController {

    private final SaleParser saleParser;

    public TestParserController(SaleParser saleParser) {
        this.saleParser = saleParser;
    }

    @PostMapping("/parse")
    public Sale parse(@RequestBody String line) {
        return saleParser.parse(line);
    }
}
