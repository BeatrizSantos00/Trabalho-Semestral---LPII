package com.petshop.controller;

import com.petshop.exception.ValidationException;
import com.petshop.http.Controller;
import com.petshop.http.JsonBody;
import com.petshop.http.Router;
import com.petshop.model.Atendimento;
import com.petshop.model.StatusAtendimento;
import com.petshop.service.AtendimentoService;
import com.petshop.util.Validacao;

public class AtendimentoController implements Controller {

    private final AtendimentoService atendimentoService;

    public AtendimentoController(AtendimentoService atendimentoService) {
        this.atendimentoService = atendimentoService;
    }

    @Override
    public void registrar(Router roteador) {
        roteador.get("/atendimentos", ctx -> ctx.json(200, atendimentoService.listar()));

        roteador.get("/atendimentos/{id}", ctx ->
                ctx.json(200, atendimentoService.buscar(ctx.parametroId("id"))));

        roteador.post("/atendimentos", ctx -> {
            JsonBody corpo = ctx.corpo();
            Atendimento atendimento = atendimentoService.criar(corpo.inteiroLongo("petId"),
                    corpo.inteiroLongo("veterinarioId"), corpo.dataHora("dataHora"),
                    corpo.texto("observacoes"), corpo.listaInteiros("servicoIds"));
            ctx.criado("/atendimentos/" + atendimento.getId(), atendimento);
        });

        roteador.put("/atendimentos/{id}", ctx -> {
            JsonBody corpo = ctx.corpo();
            StatusAtendimento status =
                    Validacao.converter(StatusAtendimento.class, corpo.texto("status"), "status");
            ctx.json(200, atendimentoService.atualizar(ctx.parametroId("id"),
                    corpo.inteiroLongo("veterinarioId"), corpo.dataHora("dataHora"), status,
                    corpo.texto("observacoes")));
        });

        roteador.delete("/atendimentos/{id}", ctx -> {
            atendimentoService.remover(ctx.parametroId("id"));
            ctx.semConteudo();
        });

        // Relacionamento N..N: serviços de um atendimento
        roteador.post("/atendimentos/{id}/servicos", ctx -> {
            Long servicoId = ctx.corpo().inteiroLongo("servicoId");
            if (servicoId == null) {
                throw new ValidationException("O campo 'servicoId' é obrigatório");
            }
            ctx.criado("/atendimentos/" + ctx.parametroId("id"),
                    atendimentoService.adicionarServico(ctx.parametroId("id"), servicoId));
        });

        roteador.delete("/atendimentos/{id}/servicos/{servicoId}", ctx ->
                ctx.json(200, atendimentoService.removerServico(
                        ctx.parametroId("id"), ctx.parametroId("servicoId"))));
    }
}
