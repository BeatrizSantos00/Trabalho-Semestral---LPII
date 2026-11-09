package com.petshop.model;

import java.math.BigDecimal;

/**
 * Linha da relação N..N entre Atendimento e Serviço. Guarda o preço cobrado na
 * data do atendimento, para que reajustes futuros no catálogo não alterem o histórico.
 */
public class ItemAtendimento {

    private final Long servicoId;
    private final String nomeServico;
    private final BigDecimal precoCobrado;

    public ItemAtendimento(Long servicoId, String nomeServico, BigDecimal precoCobrado) {
        this.servicoId = servicoId;
        this.nomeServico = nomeServico;
        this.precoCobrado = precoCobrado;
    }

    public Long getServicoId() {
        return servicoId;
    }

    public String getNomeServico() {
        return nomeServico;
    }

    public BigDecimal getPrecoCobrado() {
        return precoCobrado;
    }
}
