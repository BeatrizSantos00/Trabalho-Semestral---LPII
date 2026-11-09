package com.petshop.model;

import com.petshop.exception.ValidationException;
import com.petshop.util.Validacao;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Abstração de um animal de estimação. Cada espécie define a sua própria regra
 * de adicional de preço (polimorfismo) e a sua característica específica.
 */
public abstract class Pet extends BaseEntity {

    private final TipoPet tipo;
    private Long tutorId;
    private String nome;
    private LocalDate dataNascimento;
    private String raca;

    protected Pet(TipoPet tipo, Long tutorId, String nome, LocalDate dataNascimento, String raca) {
        this.tipo = tipo;
        setTutorId(tutorId);
        setNome(nome);
        setDataNascimento(dataNascimento);
        setRaca(raca);
    }

    /** Valor adicional cobrado sobre cada serviço, conforme a espécie. */
    public abstract BigDecimal calcularAdicional();

    /** Característica que define o adicional (porte ou pelagem), em texto. */
    public abstract String getCaracteristica();

    /** Atualiza a característica específica da espécie a partir de um texto. */
    public abstract void definirCaracteristica(String valor);

    public TipoPet getTipo() {
        return tipo;
    }

    public Long getTutorId() {
        return tutorId;
    }

    public void setTutorId(Long tutorId) {
        if (tutorId == null || tutorId <= 0) {
            throw new ValidationException("O campo 'tutorId' é obrigatório");
        }
        this.tutorId = tutorId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = Validacao.textoObrigatorio(nome, "nome", 80);
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        if (dataNascimento != null && dataNascimento.isAfter(LocalDate.now())) {
            throw new ValidationException("O campo 'dataNascimento' não pode ser uma data futura");
        }
        this.dataNascimento = dataNascimento;
    }

    public String getRaca() {
        return raca;
    }

    public void setRaca(String raca) {
        this.raca = Validacao.textoOpcional(raca, "raca", 80);
    }
}
