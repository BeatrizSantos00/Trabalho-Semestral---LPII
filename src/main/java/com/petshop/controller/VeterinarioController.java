package com.petshop.controller;

import com.petshop.exception.ValidationException;
import com.petshop.http.Controller;
import com.petshop.http.JsonBody;
import com.petshop.http.Router;
import com.petshop.model.Veterinario;
import com.petshop.service.AtendimentoService;
import com.petshop.service.VeterinarioService;

public class VeterinarioController implements Controller {

    private final VeterinarioService veterinarioService;
    private final AtendimentoService atendimentoService;

    public VeterinarioController(VeterinarioService veterinarioService,
                                 AtendimentoService atendimentoService) {
        this.veterinarioService = veterinarioService;
        this.atendimentoService = atendimentoService;
    }

    @Override
    public void registrar(Router roteador) {
        roteador.get("/veterinarios", ctx -> ctx.json(200, veterinarioService.listar()));

        roteador.get("/veterinarios/{id}", ctx ->
                ctx.json(200, veterinarioService.buscar(ctx.parametroId("id"))));

        roteador.post("/veterinarios", ctx -> {
            JsonBody corpo = ctx.corpo();
            Veterinario veterinario = veterinarioService.criar(corpo.texto("nome"), corpo.texto("crmv"),
                    corpo.texto("especialidade"), corpo.listaInteiros("servicoIds"));
            ctx.criado("/veterinarios/" + veterinario.getId(), veterinario);
        });

        roteador.put("/veterinarios/{id}", ctx -> {
            JsonBody corpo = ctx.corpo();
            ctx.json(200, veterinarioService.atualizar(ctx.parametroId("id"), corpo.texto("nome"),
                    corpo.texto("crmv"), corpo.texto("especialidade"),
                    corpo.contem("servicoIds") ? corpo.listaInteiros("servicoIds") : null));
        });

        roteador.delete("/veterinarios/{id}", ctx -> {
            veterinarioService.remover(ctx.parametroId("id"));
            ctx.semConteudo();
        });

        // Relacionamento 1..N: atendimentos de um veterinário
        roteador.get("/veterinarios/{id}/atendimentos", ctx ->
                ctx.json(200, atendimentoService.listarPorVeterinario(ctx.parametroId("id"))));

        // Relacionamento N..N: serviços em que o veterinário está habilitado
        roteador.post("/veterinarios/{id}/servicos", ctx -> {
            Long servicoId = ctx.corpo().inteiroLongo("servicoId");
            if (servicoId == null) {
                throw new ValidationException("O campo 'servicoId' é obrigatório");
            }
            ctx.criado("/veterinarios/" + ctx.parametroId("id"),
                    veterinarioService.habilitarServico(ctx.parametroId("id"), servicoId));
        });

        roteador.delete("/veterinarios/{id}/servicos/{servicoId}", ctx ->
                ctx.json(200, veterinarioService.desabilitarServico(
                        ctx.parametroId("id"), ctx.parametroId("servicoId"))));
    }
}
