package com.petshop.repository;

import com.petshop.model.Tutor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TutorRepository extends JdbcRepository implements CrudRepository<Tutor> {

    @Override
    public Tutor salvar(Tutor tutor) {
        String sql = "INSERT INTO tutor (nome, cpf, telefone, email, criado_em) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = abrirConexao();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tutor.getNome());
            ps.setString(2, tutor.getCpf());
            ps.setString(3, tutor.getTelefone());
            ps.setString(4, tutor.getEmail());
            ps.setObject(5, tutor.getCriadoEm());
            ps.executeUpdate();
            tutor.setId(chaveGerada(ps));
            return tutor;
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    @Override
    public Optional<Tutor> buscarPorId(Long id) {
        try (Connection c = abrirConexao();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM tutor WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    @Override
    public List<Tutor> listar() {
        List<Tutor> tutores = new ArrayList<>();
        try (Connection c = abrirConexao();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM tutor ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tutores.add(mapear(rs));
            }
            return tutores;
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    @Override
    public boolean atualizar(Tutor tutor) {
        String sql = "UPDATE tutor SET nome = ?, cpf = ?, telefone = ?, email = ? WHERE id = ?";
        try (Connection c = abrirConexao(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, tutor.getNome());
            ps.setString(2, tutor.getCpf());
            ps.setString(3, tutor.getTelefone());
            ps.setString(4, tutor.getEmail());
            ps.setLong(5, tutor.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    @Override
    public boolean remover(Long id) {
        try (Connection c = abrirConexao();
             PreparedStatement ps = c.prepareStatement("DELETE FROM tutor WHERE id = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    private Tutor mapear(ResultSet rs) throws SQLException {
        Tutor tutor = new Tutor(rs.getString("nome"), rs.getString("cpf"),
                rs.getString("telefone"), rs.getString("email"));
        tutor.setId(rs.getLong("id"));
        tutor.setCriadoEm(rs.getObject("criado_em", LocalDateTime.class));
        return tutor;
    }
}
