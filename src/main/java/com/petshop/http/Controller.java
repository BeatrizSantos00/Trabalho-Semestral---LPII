package com.petshop.http;

/** Todo controller sabe registrar as suas próprias rotas no roteador. */
public interface Controller {
    void registrar(Router roteador);
}
