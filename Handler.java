package com.petshop.http;

/** Tratador de uma rota da API. */
@FunctionalInterface
public interface Handler {
    void tratar(RequestContext contexto) throws Exception;
}
