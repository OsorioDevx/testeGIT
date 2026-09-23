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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        // Campo -> mensagem. As mensagens são curtas porque aparecem logo abaixo de cada campo.
        Map<Campo, String> erros = new LinkedHashMap<>();

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
            erros.put(Campo.NOME, "Informe o nome completo.");
        } else if (!Validador.nomeValido(nome)) {
            erros.put(Campo.NOME, "Informe nome e sobrenome, usando apenas letras.");
        }

        // ---- CPF ----
        if (cpf.isEmpty()) {
            erros.put(Campo.CPF, "Informe o CPF.");
        } else if (!Validador.cpfValido(cpf)) {
            erros.put(Campo.CPF, "CPF inválido.");
        } else if (pertenceAOutroCliente(repository.buscarPorCpf(cpf), idIgnorado)) {
            erros.put(Campo.CPF, "CPF já cadastrado.");
        }

        // ---- Data de nascimento ----
        LocalDate nascimento = null;
        if (dataTexto.isEmpty()) {
            erros.put(Campo.NASCIMENTO, "Informe a data de nascimento.");
        } else {
            try {
                nascimento = LocalDate.parse(dataTexto, FORMATO_DATA);
                LocalDate hoje = LocalDate.now();
                if (nascimento.isAfter(hoje)) {
                    erros.put(Campo.NASCIMENTO, "A data não pode ser futura.");
                } else if (Period.between(nascimento, hoje).getYears() < IDADE_MINIMA) {
                    erros.put(Campo.NASCIMENTO, "Idade mínima: " + IDADE_MINIMA + " anos.");
                }
            } catch (DateTimeParseException e) {
                erros.put(Campo.NASCIMENTO, "Data inválida.");
            }
        }

        // ---- E-mail ----
        if (email.isEmpty()) {
            erros.put(Campo.EMAIL, "Informe o e-mail.");
        } else if (!Validador.emailValido(email)) {
            erros.put(Campo.EMAIL, "E-mail inválido.");
        } else if (pertenceAOutroCliente(repository.buscarPorEmail(email), idIgnorado)) {
            erros.put(Campo.EMAIL, "E-mail já cadastrado.");
        }

        // ---- Telefone ----
        if (telefone.isEmpty()) {
            erros.put(Campo.TELEFONE, "Informe o telefone.");
        } else if (!Validador.telefoneValido(telefone)) {
            erros.put(Campo.TELEFONE, "Informe DDD + número.");
        }

        // ---- Endereço ----
        if (endereco.isEmpty()) {
            erros.put(Campo.ENDERECO, "Informe o endereço.");
        } else if (endereco.length() < 10) {
            erros.put(Campo.ENDERECO, "Endereço muito curto: inclua rua, número e bairro.");
        }

        // ---- Senha e confirmação ----
        if (senha.isEmpty()) {
            erros.put(Campo.SENHA, "Informe a senha.");
        } else if (!Validador.senhaValida(senha)) {
            erros.put(Campo.SENHA, "Mín. 8, com letras e números.");
        } else if (confirmacao.isEmpty()) {
            erros.put(Campo.CONFIRMACAO_SENHA, "Repita a senha.");
        } else if (!senha.equals(confirmacao)) {
            erros.put(Campo.CONFIRMACAO_SENHA, "As senhas não conferem.");
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
