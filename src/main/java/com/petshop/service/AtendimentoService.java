package com.petshop.service;

import com.petshop.exception.ConflictException;
import com.petshop.exception.NotFoundException;
import com.petshop.model.Atendimento;
import com.petshop.model.ItemAtendimento;
import com.petshop.model.Pet;
import com.petshop.model.Servico;
import com.petshop.model.StatusAtendimento;
import com.petshop.model.Veterinario;
import com.petshop.repository.AtendimentoRepository;
import com.petshop.repository.PetRepository;
import com.petshop.repository.ServicoRepository;
import com.petshop.repository.VeterinarioRepository;

import java.time.LocalDateTime;
import java.util.List;

public class AtendimentoService {

    private final AtendimentoRepository atendimentoRepositorio;
    private final PetRepository petRepositorio;
    private final ServicoRepository servicoRepositorio;
    private final VeterinarioRepository veterinarioRepositorio;

    public AtendimentoService(AtendimentoRepository atendimentoRepositorio,
                              PetRepository petRepositorio,
                              ServicoRepository servicoRepositorio,
                              VeterinarioRepository veterinarioRepositorio) {
        this.atendimentoRepositorio = atendimentoRepositorio;
        this.petRepositorio = petRepositorio;
        this.servicoRepositorio = servicoRepositorio;
        this.veterinarioRepositorio = veterinarioRepositorio;
    }

    public Atendimento criar(Long petId, Long veterinarioId, LocalDateTime dataHora,
                             String observacoes, List<Long> servicoIds) {
        Pet pet = buscarPet(petId);
        Veterinario veterinario = buscarVeterinario(veterinarioId);
        Atendimento atendimento = new Atendimento(petId, veterinarioId, dataHora, observacoes);
        for (Long servicoId : servicoIds) {
            Servico servico = buscarServico(servicoId);
            exigirHabilitacao(veterinario, servico);
            atendimento.adicionarItem(precificar(pet, servico));
        }
        return atendimentoRepositorio.salvar(atendimento);
    }

    public Atendimento buscar(Long id) {
        return atendimentoRepositorio.buscarPorId(id)
                .orElseThrow(() -> new NotFoundException("Atendimento " + id + " não encontrado"));
    }

    public List<Atendimento> listar() {
        return atendimentoRepositorio.listar();
    }

    public List<Atendimento> listarPorPet(Long petId) {
        buscarPet(petId);
        return atendimentoRepositorio.listarPorPet(petId);
    }

    public List<Atendimento> listarPorVeterinario(Long veterinarioId) {
        buscarVeterinario(veterinarioId);
        return atendimentoRepositorio.listarPorVeterinario(veterinarioId);
    }

    /** Se "veterinarioId" for nulo, o veterinário atual é mantido. */
    public Atendimento atualizar(Long id, Long veterinarioId, LocalDateTime dataHora,
                                 StatusAtendimento status, String observacoes) {
        Atendimento atendimento = buscar(id);
        if (veterinarioId != null && !veterinarioId.equals(atendimento.getVeterinarioId())) {
            Veterinario novo = buscarVeterinario(veterinarioId);
            for (ItemAtendimento item : atendimento.getItens()) {
                exigirHabilitacao(novo, buscarServico(item.getServicoId()));
            }
            atendimento.setVeterinarioId(veterinarioId);
        }
        atendimento.setDataHora(dataHora);
        atendimento.setStatus(status);
        atendimento.setObservacoes(observacoes);
        atendimentoRepositorio.atualizar(atendimento);
        return atendimento;
    }

    public void remover(Long id) {
        buscar(id);
        atendimentoRepositorio.remover(id);
    }

    public Atendimento adicionarServico(Long atendimentoId, Long servicoId) {
        Atendimento atendimento = buscarEditavel(atendimentoId);
        Pet pet = buscarPet(atendimento.getPetId());
        Servico servico = buscarServico(servicoId);
        exigirHabilitacao(buscarVeterinario(atendimento.getVeterinarioId()), servico);
        atendimento.adicionarItem(precificar(pet, servico));
        atendimentoRepositorio.atualizar(atendimento);
        return atendimento;
    }

    public Atendimento removerServico(Long atendimentoId, Long servicoId) {
        Atendimento atendimento = buscarEditavel(atendimentoId);
        if (!atendimento.removerItem(servicoId)) {
            throw new NotFoundException(
                    "O serviço " + servicoId + " não faz parte do atendimento " + atendimentoId);
        }
        atendimentoRepositorio.atualizar(atendimento);
        return atendimento;
    }

    /**
     * Polimorfismo em ação: o serviço não sabe se o pet é cachorro ou gato,
     * apenas pede o adicional e cada espécie aplica a sua regra.
     */
    private ItemAtendimento precificar(Pet pet, Servico servico) {
        return new ItemAtendimento(servico.getId(), servico.getNome(),
                servico.getPrecoBase().add(pet.calcularAdicional()));
    }

    /** Regra de negócio do N..N Veterinário ↔ Serviço: só realiza o que está habilitado a fazer. */
    private void exigirHabilitacao(Veterinario veterinario, Servico servico) {
        if (!veterinario.estaHabilitadoEm(servico.getId())) {
            throw new ConflictException("O veterinário " + veterinario.getId()
                    + " não está habilitado no serviço " + servico.getId() + " (" + servico.getNome() + ")");
        }
    }

    private Atendimento buscarEditavel(Long atendimentoId) {
        Atendimento atendimento = buscar(atendimentoId);
        if (!atendimento.getStatus().permiteAlterarServicos()) {
            throw new ConflictException("Atendimento " + atendimento.getStatus()
                    + " não permite alterar serviços");
        }
        return atendimento;
    }

    private Pet buscarPet(Long petId) {
        return petRepositorio.buscarPorId(petId)
                .orElseThrow(() -> new NotFoundException("Pet " + petId + " não encontrado"));
    }

    private Servico buscarServico(Long servicoId) {
        return servicoRepositorio.buscarPorId(servicoId)
                .orElseThrow(() -> new NotFoundException("Serviço " + servicoId + " não encontrado"));
    }

    private Veterinario buscarVeterinario(Long veterinarioId) {
        if (veterinarioId == null) {
            throw new com.petshop.exception.ValidationException("O campo 'veterinarioId' é obrigatório");
        }
        return veterinarioRepositorio.buscarPorId(veterinarioId)
                .orElseThrow(() -> new NotFoundException("Veterinário " + veterinarioId + " não encontrado"));
    }
}
