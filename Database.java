package com.petshop.config;

import com.petshop.exception.RepositoryException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Conexão com o H2 em modo arquivo (embedded, persistente) e criação do esquema. */
public final class Database {

    private static final String URL =
            System.getProperty("petshop.db.url", "jdbc:h2:file:./data/petshop;DB_CLOSE_DELAY=-1");
    private static final String USUARIO = "sa";
    private static final String SENHA = "";

    private static final String[] ESQUEMA = {
            """
            CREATE TABLE IF NOT EXISTS tutor (
                id        BIGINT AUTO_INCREMENT PRIMARY KEY,
                nome      VARCHAR(120) NOT NULL,
                cpf       VARCHAR(11)  NOT NULL UNIQUE,
                telefone  VARCHAR(20),
                email     VARCHAR(120),
                criado_em TIMESTAMP    NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS pet (
                id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                tutor_id        BIGINT      NOT NULL,
                tipo            VARCHAR(10) NOT NULL,
                nome            VARCHAR(80) NOT NULL,
                data_nascimento DATE,
                raca            VARCHAR(80),
                porte           VARCHAR(10),
                pelagem         VARCHAR(10),
                criado_em       TIMESTAMP   NOT NULL,
                CONSTRAINT fk_pet_tutor FOREIGN KEY (tutor_id) REFERENCES tutor (id) ON DELETE CASCADE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS servico (
                id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                nome            VARCHAR(100)   NOT NULL,
                descricao       VARCHAR(300),
                preco_base      DECIMAL(10, 2) NOT NULL,
                duracao_minutos INT            NOT NULL,
                criado_em       TIMESTAMP      NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS veterinario (
                id            BIGINT AUTO_INCREMENT PRIMARY KEY,
                nome          VARCHAR(120) NOT NULL,
                crmv          VARCHAR(20)  NOT NULL UNIQUE,
                especialidade VARCHAR(100),
                criado_em     TIMESTAMP    NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS veterinario_servico (
                veterinario_id BIGINT NOT NULL,
                servico_id     BIGINT NOT NULL,
                PRIMARY KEY (veterinario_id, servico_id),
                CONSTRAINT fk_vs_veterinario FOREIGN KEY (veterinario_id) REFERENCES veterinario (id) ON DELETE CASCADE,
                CONSTRAINT fk_vs_servico FOREIGN KEY (servico_id) REFERENCES servico (id)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS atendimento (
                id             BIGINT AUTO_INCREMENT PRIMARY KEY,
                pet_id         BIGINT         NOT NULL,
                veterinario_id BIGINT         NOT NULL,
                data_hora      TIMESTAMP      NOT NULL,
                status         VARCHAR(20)    NOT NULL,
                observacoes    VARCHAR(500),
                valor_total    DECIMAL(10, 2) NOT NULL,
                criado_em      TIMESTAMP      NOT NULL,
                CONSTRAINT fk_atendimento_pet FOREIGN KEY (pet_id) REFERENCES pet (id) ON DELETE CASCADE,
                CONSTRAINT fk_atendimento_vet FOREIGN KEY (veterinario_id) REFERENCES veterinario (id)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS atendimento_servico (
                atendimento_id BIGINT         NOT NULL,
                servico_id     BIGINT         NOT NULL,
                preco_cobrado  DECIMAL(10, 2) NOT NULL,
                PRIMARY KEY (atendimento_id, servico_id),
                CONSTRAINT fk_as_atendimento FOREIGN KEY (atendimento_id) REFERENCES atendimento (id) ON DELETE CASCADE,
                CONSTRAINT fk_as_servico FOREIGN KEY (servico_id) REFERENCES servico (id)
            )
            """
    };

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }

    public static String getUrl() {
        return URL;
    }

    /** Cria as tabelas caso ainda não existam. */
    public static void inicializar() {
        try (Connection conexao = getConnection(); Statement st = conexao.createStatement()) {
            for (String ddl : ESQUEMA) {
                st.execute(ddl);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Falha ao inicializar o banco de dados", e);
        }
    }
}
