package com.petshop.repository;

import com.petshop.model.Atendimento;
import com.petshop.model.ItemAtendimento;
import com.petshop.model.StatusAtendimento;

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
 * Persiste o atendimento e os seus itens (tabela associativa atendimento_servico,
 * relação N..N com Serviço) sempre na mesma transação.
 */
public class AtendimentoRepository extends JdbcRepository implements CrudRepository<Atendimento> {

    @Override
    public Atendimento salvar(Atendimento atendimento) {
        String sql = "INSERT INTO atendimento (pet_id, veterinario_id, data_hora, status, observacoes, "
                + "valor_total, criado_em) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = abrirConexao()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, atendimento.getPetId());
                ps.setLong(2, atendimento.getVeterinarioId());
                ps.setObject(3, atendimento.getDataHora());
                ps.setString(4, atendimento.getStatus().name());
                ps.setString(5, atendimento.getObservacoes());
                ps.setBigDecimal(6, atendimento.getValorTotal());
                ps.setObject(7, atendimento.getCriadoEm());
                ps.executeUpdate();
                atendimento.setId(chaveGerada(ps));
                inserirItens(c, atendimento);
                c.commit();
                return atendimento;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    @Override
    public Optional<Atendimento> buscarPorId(Long id) {
        try (Connection c = abrirConexao();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM atendimento WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Atendimento atendimento = mapear(rs);
                carregarItens(c, atendimento);
                return Optional.of(atendimento);
            }
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    @Override
    public List<Atendimento> listar() {
        return consultarLista("SELECT * FROM atendimento ORDER BY id", null);
    }

    public List<Atendimento> listarPorPet(Long petId) {
        return consultarLista("SELECT * FROM atendimento WHERE pet_id = ? ORDER BY id", petId);
    }

    public List<Atendimento> listarPorVeterinario(Long veterinarioId) {
        return consultarLista("SELECT * FROM atendimento WHERE veterinario_id = ? ORDER BY id",
                veterinarioId);
    }

    /** Atualiza os dados do atendimento e substitui os seus itens, de forma atômica. */
    @Override
    public boolean atualizar(Atendimento atendimento) {
        String sql = "UPDATE atendimento SET veterinario_id = ?, data_hora = ?, status = ?, "
                + "observacoes = ?, valor_total = ? WHERE id = ?";
        try (Connection c = abrirConexao()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setLong(1, atendimento.getVeterinarioId());
                ps.setObject(2, atendimento.getDataHora());
                ps.setString(3, atendimento.getStatus().name());
                ps.setString(4, atendimento.getObservacoes());
                ps.setBigDecimal(5, atendimento.getValorTotal());
                ps.setLong(6, atendimento.getId());
                if (ps.executeUpdate() == 0) {
                    c.rollback();
                    return false;
                }
                try (PreparedStatement limpar = c.prepareStatement(
                        "DELETE FROM atendimento_servico WHERE atendimento_id = ?")) {
                    limpar.setLong(1, atendimento.getId());
                    limpar.executeUpdate();
                }
                inserirItens(c, atendimento);
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
             PreparedStatement ps = c.prepareStatement("DELETE FROM atendimento WHERE id = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    private List<Atendimento> consultarLista(String sql, Long parametro) {
        List<Atendimento> atendimentos = new ArrayList<>();
        try (Connection c = abrirConexao(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (parametro != null) {
                ps.setLong(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    atendimentos.add(mapear(rs));
                }
            }
            for (Atendimento atendimento : atendimentos) {
                carregarItens(c, atendimento);
            }
            return atendimentos;
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    private void inserirItens(Connection c, Atendimento atendimento) throws SQLException {
        String sql = "INSERT INTO atendimento_servico (atendimento_id, servico_id, preco_cobrado) "
                + "VALUES (?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (ItemAtendimento item : atendimento.getItens()) {
                ps.setLong(1, atendimento.getId());
                ps.setLong(2, item.getServicoId());
                ps.setBigDecimal(3, item.getPrecoCobrado());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void carregarItens(Connection c, Atendimento atendimento) throws SQLException {
        String sql = "SELECT s.id, s.nome, x.preco_cobrado FROM atendimento_servico x "
                + "JOIN servico s ON s.id = x.servico_id WHERE x.atendimento_id = ? ORDER BY s.id";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, atendimento.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    atendimento.adicionarItem(new ItemAtendimento(
                            rs.getLong("id"), rs.getString("nome"), rs.getBigDecimal("preco_cobrado")));
                }
            }
        }
    }

    private Atendimento mapear(ResultSet rs) throws SQLException {
        Atendimento atendimento = new Atendimento(rs.getLong("pet_id"), rs.getLong("veterinario_id"),
                rs.getObject("data_hora", LocalDateTime.class), rs.getString("observacoes"));
        atendimento.setStatus(StatusAtendimento.valueOf(rs.getString("status")));
        atendimento.setId(rs.getLong("id"));
        atendimento.setCriadoEm(rs.getObject("criado_em", LocalDateTime.class));
        return atendimento;
    }
}
