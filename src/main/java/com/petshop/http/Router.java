package com.petshop.http;

import com.petshop.exception.ConflictException;
import com.petshop.exception.NotFoundException;
import com.petshop.exception.ValidationException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Roteador mínimo: associa método + padrão de caminho (ex.: /pets/{id}) a um Handler
 * e traduz exceções de negócio em respostas HTTP.
 */
public final class Router implements HttpHandler {

    private record Rota(String metodo, String[] segmentos, Handler handler) {
    }

    private final List<Rota> rotas = new ArrayList<>();

    public void get(String padrao, Handler handler) {
        adicionar("GET", padrao, handler);
    }

    public void post(String padrao, Handler handler) {
        adicionar("POST", padrao, handler);
    }

    public void put(String padrao, Handler handler) {
        adicionar("PUT", padrao, handler);
    }

    public void delete(String padrao, Handler handler) {
        adicionar("DELETE", padrao, handler);
    }

    private void adicionar(String metodo, String padrao, Handler handler) {
        rotas.add(new Rota(metodo, dividir(padrao), handler));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String[] caminho = dividir(exchange.getRequestURI().getPath());
            String metodo = exchange.getRequestMethod();
            Set<String> metodosPermitidos = new LinkedHashSet<>();

            for (Rota rota : rotas) {
                Map<String, String> parametros = casar(rota.segmentos(), caminho);
                if (parametros == null) {
                    continue;
                }
                if (rota.metodo().equals(metodo)) {
                    rota.handler().tratar(new RequestContext(exchange, parametros));
                    return;
                }
                metodosPermitidos.add(rota.metodo());
            }

            RequestContext contexto = new RequestContext(exchange, Map.of());
            if (!metodosPermitidos.isEmpty()) {
                exchange.getResponseHeaders().set("Allow", String.join(", ", metodosPermitidos));
                contexto.erro(405, "Método " + metodo + " não permitido para esta rota");
            } else {
                contexto.erro(404, "Rota não encontrada");
            }
        } catch (ValidationException e) {
            responderErro(exchange, 400, e.getMessage());
        } catch (NotFoundException e) {
            responderErro(exchange, 404, e.getMessage());
        } catch (ConflictException e) {
            responderErro(exchange, 409, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            responderErro(exchange, 500, "Erro interno do servidor");
        } finally {
            exchange.close();
        }
    }

    private void responderErro(HttpExchange exchange, int status, String mensagem) {
        try {
            new RequestContext(exchange, Map.of()).erro(status, mensagem);
        } catch (IOException | RuntimeException ignorado) {
            // a resposta já pode ter sido enviada ou a conexão fechada
        }
    }

    private static Map<String, String> casar(String[] padrao, String[] caminho) {
        if (padrao.length != caminho.length) {
            return null;
        }
        Map<String, String> parametros = new HashMap<>();
        for (int i = 0; i < padrao.length; i++) {
            if (padrao[i].startsWith("{") && padrao[i].endsWith("}")) {
                parametros.put(padrao[i].substring(1, padrao[i].length() - 1), caminho[i]);
            } else if (!padrao[i].equals(caminho[i])) {
                return null;
            }
        }
        return parametros;
    }

    private static String[] dividir(String caminho) {
        return java.util.Arrays.stream(caminho.split("/"))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }
}
