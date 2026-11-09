package com.petshop.controller;

import com.petshop.exception.ValidationException;
import com.petshop.http.Controller;
import com.petshop.http.JsonBody;
import com.petshop.http.Router;
import com.petshop.model.Pet;
import com.petshop.model.TipoPet;
import com.petshop.service.AtendimentoService;
import com.petshop.service.PetService;
import com.petshop.util.Validacao;

public class PetController implements Controller {

    private final PetService petService;
    private final AtendimentoService atendimentoService;

    public PetController(PetService petService, AtendimentoService atendimentoService) {
        this.petService = petService;
        this.atendimentoService = atendimentoService;
    }

    @Override
    public void registrar(Router roteador) {
        roteador.get("/pets", ctx -> ctx.json(200, petService.listar()));

        roteador.get("/pets/{id}", ctx -> ctx.json(200, petService.buscar(ctx.parametroId("id"))));

        roteador.post("/pets", ctx -> {
            JsonBody corpo = ctx.corpo();
            TipoPet tipo = Validacao.converter(TipoPet.class, corpo.texto("tipo"), "tipo");
            Pet pet = petService.criar(tipo, corpo.inteiroLongo("tutorId"), corpo.texto("nome"),
                    corpo.data("dataNascimento"), corpo.texto("raca"),
                    caracteristica(tipo, corpo));
            ctx.criado("/pets/" + pet.getId(), pet);
        });

        roteador.put("/pets/{id}", ctx -> {
            Long id = ctx.parametroId("id");
            JsonBody corpo = ctx.corpo();
            Pet atual = petService.buscar(id);
            if (corpo.texto("tipo") != null
                    && Validacao.converter(TipoPet.class, corpo.texto("tipo"), "tipo") != atual.getTipo()) {
                throw new ValidationException("A espécie (tipo) de um pet não pode ser alterada");
            }
            ctx.json(200, petService.atualizar(id, corpo.inteiroLongo("tutorId"), corpo.texto("nome"),
                    corpo.data("dataNascimento"), corpo.texto("raca"),
                    caracteristica(atual.getTipo(), corpo)));
        });

        roteador.delete("/pets/{id}", ctx -> {
            petService.remover(ctx.parametroId("id"));
            ctx.semConteudo();
        });

        // Relacionamento 1..N: atendimentos de um pet
        roteador.get("/pets/{id}/atendimentos", ctx ->
                ctx.json(200, atendimentoService.listarPorPet(ctx.parametroId("id"))));
    }

    /** Cachorro informa "porte"; gato informa "pelagem". */
    private String caracteristica(TipoPet tipo, JsonBody corpo) {
        return tipo == TipoPet.CACHORRO ? corpo.texto("porte") : corpo.texto("pelagem");
    }
}
