package com.petshop.model;

import com.petshop.exception.ValidationException;
import com.petshop.util.Validacao;

import java.util.regex.Pattern;

/** Dono dos animais. Um tutor possui vários pets (1..N). */
public class Tutor extends BaseEntity {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private String nome;
    private String cpf;
    private String telefone;
    private String email;

    public Tutor(String nome, String cpf, String telefone, String email) {
        setNome(nome);
        setCpf(cpf);
        setTelefone(telefone);
        setEmail(email);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = Validacao.textoObrigatorio(nome, "nome", 120);
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        String digitos = Validacao.textoObrigatorio(cpf, "cpf", 14).replaceAll("\\D", "");
        if (!cpfValido(digitos)) {
            throw new ValidationException("O campo 'cpf' é inválido");
        }
        this.cpf = digitos;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        String limpo = Validacao.textoOpcional(telefone, "telefone", 20);
        if (limpo != null) {
            String digitos = limpo.replaceAll("\\D", "");
            if (digitos.length() < 10 || digitos.length() > 11) {
                throw new ValidationException("O campo 'telefone' deve ter 10 ou 11 dígitos (com DDD)");
            }
            limpo = digitos;
        }
        this.telefone = limpo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        String limpo = Validacao.textoOpcional(email, "email", 120);
        if (limpo != null && !EMAIL.matcher(limpo).matches()) {
            throw new ValidationException("O campo 'email' é inválido");
        }
        this.email = limpo;
    }

    private static boolean cpfValido(String cpf) {
        if (cpf.length() != 11 || cpf.chars().distinct().count() == 1) {
            return false;
        }
        for (int tamanho = 9; tamanho < 11; tamanho++) {
            int soma = 0;
            for (int i = 0; i < tamanho; i++) {
                soma += (cpf.charAt(i) - '0') * (tamanho + 1 - i);
            }
            int digito = (soma * 10) % 11;
            if (digito == 10) {
                digito = 0;
            }
            if (digito != cpf.charAt(tamanho) - '0') {
                return false;
            }
        }
        return true;
    }
}
