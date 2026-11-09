package com.petshop.exception;

/** Dados inválidos enviados pelo cliente (HTTP 400). */
public class ValidationException extends RuntimeException {
    public ValidationException(String mensagem) {
        super(mensagem);
    }
}
