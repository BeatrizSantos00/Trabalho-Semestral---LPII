package com.petshop.exception;

/** Operação em conflito com o estado atual dos dados (HTTP 409). */
public class ConflictException extends RuntimeException {
    public ConflictException(String mensagem) {
        super(mensagem);
    }
}
