package com.petshop.util;

import com.petshop.exception.ValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Locale;

/** Regras de validação reutilizadas pelos setters das entidades. */
public final class Validacao {

    private Validacao() {
    }

    public static String textoObrigatorio(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank()) {
            throw new ValidationException("O campo '" + campo + "' é obrigatório");
        }
        return limitar(valor.trim(), campo, maximo);
    }

    public static String textoOpcional(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return limitar(valor.trim(), campo, maximo);
    }

    private static String limitar(String valor, String campo, int maximo) {
        if (valor.length() > maximo) {
            throw new ValidationException(
                    "O campo '" + campo + "' deve ter no máximo " + maximo + " caracteres");
        }
        return valor;
    }

    public static BigDecimal valorPositivo(BigDecimal valor, String campo) {
        if (valor == null || valor.signum() <= 0) {
            throw new ValidationException("O campo '" + campo + "' deve ser maior que zero");
        }
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    public static int inteiroPositivo(Integer valor, String campo) {
        if (valor == null || valor <= 0) {
            throw new ValidationException("O campo '" + campo + "' deve ser maior que zero");
        }
        return valor;
    }

    public static <E extends Enum<E>> E converter(Class<E> tipo, String valor, String campo) {
        String aceitos = Arrays.toString(tipo.getEnumConstants());
        if (valor == null || valor.isBlank()) {
            throw new ValidationException(
                    "O campo '" + campo + "' é obrigatório. Valores aceitos: " + aceitos);
        }
        try {
            return Enum.valueOf(tipo, valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ValidationException(
                    "Valor inválido para '" + campo + "'. Valores aceitos: " + aceitos);
        }
    }
}
