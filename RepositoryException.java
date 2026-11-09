package com.petshop.exception;

/** Falha inesperada de acesso ao banco de dados (HTTP 500). */
public class RepositoryException extends RuntimeException {
    public RepositoryException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
