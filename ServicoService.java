package com.petshop.service;

import com.petshop.exception.NotFoundException;
import com.petshop.model.Servico;
import com.petshop.repository.ServicoRepository;

import java.math.BigDecimal;
import java.util.List;

public class ServicoService {

    private final ServicoRepository repositorio;

    public ServicoService(ServicoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Servico criar(String nome, String descricao, BigDecimal precoBase, Integer duracaoMinutos) {
        return repositorio.salvar(new Servico(nome, descricao, precoBase, duracaoMinutos));
    }

    public Servico buscar(Long id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new NotFoundException("Serviço " + id + " não encontrado"));
    }

    public List<Servico> listar() {
        return repositorio.listar();
    }

    public Servico atualizar(Long id, String nome, String descricao, BigDecimal precoBase,
                             Integer duracaoMinutos) {
        Servico servico = buscar(id);
        servico.setNome(nome);
        servico.setDescricao(descricao);
        servico.setPrecoBase(precoBase);
        servico.setDuracaoMinutos(duracaoMinutos);
        repositorio.atualizar(servico);
        return servico;
    }

    /** Falha com 409 se o serviço já foi usado em algum atendimento. */
    public void remover(Long id) {
        buscar(id);
        repositorio.remover(id);
    }
}
