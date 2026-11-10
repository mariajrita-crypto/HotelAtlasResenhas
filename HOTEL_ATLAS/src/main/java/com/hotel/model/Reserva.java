package com.hotel.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Reserva {
    private Long id;
    private Long hospedeId;
    private Long quartoId;
    private LocalDateTime dataEntrada;
    private LocalDateTime dataSaida;
    private Integer quantidadeHospedes;
    private BigDecimal valorTotal;
    private StatusReserva status;
    private String observacoes;

    public Reserva() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getHospedeId() { return hospedeId; }
    public void setHospedeId(Long hospedeId) { this.hospedeId = hospedeId; }

    public Long getQuartoId() { return quartoId; }
    public void setQuartoId(Long quartoId) { this.quartoId = quartoId; }

    public LocalDateTime getDataEntrada() { return dataEntrada; }
    public void setDataEntrada(LocalDateTime dataEntrada) { this.dataEntrada = dataEntrada; }

    public LocalDateTime getDataSaida() { return dataSaida; }
    public void setDataSaida(LocalDateTime dataSaida) { this.dataSaida = dataSaida; }

    public Integer getQuantidadeHospedes() { return quantidadeHospedes; }
    public void setQuantidadeHospedes(Integer quantidadeHospedes) { this.quantidadeHospedes = quantidadeHospedes; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }

    public StatusReserva getStatus() { return status; }
    public void setStatus(StatusReserva status) { this.status = status; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}