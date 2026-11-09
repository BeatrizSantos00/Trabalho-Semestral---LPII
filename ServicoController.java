package com.petshop.controller;

import com.petshop.http.Controller;
import com.petshop.http.JsonBody;
import com.petshop.http.Router;
import com.petshop.model.Servico;
import com.petshop.service.ServicoService;
import com.petshop.service.VeterinarioService;

public class ServicoController implements Controller {

    private final ServicoService servicoService;
    private final VeterinarioService veterinarioService;

    public ServicoController(ServicoService servicoService, VeterinarioService veterinarioService) {
        this.servicoService = servicoService;
        this.veterinarioService = veterinarioService;
    }

    @Override
    public void registrar(Router roteador) {
        roteador.get("/servicos", ctx -> ctx.json(200, servicoService.listar()));

        roteador.get("/servicos/{id}", ctx ->
                ctx.json(200, servicoService.buscar(ctx.parametroId("id"))));

        roteador.post("/servicos", ctx -> {
            JsonBody corpo = ctx.corpo();
            Servico servico = servicoService.criar(corpo.texto("nome"), corpo.texto("descricao"),
                    corpo.decimal("precoBase"), corpo.inteiro("duracaoMinutos"));
            ctx.criado("/servicos/" + servico.getId(), servico);
        });

        roteador.put("/servicos/{id}", ctx -> {
            JsonBody corpo = ctx.corpo();
            ctx.json(200, servicoService.atualizar(ctx.parametroId("id"), corpo.texto("nome"),
                    corpo.texto("descricao"), corpo.decimal("precoBase"),
                    corpo.inteiro("duracaoMinutos")));
        });

        roteador.delete("/servicos/{id}", ctx -> {
            servicoService.remover(ctx.parametroId("id"));
            ctx.semConteudo();
        });

        // Relacionamento N..N (lado inverso): veterinários habilitados no serviço
        roteador.get("/servicos/{id}/veterinarios", ctx ->
                ctx.json(200, veterinarioService.listarPorServico(ctx.parametroId("id"))));
    }
}
