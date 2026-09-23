package br.com.banco.service;

import java.util.List;

/**
 * Exceção lançada quando algum dado não passa nas regras de negócio.
 * Ela carrega a lista de erros para a tela mostrar todos de uma vez.
 */
public class ValidacaoException extends RuntimeException {

    private final List<String> erros;

    public ValidacaoException(List<String> erros) {
        super(String.join("\n", erros));
        this.erros = List.copyOf(erros);
    }

    public ValidacaoException(String erro) {
        this(List.of(erro));
    }

    public List<String> getErros() {
        return erros;
    }
}
