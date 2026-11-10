package com.hotel.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MovimentacaoFinanceira {
    private Long id;
    private String descricao;
    private BigDecimal valor;
    private String tipo; // RECEITA ou DESPESA
    private TipoPagamento formaPagamento;
    private LocalDateTime dataHora;
    private Long reservaId;

    public MovimentacaoFinanceira() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public TipoPagamento getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(TipoPagamento formaPagamento) { this.formaPagamento = formaPagamento; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public Long getReservaId() { return reservaId; }
    public void setReservaId(Long reservaId) { this.reservaId = reservaId; }
}