package com.petshop;

import com.petshop.config.Database;
import com.petshop.controller.AtendimentoController;
import com.petshop.controller.HealthController;
import com.petshop.controller.PetController;
import com.petshop.controller.ServicoController;
import com.petshop.controller.TutorController;
import com.petshop.controller.VeterinarioController;
import com.petshop.http.Controller;
import com.petshop.http.Router;
import com.petshop.repository.AtendimentoRepository;
import com.petshop.repository.PetRepository;
import com.petshop.repository.ServicoRepository;
import com.petshop.repository.TutorRepository;
import com.petshop.repository.VeterinarioRepository;
import com.petshop.service.AtendimentoService;
import com.petshop.service.PetService;
import com.petshop.service.ServicoService;
import com.petshop.service.TutorService;
import com.petshop.service.VeterinarioService;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws IOException {
        int porta = Integer.parseInt(
                System.getProperty("petshop.port", System.getenv().getOrDefault("PORT", "8080")));

        Database.inicializar();

        // Composição manual das dependências (injeção via construtor, sem framework)
        TutorRepository tutorRepositorio = new TutorRepository();
        PetRepository petRepositorio = new PetRepository();
        ServicoRepository servicoRepositorio = new ServicoRepository();
        AtendimentoRepository atendimentoRepositorio = new AtendimentoRepository();
        VeterinarioRepository veterinarioRepositorio = new VeterinarioRepository();

        TutorService tutorService = new TutorService(tutorRepositorio);
        PetService petService = new PetService(petRepositorio, tutorRepositorio);
        ServicoService servicoService = new ServicoService(servicoRepositorio);
        VeterinarioService veterinarioService =
                new VeterinarioService(veterinarioRepositorio, servicoRepositorio);
        AtendimentoService atendimentoService = new AtendimentoService(atendimentoRepositorio,
                petRepositorio, servicoRepositorio, veterinarioRepositorio);

        List<Controller> controllers = List.of(
                new HealthController(),
                new TutorController(tutorService, petService),
                new PetController(petService, atendimentoService),
                new ServicoController(servicoService, veterinarioService),
                new VeterinarioController(veterinarioService, atendimentoService),
                new AtendimentoController(atendimentoService));

        Router roteador = new Router();
        controllers.forEach(controller -> controller.registrar(roteador));

        HttpServer servidor = HttpServer.create(new InetSocketAddress(porta), 0);
        servidor.createContext("/", roteador);
        servidor.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        servidor.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> servidor.stop(0)));

        System.out.println("===========================================");
        System.out.println("Pet Shop API iniciada com sucesso");
        System.out.println("Endereco: http://localhost:" + porta);
        System.out.println("Banco:    " + Database.getUrl());
        System.out.println("Ctrl+C para encerrar");
        System.out.println("===========================================");
    }
}
