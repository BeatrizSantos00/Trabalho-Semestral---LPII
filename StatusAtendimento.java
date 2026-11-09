package com.petshop.model;

public enum StatusAtendimento {
    AGENDADO(true),
    EM_ANDAMENTO(true),
    CONCLUIDO(false),
    CANCELADO(false);

    private final boolean permiteAlterarServicos;

    StatusAtendimento(boolean permiteAlterarServicos) {
        this.permiteAlterarServicos = permiteAlterarServicos;
    }

    /** Atendimentos concluídos ou cancelados ficam "congelados". */
    public boolean permiteAlterarServicos() {
        return permiteAlterarServicos;
    }
}
