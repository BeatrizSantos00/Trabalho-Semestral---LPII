package com.petshop.http;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.petshop.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Leitura tipada do corpo JSON da requisição. Só converte tipos; as regras de
 * negócio continuam nos setters das entidades.
 */
public final class JsonBody {

    private final JsonObject objeto;

    private JsonBody(JsonObject objeto) {
        this.objeto = objeto;
    }

    public static JsonBody de(String bruto) {
        if (bruto == null || bruto.isBlank()) {
            throw new ValidationException("O corpo da requisição é obrigatório (JSON)");
        }
        try {
            JsonElement elemento = JsonParser.parseString(bruto);
            if (!elemento.isJsonObject()) {
                throw new ValidationException("O corpo da requisição deve ser um objeto JSON");
            }
            return new JsonBody(elemento.getAsJsonObject());
        } catch (com.google.gson.JsonParseException e) {
            throw new ValidationException("JSON inválido no corpo da requisição");
        }
    }

    /** Indica se o campo foi enviado com algum valor (diferente de ausente ou null). */
    public boolean contem(String campo) {
        return elemento(campo) != null;
    }

    public String texto(String campo) {
        JsonElement e = elemento(campo);
        if (e == null) {
            return null;
        }
        if (!e.isJsonPrimitive()) {
            throw invalido(campo);
        }
        return e.getAsString();
    }

    public Long inteiroLongo(String campo) {
        JsonElement e = elemento(campo);
        if (e == null) {
            return null;
        }
        try {
            return e.getAsBigDecimal().longValueExact();
        } catch (RuntimeException ex) {
            throw invalido(campo);
        }
    }

    public Integer inteiro(String campo) {
        Long valor = inteiroLongo(campo);
        if (valor == null) {
            return null;
        }
        if (valor > Integer.MAX_VALUE || valor < Integer.MIN_VALUE) {
            throw invalido(campo);
        }
        return valor.intValue();
    }

    public BigDecimal decimal(String campo) {
        JsonElement e = elemento(campo);
        if (e == null) {
            return null;
        }
        try {
            return e.getAsBigDecimal();
        } catch (RuntimeException ex) {
            throw invalido(campo);
        }
    }

    public LocalDate data(String campo) {
        String valor = texto(campo);
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(valor.trim());
        } catch (DateTimeParseException ex) {
            throw new ValidationException("O campo '" + campo + "' deve estar no formato AAAA-MM-DD");
        }
    }

    public LocalDateTime dataHora(String campo) {
        String valor = texto(campo);
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(valor.trim());
        } catch (DateTimeParseException ex) {
            throw new ValidationException(
                    "O campo '" + campo + "' deve estar no formato AAAA-MM-DDTHH:MM (ex.: 2026-11-10T14:30)");
        }
    }

    public List<Long> listaInteiros(String campo) {
        JsonElement e = elemento(campo);
        List<Long> valores = new ArrayList<>();
        if (e == null) {
            return valores;
        }
        if (!e.isJsonArray()) {
            throw invalido(campo);
        }
        JsonArray array = e.getAsJsonArray();
        for (JsonElement item : array) {
            try {
                valores.add(item.getAsBigDecimal().longValueExact());
            } catch (RuntimeException ex) {
                throw invalido(campo);
            }
        }
        return valores;
    }

    private JsonElement elemento(String campo) {
        JsonElement e = objeto.get(campo);
        return (e == null || e.isJsonNull()) ? null : e;
    }

    private ValidationException invalido(String campo) {
        return new ValidationException("O campo '" + campo + "' possui um valor inválido");
    }
}
