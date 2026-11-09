package com.petshop.repository;

import com.petshop.model.Servico;
import com.petshop.model.Veterinario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Persiste o veterinário e as suas habilitações (tabela associativa veterinario_servico,
 * relação N..N com Serviço) sempre na mesma transação.
 */
public class VeterinarioRepository extends JdbcRepository implements CrudRepository<Veterinario> {

    @Override
    public Veterinario salvar(Veterinario veterinario) {
        String sql = "INSERT INTO veterinario (nome, crmv, especialidade, criado_em) VALUES (?, ?, ?, ?)";
        try (Connection c = abrirConexao()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, veterinario.getNome());
                ps.setString(2, veterinario.getCrmv());
                ps.setString(3, veterinario.getEspecialidade());
                ps.setObject(4, veterinario.getCriadoEm());
                ps.executeUpdate();
                veterinario.setId(chaveGerada(ps));
                inserirHabilitacoes(c, veterinario);
                c.commit();
                return veterinario;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    @Override
    public Optional<Veterinario> buscarPorId(Long id) {
        try (Connection c = abrirConexao();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM veterinario WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Veterinario veterinario = mapear(rs);
                carregarServicos(c, veterinario);
                return Optional.of(veterinario);
            }
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    @Override
    public List<Veterinario> listar() {
        return consultarLista("SELECT * FROM veterinario ORDER BY id", null);
    }

    /** Veterinários habilitados em um serviço (lado inverso do N..N). */
    public List<Veterinario> listarPorServico(Long servicoId) {
        return consultarLista("SELECT v.* FROM veterinario v JOIN veterinario_servico x "
                + "ON x.veterinario_id = v.id WHERE x.servico_id = ? ORDER BY v.id", servicoId);
    }

    /** Atualiza os dados do veterinário e substitui as suas habilitações, de forma atômica. */
    @Override
    public boolean atualizar(Veterinario veterinario) {
        String sql = "UPDATE veterinario SET nome = ?, crmv = ?, especialidade = ? WHERE id = ?";
        try (Connection c = abrirConexao()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, veterinario.getNome());
                ps.setString(2, veterinario.getCrmv());
                ps.setString(3, veterinario.getEspecialidade());
                ps.setLong(4, veterinario.getId());
                if (ps.executeUpdate() == 0) {
                    c.rollback();
                    return false;
                }
                try (PreparedStatement limpar = c.prepareStatement(
                        "DELETE FROM veterinario_servico WHERE veterinario_id = ?")) {
                    limpar.setLong(1, veterinario.getId());
                    limpar.executeUpdate();
                }
                inserirHabilitacoes(c, veterinario);
                c.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    @Override
    public boolean remover(Long id) {
        try (Connection c = abrirConexao();
             PreparedStatement ps = c.prepareStatement("DELETE FROM veterinario WHERE id = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    private List<Veterinario> consultarLista(String sql, Long parametro) {
        List<Veterinario> veterinarios = new ArrayList<>();
        try (Connection c = abrirConexao(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (parametro != null) {
                ps.setLong(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    veterinarios.add(mapear(rs));
                }
            }
            for (Veterinario veterinario : veterinarios) {
                carregarServicos(c, veterinario);
            }
            return veterinarios;
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    private void inserirHabilitacoes(Connection c, Veterinario veterinario) throws SQLException {
        String sql = "INSERT INTO veterinario_servico (veterinario_id, servico_id) VALUES (?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (Servico servico : veterinario.getServicos()) {
                ps.setLong(1, veterinario.getId());
                ps.setLong(2, servico.getId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void carregarServicos(Connection c, Veterinario veterinario) throws SQLException {
        String sql = "SELECT s.* FROM servico s JOIN veterinario_servico x ON x.servico_id = s.id "
                + "WHERE x.veterinario_id = ? ORDER BY s.id";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, veterinario.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Servico servico = new Servico(rs.getString("nome"), rs.getString("descricao"),
                            rs.getBigDecimal("preco_base"), rs.getInt("duracao_minutos"));
                    servico.setId(rs.getLong("id"));
                    servico.setCriadoEm(rs.getObject("criado_em", LocalDateTime.class));
                    veterinario.habilitar(servico);
                }
            }
        }
    }

    private Veterinario mapear(ResultSet rs) throws SQLException {
        Veterinario veterinario = new Veterinario(rs.getString("nome"), rs.getString("crmv"),
                rs.getString("especialidade"));
        veterinario.setId(rs.getLong("id"));
        veterinario.setCriadoEm(rs.getObject("criado_em", LocalDateTime.class));
        return veterinario;
    }
}
