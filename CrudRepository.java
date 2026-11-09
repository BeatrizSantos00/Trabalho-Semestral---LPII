package com.petshop.repository;

import java.util.List;
import java.util.Optional;

/** Contrato comum de persistência. Cada entidade tem a sua implementação (polimorfismo). */
public interface CrudRepository<T> {

    T salvar(T entidade);

    Optional<T> buscarPorId(Long id);

    List<T> listar();

    /** @return true se o registro existia e foi atualizado */
    boolean atualizar(T entidade);

    /** @return true se o registro existia e foi removido */
    boolean remover(Long id);
}
