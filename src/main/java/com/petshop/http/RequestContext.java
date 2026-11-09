package com.petshop.http;

import com.petshop.exception.ValidationException;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Encapsula a requisição/resposta HTTP para que os controllers não dependam do HttpExchange. */
public final class RequestContext {

    private final HttpExchange exchange;
    private final Map<String, String> parametros;

    RequestContext(HttpExchange exchange, Map<String, String> parametros) {
        this.exchange = exchange;
        this.parametros = parametros;
    }

    /** Lê um parâmetro de caminho (ex.: {id}) como número inteiro positivo. */
    public Long parametroId(String nome) {
        try {
            long valor = Long.parseLong(parametros.get(nome));
            if (valor <= 0) {
                throw new NumberFormatException();
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new ValidationException(
                    "O parâmetro '" + nome + "' da URL deve ser um número inteiro positivo");
        }
    }

    public JsonBody corpo() throws IOException {
        String bruto = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        return JsonBody.de(bruto);
    }

    public void json(int status, Object corpo) throws IOException {
        byte[] bytes = Json.paraJson(corpo).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream saida = exchange.getResponseBody()) {
            saida.write(bytes);
        }
    }

    public void criado(String localizacao, Object corpo) throws IOException {
        exchange.getResponseHeaders().set("Location", localizacao);
        json(201, corpo);
    }

    public void semConteudo() throws IOException {
        exchange.sendResponseHeaders(204, -1);
    }

    public void erro(int status, String mensagem) throws IOException {
        json(status, Map.of("erro", mensagem));
    }

    HttpExchange exchange() {
        return exchange;
    }
}
