package com.equals.desafio.repository;

import com.equals.desafio.domain.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByEventDateBetween(LocalDate start, LocalDate end);

    List<Sale> findByEventDateGreaterThanEqual(LocalDate start);

    List<Sale> findByEventDateLessThanEqual(LocalDate end);
}