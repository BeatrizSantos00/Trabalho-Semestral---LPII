package com.petshop.service;

import com.petshop.exception.NotFoundException;
import com.petshop.model.Cachorro;
import com.petshop.model.Gato;
import com.petshop.model.Pelagem;
import com.petshop.model.Pet;
import com.petshop.model.Porte;
import com.petshop.model.TipoPet;
import com.petshop.repository.PetRepository;
import com.petshop.repository.TutorRepository;
import com.petshop.util.Validacao;

import java.time.LocalDate;
import java.util.List;

public class PetService {

    private final PetRepository petRepositorio;
    private final TutorRepository tutorRepositorio;

    public PetService(PetRepository petRepositorio, TutorRepository tutorRepositorio) {
        this.petRepositorio = petRepositorio;
        this.tutorRepositorio = tutorRepositorio;
    }

    /**
     * @param caracteristica porte (se cachorro) ou pelagem (se gato)
     */
    public Pet criar(TipoPet tipo, Long tutorId, String nome, LocalDate dataNascimento,
                     String raca, String caracteristica) {
        garantirTutor(tutorId);
        Pet pet = switch (tipo) {
            case CACHORRO -> new Cachorro(tutorId, nome, dataNascimento, raca,
                    Validacao.converter(Porte.class, caracteristica, "porte"));
            case GATO -> new Gato(tutorId, nome, dataNascimento, raca,
                    Validacao.converter(Pelagem.class, caracteristica, "pelagem"));
        };
        return petRepositorio.salvar(pet);
    }

    public Pet buscar(Long id) {
        return petRepositorio.buscarPorId(id)
                .orElseThrow(() -> new NotFoundException("Pet " + id + " não encontrado"));
    }

    public List<Pet> listar() {
        return petRepositorio.listar();
    }

    public List<Pet> listarPorTutor(Long tutorId) {
        garantirTutor(tutorId);
        return petRepositorio.listarPorTutor(tutorId);
    }

    /** A espécie do pet não muda; a característica é tratada de forma polimórfica. */
    public Pet atualizar(Long id, Long tutorId, String nome, LocalDate dataNascimento,
                         String raca, String caracteristica) {
        Pet pet = buscar(id);
        garantirTutor(tutorId);
        pet.setTutorId(tutorId);
        pet.setNome(nome);
        pet.setDataNascimento(dataNascimento);
        pet.setRaca(raca);
        pet.definirCaracteristica(caracteristica);
        petRepositorio.atualizar(pet);
        return pet;
    }

    public void remover(Long id) {
        buscar(id);
        petRepositorio.remover(id);
    }

    private void garantirTutor(Long tutorId) {
        if (tutorId == null || tutorRepositorio.buscarPorId(tutorId).isEmpty()) {
            throw new NotFoundException("Tutor " + tutorId + " não encontrado");
        }
    }
}
