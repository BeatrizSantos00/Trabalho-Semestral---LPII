package com.petshop.model;

import com.petshop.exception.ValidationException;
import com.petshop.util.Validacao;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Gato: o adicional de preço varia conforme a PELAGEM. */
public class Gato extends Pet {

    private Pelagem pelagem;

    public Gato(Long tutorId, String nome, LocalDate dataNascimento, String raca, Pelagem pelagem) {
        super(TipoPet.GATO, tutorId, nome, dataNascimento, raca);
        setPelagem(pelagem);
    }

    public Pelagem getPelagem() {
        return pelagem;
    }

    public void setPelagem(Pelagem pelagem) {
        if (pelagem == null) {
            throw new ValidationException("O campo 'pelagem' é obrigatório para gatos");
        }
        this.pelagem = pelagem;
    }

    @Override
    public BigDecimal calcularAdicional() {
        return switch (pelagem) {
            case CURTA -> new BigDecimal("0.00");
            case LONGA -> new BigDecimal("20.00");
        };
    }

    @Override
    public String getCaracteristica() {
        return pelagem.name();
    }

    @Override
    public void definirCaracteristica(String valor) {
        setPelagem(Validacao.converter(Pelagem.class, valor, "pelagem"));
    }
}
