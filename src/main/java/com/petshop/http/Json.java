package com.petshop.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Serialização de objetos para JSON (Gson), com datas em formato ISO-8601. */
public final class Json {

    private static final DateTimeFormatter DATA_HORA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .registerTypeAdapter(LocalDate.class,
                    (JsonSerializer<LocalDate>) (data, tipo, ctx) -> new JsonPrimitive(data.toString()))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (data, tipo, ctx) ->
                            new JsonPrimitive(data.format(DATA_HORA)))
            .create();

    private Json() {
    }

    public static String paraJson(Object objeto) {
        return GSON.toJson(objeto);
    }
}
