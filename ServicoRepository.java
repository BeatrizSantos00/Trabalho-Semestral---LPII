package com.petshop.repository;

import com.petshop.model.Servico;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServicoRepository extends JdbcRepository implements CrudRepository<Servico> {

    @Override
    public Servico salvar(Servico servico) {
        String sql = "INSERT INTO servico (nome, descricao, preco_base, duracao_minutos, criado_em) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = abrirConexao();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, servico.getNome());
            ps.setString(2, servico.getDescricao());
            ps.setBigDecimal(3, servico.getPrecoBase());
            ps.setInt(4, servico.getDuracaoMinutos());
            ps.setObject(5, servico.getCriadoEm());
            ps.executeUpdate();
            servico.setId(chaveGerada(ps));
            return servico;
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    @Override
    public Optional<Servico> buscarPorId(Long id) {
        try (Connection c = abrirConexao();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM servico WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    @Override
    public List<Servico> listar() {
        List<Servico> servicos = new ArrayList<>();
        try (Connection c = abrirConexao();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM servico ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                servicos.add(mapear(rs));
            }
            return servicos;
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    @Override
    public boolean atualizar(Servico servico) {
        String sql = "UPDATE servico SET nome = ?, descricao = ?, preco_base = ?, duracao_minutos = ? "
                + "WHERE id = ?";
        try (Connection c = abrirConexao(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, servico.getNome());
            ps.setString(2, servico.getDescricao());
            ps.setBigDecimal(3, servico.getPrecoBase());
            ps.setInt(4, servico.getDuracaoMinutos());
            ps.setLong(5, servico.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    @Override
    public boolean remover(Long id) {
        try (Connection c = abrirConexao();
             PreparedStatement ps = c.prepareStatement("DELETE FROM servico WHERE id = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    private Servico mapear(ResultSet rs) throws SQLException {
        Servico servico = new Servico(rs.getString("nome"), rs.getString("descricao"),
                rs.getBigDecimal("preco_base"), rs.getInt("duracao_minutos"));
        servico.setId(rs.getLong("id"));
        servico.setCriadoEm(rs.getObject("criado_em", LocalDateTime.class));
        return servico;
    }
}
