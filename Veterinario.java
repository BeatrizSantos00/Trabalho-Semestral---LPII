package com.petshop.model;

import com.petshop.exception.ConflictException;
import com.petshop.util.Validacao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Profissional que realiza os atendimentos. Relaciona-se N..N com Serviço
 * (serviços em que está habilitado) e 1..N com Atendimento.
 */
public class Veterinario extends BaseEntity {

    private String nome;
    private String crmv;
    private String especialidade;
    private final List<Servico> servicos = new ArrayList<>();

    public Veterinario(String nome, String crmv, String especialidade) {
        setNome(nome);
        setCrmv(crmv);
        setEspecialidade(especialidade);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = Validacao.textoObrigatorio(nome, "nome", 120);
    }

    public String getCrmv() {
        return crmv;
    }

    public void setCrmv(String crmv) {
        this.crmv = Validacao.textoObrigatorio(crmv, "crmv", 20).toUpperCase();
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = Validacao.textoOpcional(especialidade, "especialidade", 100);
    }

    /** Serviços em que o veterinário está habilitado. */
    public List<Servico> getServicos() {
        return Collections.unmodifiableList(servicos);
    }

    public boolean estaHabilitadoEm(Long servicoId) {
        return servicos.stream().anyMatch(s -> s.getId().equals(servicoId));
    }

    public void habilitar(Servico servico) {
        if (estaHabilitadoEm(servico.getId())) {
            throw new ConflictException("O veterinário já está habilitado no serviço " + servico.getId());
        }
        servicos.add(servico);
    }

    public boolean desabilitar(Long servicoId) {
        return servicos.removeIf(s -> s.getId().equals(servicoId));
    }
}
