package com.petshop.model;

import java.time.LocalDateTime;

/** Classe base abstrata: identidade e data de criação comuns a todas as entidades. */
public abstract class BaseEntity {

    private Long id;
    private LocalDateTime criadoEm;

    protected BaseEntity() {
        this.criadoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}
