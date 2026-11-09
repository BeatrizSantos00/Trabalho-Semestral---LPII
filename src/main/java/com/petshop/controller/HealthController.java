package com.petshop.controller;

import com.petshop.http.Controller;
import com.petshop.http.Router;

import java.util.Map;

public class HealthController implements Controller {

    @Override
    public void registrar(Router roteador) {
        roteador.get("/health", ctx -> ctx.json(200, Map.of("status", "ok")));
    }
}
