package com.equals.desafio.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "sales")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // do arquivo: estabelecimento (campo posicional)
    @Column(name = "establishment_code", length = 10, nullable = false)
    private String establishmentCode;

    // do arquivo: data do evento (AAAAMMDD)
    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    // do arquivo: hora do evento (HHMMSS) - pode ser null se você decidir não usar agora
    @Column(name = "event_time")
    private LocalTime eventTime;

    // do arquivo: bandeira/instituição financeira
    @Column(name = "brand", length = 30)
    private String brand;

    // do arquivo: valor total (guarde em reais como decimal)
    @Column(name = "total_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    public Sale() {}

    // getters/setters (iniciante-friendly: sem Lombok por enquanto)
    public Long getId() { return id; }

    public String getEstablishmentCode() { return establishmentCode; }
    public void setEstablishmentCode(String establishmentCode) { this.establishmentCode = establishmentCode; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public LocalTime getEventTime() { return eventTime; }
    public void setEventTime(LocalTime eventTime) { this.eventTime = eventTime; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
}
