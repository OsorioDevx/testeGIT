package br.com.banco.repository;

import br.com.banco.model.Usuario;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Camada de armazenamento.
 *
 * Por enquanto os dados ficam em memória (um Map). Ao fechar o programa, tudo é perdido.
 * Se um dia você trocar por um banco de dados (JDBC, JPA...), só esta classe muda:
 * a tela e as regras de negócio continuam iguais. Essa é a vantagem de separar as camadas.
 */
public class UsuarioRepository {

    // LinkedHashMap mantém a ordem de inserção (a lista aparece na ordem de cadastro)
    private final Map<Integer, Usuario> usuarios = new LinkedHashMap<>();
    private int proximoId = 1;

    /** Insere (se não tem id) ou atualiza (se já tem id). */
    public Usuario salvar(Usuario usuario) {
        if (usuario.getId() == null) {
            usuario.setId(proximoId++);
        }
        usuarios.put(usuario.getId(), usuario);
        return usuario;
    }

    public Optional<Usuario> buscarPorId(int id) {
        return Optional.ofNullable(usuarios.get(id));
    }

    public Optional<Usuario> buscarPorCpf(String cpf) {
        return usuarios.values().stream()
                .filter(u -> u.getCpf().equals(cpf))
                .findFirst();
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarios.values().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    /** Retorna uma cópia da lista, para ninguém alterar o armazenamento "por fora". */
    public List<Usuario> listarTodos() {
        return new ArrayList<>(usuarios.values());
    }

    /** Retorna true se removeu, false se o id não existia. */
    public boolean remover(int id) {
        return usuarios.remove(id) != null;
    }
}
