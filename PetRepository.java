package com.petshop.repository;

import com.petshop.model.Cachorro;
import com.petshop.model.Gato;
import com.petshop.model.Pelagem;
import com.petshop.model.Pet;
import com.petshop.model.Porte;
import com.petshop.model.TipoPet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Persiste cachorros e gatos em uma única tabela (coluna "tipo" como discriminador). */
public class PetRepository extends JdbcRepository implements CrudRepository<Pet> {

    @Override
    public Pet salvar(Pet pet) {
        String sql = "INSERT INTO pet (tutor_id, tipo, nome, data_nascimento, raca, porte, pelagem, criado_em) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = abrirConexao();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, pet.getTutorId());
            ps.setString(2, pet.getTipo().name());
            ps.setString(3, pet.getNome());
            definirData(ps, 4, pet.getDataNascimento());
            ps.setString(5, pet.getRaca());
            definirCaracteristicas(ps, 6, pet);
            ps.setObject(8, pet.getCriadoEm());
            ps.executeUpdate();
            pet.setId(chaveGerada(ps));
            return pet;
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    @Override
    public Optional<Pet> buscarPorId(Long id) {
        try (Connection c = abrirConexao();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM pet WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    @Override
    public List<Pet> listar() {
        return consultarLista("SELECT * FROM pet ORDER BY id", null);
    }

    public List<Pet> listarPorTutor(Long tutorId) {
        return consultarLista("SELECT * FROM pet WHERE tutor_id = ? ORDER BY id", tutorId);
    }

    @Override
    public boolean atualizar(Pet pet) {
        String sql = "UPDATE pet SET tutor_id = ?, nome = ?, data_nascimento = ?, raca = ?, "
                + "porte = ?, pelagem = ? WHERE id = ?";
        try (Connection c = abrirConexao(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, pet.getTutorId());
            ps.setString(2, pet.getNome());
            definirData(ps, 3, pet.getDataNascimento());
            ps.setString(4, pet.getRaca());
            definirCaracteristicas(ps, 5, pet);
            ps.setLong(7, pet.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    @Override
    public boolean remover(Long id) {
        try (Connection c = abrirConexao();
             PreparedStatement ps = c.prepareStatement("DELETE FROM pet WHERE id = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    private List<Pet> consultarLista(String sql, Long parametro) {
        List<Pet> pets = new ArrayList<>();
        try (Connection c = abrirConexao(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (parametro != null) {
                ps.setLong(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pets.add(mapear(rs));
                }
            }
            return pets;
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    /** Preenche as colunas porte (indice) e pelagem (indice + 1), conforme a espécie. */
    private void definirCaracteristicas(PreparedStatement ps, int indice, Pet pet) throws SQLException {
        boolean cachorro = pet.getTipo() == TipoPet.CACHORRO;
        ps.setString(indice, cachorro ? pet.getCaracteristica() : null);
        ps.setString(indice + 1, cachorro ? null : pet.getCaracteristica());
    }

    private Pet mapear(ResultSet rs) throws SQLException {
        TipoPet tipo = TipoPet.valueOf(rs.getString("tipo"));
        Long tutorId = rs.getLong("tutor_id");
        String nome = rs.getString("nome");
        LocalDate nascimento = rs.getObject("data_nascimento", LocalDate.class);
        String raca = rs.getString("raca");

        Pet pet = switch (tipo) {
            case CACHORRO -> new Cachorro(tutorId, nome, nascimento, raca,
                    Porte.valueOf(rs.getString("porte")));
            case GATO -> new Gato(tutorId, nome, nascimento, raca,
                    Pelagem.valueOf(rs.getString("pelagem")));
        };
        pet.setId(rs.getLong("id"));
        pet.setCriadoEm(rs.getObject("criado_em", LocalDateTime.class));
        return pet;
    }
}
