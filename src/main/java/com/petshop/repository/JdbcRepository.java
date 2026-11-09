package com.petshop.repository;

import com.petshop.config.Database;
import com.petshop.exception.ConflictException;
import com.petshop.exception.RepositoryException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;

/** Base com utilitários JDBC reaproveitados por todos os repositórios. */
public abstract class JdbcRepository {

    protected Connection abrirConexao() throws SQLException {
        return Database.getConnection();
    }

    protected Long chaveGerada(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (!rs.next()) {
                throw new SQLException("O banco não retornou a chave gerada");
            }
            return rs.getLong(1);
        }
    }

    protected void definirData(PreparedStatement ps, int indice, LocalDate data) throws SQLException {
        if (data == null) {
            ps.setNull(indice, Types.DATE);
        } else {
            ps.setObject(indice, data);
        }
    }

    /** Converte violações de integridade do banco em conflitos de negócio (HTTP 409). */
    protected RuntimeException traduzir(SQLException e) {
        String estado = e.getSQLState();
        if ("23505".equals(estado)) {
            return new ConflictException("Já existe um registro com esses dados (valor duplicado)");
        }
        if ("23503".equals(estado) || "23506".equals(estado)) {
            return new ConflictException(
                    "A operação viola um vínculo com outros registros (chave estrangeira)");
        }
        return new RepositoryException("Erro ao acessar o banco de dados", e);
    }
}
