package br.com.banco.dto;

/**
 * DTO (Data Transfer Object): carrega os dados "crus" digitados no formulário
 * até a camada de serviço, onde serão validados e convertidos em Usuario.
 *
 * Tudo é String porque é assim que o texto sai dos campos da tela.
 * Um "record" é uma classe imutável e enxuta (Java 16+).
 */
public record UsuarioDTO(
        String nomeCompleto,
        String cpf,
        String dataNascimento,
        String email,
        String telefone,
        String endereco,
        String senha,
        String confirmacaoSenha
) {
}
