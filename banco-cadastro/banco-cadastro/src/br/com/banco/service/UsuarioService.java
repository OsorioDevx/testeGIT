package br.com.banco.service;

import br.com.banco.dto.UsuarioDTO;
import br.com.banco.model.Usuario;
import br.com.banco.repository.UsuarioRepository;
import br.com.banco.util.Validador;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Regras de negócio do cadastro.
 *
 * A tela NÃO decide nada: ela só entrega os dados para cá.
 * Aqui validamos, convertemos e pedimos ao repositório para salvar.
 */
public class UsuarioService {

    private static final int IDADE_MINIMA = 18;

    // "uuuu" + STRICT faz o Java recusar datas impossíveis, como 31/02/2000
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public Usuario cadastrar(UsuarioDTO dados) {
        Usuario usuario = validarEConverter(dados, null);
        return repository.salvar(usuario);
    }

    public Usuario editar(int id, UsuarioDTO dados) {
        repository.buscarPorId(id)
                .orElseThrow(() -> new ValidacaoException("Cliente não encontrado. Ele pode ter sido excluído."));

        Usuario atualizado = validarEConverter(dados, id);
        atualizado.setId(id); // mantém o mesmo id -> o repositório atualiza em vez de inserir
        return repository.salvar(atualizado);
    }

    public void excluir(int id) {
        if (!repository.remover(id)) {
            throw new ValidacaoException("Cliente não encontrado. Ele pode já ter sido excluído.");
        }
    }

    public List<Usuario> listar() {
        return repository.listarTodos();
    }

    public Optional<Usuario> buscarPorId(int id) {
        return repository.buscarPorId(id);
    }

    /**
     * Valida todos os campos e, se estiver tudo certo, cria o objeto Usuario.
     *
     * @param idIgnorado id do próprio cliente durante uma edição (para ele não
     *                   "conflitar" consigo mesmo na checagem de CPF/e-mail repetido).
     *                   No cadastro é null.
     */
    private Usuario validarEConverter(UsuarioDTO d, Integer idIgnorado) {
        List<String> erros = new ArrayList<>();

        // Normaliza os textos antes de validar
        String nome = texto(d.nomeCompleto()).replaceAll("\\s+", " ");
        String cpf = Validador.somenteDigitos(d.cpf());
        String dataTexto = texto(d.dataNascimento());
        String email = texto(d.email()).toLowerCase();
        String telefone = Validador.somenteDigitos(d.telefone());
        String endereco = texto(d.endereco()).replaceAll("\\s+", " ");
        String senha = d.senha() == null ? "" : d.senha();
        String confirmacao = d.confirmacaoSenha() == null ? "" : d.confirmacaoSenha();

        // ---- Nome ----
        if (nome.isEmpty()) {
            erros.add("Nome completo é obrigatório.");
        } else if (!Validador.nomeValido(nome)) {
            erros.add("Informe nome e sobrenome, usando apenas letras.");
        }

        // ---- CPF ----
        if (cpf.isEmpty()) {
            erros.add("CPF é obrigatório.");
        } else if (!Validador.cpfValido(cpf)) {
            erros.add("CPF inválido. Confira os números digitados.");
        } else if (pertenceAOutroCliente(repository.buscarPorCpf(cpf), idIgnorado)) {
            erros.add("Já existe um cliente cadastrado com este CPF.");
        }

        // ---- Data de nascimento ----
        LocalDate nascimento = null;
        if (dataTexto.isEmpty()) {
            erros.add("Data de nascimento é obrigatória.");
        } else {
            try {
                nascimento = LocalDate.parse(dataTexto, FORMATO_DATA);
                LocalDate hoje = LocalDate.now();
                if (nascimento.isAfter(hoje)) {
                    erros.add("A data de nascimento não pode estar no futuro.");
                } else if (Period.between(nascimento, hoje).getYears() < IDADE_MINIMA) {
                    erros.add("O cliente precisa ter pelo menos " + IDADE_MINIMA + " anos.");
                }
            } catch (DateTimeParseException e) {
                erros.add("Data de nascimento inválida. Use o formato dd/mm/aaaa (ex.: 15/03/1995).");
            }
        }

        // ---- E-mail ----
        if (email.isEmpty()) {
            erros.add("E-mail é obrigatório.");
        } else if (!Validador.emailValido(email)) {
            erros.add("E-mail inválido. Exemplo de formato correto: nome@dominio.com");
        } else if (pertenceAOutroCliente(repository.buscarPorEmail(email), idIgnorado)) {
            erros.add("Já existe um cliente cadastrado com este e-mail.");
        }

        // ---- Telefone ----
        if (telefone.isEmpty()) {
            erros.add("Telefone é obrigatório.");
        } else if (!Validador.telefoneValido(telefone)) {
            erros.add("Telefone inválido. Informe DDD + número (10 ou 11 dígitos).");
        }

        // ---- Endereço ----
        if (endereco.isEmpty()) {
            erros.add("Endereço é obrigatório.");
        } else if (endereco.length() < 10) {
            erros.add("Endereço muito curto. Informe rua, número e bairro.");
        }

        // ---- Senha ----
        if (senha.isEmpty()) {
            erros.add("Senha é obrigatória.");
        } else if (!Validador.senhaValida(senha)) {
            erros.add("A senha deve ter no mínimo 8 caracteres, com pelo menos uma letra e um número, e sem espaços.");
        } else if (!senha.equals(confirmacao)) {
            erros.add("A confirmação de senha não confere com a senha digitada.");
        }

        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }

        return new Usuario(nome, cpf, nascimento, email, telefone, endereco, senha);
    }

    /** true se encontrou alguém E esse alguém não é o próprio cliente que está sendo editado. */
    private boolean pertenceAOutroCliente(Optional<Usuario> encontrado, Integer idIgnorado) {
        return encontrado.isPresent() && !encontrado.get().getId().equals(idIgnorado);
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
