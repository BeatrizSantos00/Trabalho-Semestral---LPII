package com.petshop.model;

import com.petshop.exception.ValidationException;
import com.petshop.util.Validacao;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Cachorro: o adicional de preço varia conforme o PORTE. */
public class Cachorro extends Pet {

    private Porte porte;

    public Cachorro(Long tutorId, String nome, LocalDate dataNascimento, String raca, Porte porte) {
        super(TipoPet.CACHORRO, tutorId, nome, dataNascimento, raca);
        setPorte(porte);
    }

    public Porte getPorte() {
        return porte;
    }

    public void setPorte(Porte porte) {
        if (porte == null) {
            throw new ValidationException("O campo 'porte' é obrigatório para cachorros");
        }
        this.porte = porte;
    }

    @Override
    public BigDecimal calcularAdicional() {
        return switch (porte) {
            case PEQUENO -> new BigDecimal("0.00");
            case MEDIO -> new BigDecimal("15.00");
            case GRANDE -> new BigDecimal("30.00");
        };
    }

    @Override
    public String getCaracteristica() {
        return porte.name();
    }

    @Override
    public void definirCaracteristica(String valor) {
        setPorte(Validacao.converter(Porte.class, valor, "porte"));
    }
}
