package com.petshop.model;

import com.petshop.util.Validacao;

import java.math.BigDecimal;

/** Serviço do catálogo do pet shop (banho, tosa, hidratação...). */
public class Servico extends BaseEntity {

    private String nome;
    private String descricao;
    private BigDecimal precoBase;
    private int duracaoMinutos;

    public Servico(String nome, String descricao, BigDecimal precoBase, Integer duracaoMinutos) {
        setNome(nome);
        setDescricao(descricao);
        setPrecoBase(precoBase);
        setDuracaoMinutos(duracaoMinutos);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = Validacao.textoObrigatorio(nome, "nome", 100);
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = Validacao.textoOpcional(descricao, "descricao", 300);
    }

    public BigDecimal getPrecoBase() {
        return precoBase;
    }

    public void setPrecoBase(BigDecimal precoBase) {
        this.precoBase = Validacao.valorPositivo(precoBase, "precoBase");
    }

    public int getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public void setDuracaoMinutos(Integer duracaoMinutos) {
        this.duracaoMinutos = Validacao.inteiroPositivo(duracaoMinutos, "duracaoMinutos");
    }
}
