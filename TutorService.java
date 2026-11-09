package com.petshop.service;

import com.petshop.exception.NotFoundException;
import com.petshop.model.Tutor;
import com.petshop.repository.TutorRepository;

import java.util.List;

public class TutorService {

    private final TutorRepository repositorio;

    public TutorService(TutorRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Tutor criar(String nome, String cpf, String telefone, String email) {
        return repositorio.salvar(new Tutor(nome, cpf, telefone, email));
    }

    public Tutor buscar(Long id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new NotFoundException("Tutor " + id + " não encontrado"));
    }

    public List<Tutor> listar() {
        return repositorio.listar();
    }

    public Tutor atualizar(Long id, String nome, String cpf, String telefone, String email) {
        Tutor tutor = buscar(id);
        tutor.setNome(nome);
        tutor.setCpf(cpf);
        tutor.setTelefone(telefone);
        tutor.setEmail(email);
        repositorio.atualizar(tutor);
        return tutor;
    }

    public void remover(Long id) {
        buscar(id);
        repositorio.remover(id);
    }
}
