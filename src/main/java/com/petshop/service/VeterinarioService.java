package com.petshop.service;

import com.petshop.exception.NotFoundException;
import com.petshop.model.Servico;
import com.petshop.model.Veterinario;
import com.petshop.repository.ServicoRepository;
import com.petshop.repository.VeterinarioRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VeterinarioService {

    private final VeterinarioRepository veterinarioRepositorio;
    private final ServicoRepository servicoRepositorio;

    public VeterinarioService(VeterinarioRepository veterinarioRepositorio,
                              ServicoRepository servicoRepositorio) {
        this.veterinarioRepositorio = veterinarioRepositorio;
        this.servicoRepositorio = servicoRepositorio;
    }

    public Veterinario criar(String nome, String crmv, String especialidade, List<Long> servicoIds) {
        Veterinario veterinario = new Veterinario(nome, crmv, especialidade);
        habilitarTodos(veterinario, servicoIds);
        return veterinarioRepositorio.salvar(veterinario);
    }

    public Veterinario buscar(Long id) {
        return veterinarioRepositorio.buscarPorId(id)
                .orElseThrow(() -> new NotFoundException("Veterinário " + id + " não encontrado"));
    }

    public List<Veterinario> listar() {
        return veterinarioRepositorio.listar();
    }

    public List<Veterinario> listarPorServico(Long servicoId) {
        buscarServico(servicoId);
        return veterinarioRepositorio.listarPorServico(servicoId);
    }

    /** Se "servicoIds" for nulo, as habilitações atuais são mantidas; senão, são substituídas. */
    public Veterinario atualizar(Long id, String nome, String crmv, String especialidade,
                                 List<Long> servicoIds) {
        Veterinario atual = buscar(id);
        atual.setNome(nome);
        atual.setCrmv(crmv);
        atual.setEspecialidade(especialidade);
        if (servicoIds != null) {
            new java.util.ArrayList<>(atual.getServicos())
                    .forEach(s -> atual.desabilitar(s.getId()));
            habilitarTodos(atual, servicoIds);
        }
        veterinarioRepositorio.atualizar(atual);
        return atual;
    }

    /** Falha com 409 se o veterinário já realizou atendimentos. */
    public void remover(Long id) {
        buscar(id);
        veterinarioRepositorio.remover(id);
    }

    public Veterinario habilitarServico(Long veterinarioId, Long servicoId) {
        Veterinario veterinario = buscar(veterinarioId);
        veterinario.habilitar(buscarServico(servicoId));
        veterinarioRepositorio.atualizar(veterinario);
        return veterinario;
    }

    public Veterinario desabilitarServico(Long veterinarioId, Long servicoId) {
        Veterinario veterinario = buscar(veterinarioId);
        if (!veterinario.desabilitar(servicoId)) {
            throw new NotFoundException("O veterinário " + veterinarioId
                    + " não está habilitado no serviço " + servicoId);
        }
        veterinarioRepositorio.atualizar(veterinario);
        return veterinario;
    }

    private void habilitarTodos(Veterinario veterinario, List<Long> servicoIds) {
        Set<Long> vistos = new HashSet<>();
        for (Long servicoId : servicoIds) {
            if (vistos.add(servicoId)) {
                veterinario.habilitar(buscarServico(servicoId));
            }
        }
    }

    private Servico buscarServico(Long servicoId) {
        return servicoRepositorio.buscarPorId(servicoId)
                .orElseThrow(() -> new NotFoundException("Serviço " + servicoId + " não encontrado"));
    }
}
