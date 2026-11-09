package com.petshop.controller;

import com.petshop.http.Controller;
import com.petshop.http.JsonBody;
import com.petshop.http.Router;
import com.petshop.model.Tutor;
import com.petshop.service.PetService;
import com.petshop.service.TutorService;

public class TutorController implements Controller {

    private final TutorService tutorService;
    private final PetService petService;

    public TutorController(TutorService tutorService, PetService petService) {
        this.tutorService = tutorService;
        this.petService = petService;
    }

    @Override
    public void registrar(Router roteador) {
        roteador.get("/tutores", ctx -> ctx.json(200, tutorService.listar()));

        roteador.get("/tutores/{id}", ctx ->
                ctx.json(200, tutorService.buscar(ctx.parametroId("id"))));

        roteador.post("/tutores", ctx -> {
            JsonBody corpo = ctx.corpo();
            Tutor tutor = tutorService.criar(corpo.texto("nome"), corpo.texto("cpf"),
                    corpo.texto("telefone"), corpo.texto("email"));
            ctx.criado("/tutores/" + tutor.getId(), tutor);
        });

        roteador.put("/tutores/{id}", ctx -> {
            JsonBody corpo = ctx.corpo();
            ctx.json(200, tutorService.atualizar(ctx.parametroId("id"), corpo.texto("nome"),
                    corpo.texto("cpf"), corpo.texto("telefone"), corpo.texto("email")));
        });

        roteador.delete("/tutores/{id}", ctx -> {
            tutorService.remover(ctx.parametroId("id"));
            ctx.semConteudo();
        });

        // Relacionamento 1..N: pets de um tutor
        roteador.get("/tutores/{id}/pets", ctx ->
                ctx.json(200, petService.listarPorTutor(ctx.parametroId("id"))));
    }
}
