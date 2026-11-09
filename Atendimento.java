package com.petshop.model;

import com.petshop.exception.ConflictException;
import com.petshop.exception.ValidationException;
import com.petshop.util.Validacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Visita de um pet ao pet shop. Reúne vários serviços (N..N) e calcula o valor total. */
public class Atendimento extends BaseEntity {

    private Long petId;
    private Long veterinarioId;
    private LocalDateTime dataHora;
    private StatusAtendimento status;
    private String observacoes;
    private BigDecimal valorTotal = BigDecimal.ZERO.setScale(2);
    private final List<ItemAtendimento> itens = new ArrayList<>();

    public Atendimento(Long petId, Long veterinarioId, LocalDateTime dataHora, String observacoes) {
        setPetId(petId);
        setVeterinarioId(veterinarioId);
        setDataHora(dataHora);
        setObservacoes(observacoes);
        this.status = StatusAtendimento.AGENDADO;
    }

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        if (petId == null || petId <= 0) {
            throw new ValidationException("O campo 'petId' é obrigatório");
        }
        this.petId = petId;
    }

    public Long getVeterinarioId() {
        return veterinarioId;
    }

    public void setVeterinarioId(Long veterinarioId) {
        if (veterinarioId == null || veterinarioId <= 0) {
            throw new ValidationException("O campo 'veterinarioId' é obrigatório");
        }
        this.veterinarioId = veterinarioId;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        if (dataHora == null) {
            throw new ValidationException("O campo 'dataHora' é obrigatório");
        }
        this.dataHora = dataHora;
    }

    public StatusAtendimento getStatus() {
        return status;
    }

    public void setStatus(StatusAtendimento status) {
        if (status == null) {
            throw new ValidationException("O campo 'status' é obrigatório");
        }
        this.status = status;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = Validacao.textoOpcional(observacoes, "observacoes", 500);
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public List<ItemAtendimento> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public void adicionarItem(ItemAtendimento item) {
        boolean jaExiste = itens.stream().anyMatch(i -> i.getServicoId().equals(item.getServicoId()));
        if (jaExiste) {
            throw new ConflictException(
                    "O serviço " + item.getServicoId() + " já está incluído neste atendimento");
        }
        itens.add(item);
        recalcularTotal();
    }

    public boolean removerItem(Long servicoId) {
        boolean removido = itens.removeIf(i -> i.getServicoId().equals(servicoId));
        recalcularTotal();
        return removido;
    }

    public void recalcularTotal() {
        this.valorTotal = itens.stream()
                .map(ItemAtendimento::getPrecoCobrado)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2);
    }
}
