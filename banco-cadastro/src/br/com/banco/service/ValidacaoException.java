package br.com.banco.service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Exceção lançada quando algum dado não passa nas regras de negócio.
 *
 * Pode carregar:
 * - erros por campo (ex.: CPF -> "CPF inválido."), usados para destacar cada campo na tela;
 * - ou uma mensagem geral (ex.: "Cliente não encontrado."), sem campo específico.
 */
public class ValidacaoException extends RuntimeException {

    private final Map<Campo, String> errosPorCampo;

    public ValidacaoException(Map<Campo, String> errosPorCampo) {
        super(String.join("\n", errosPorCampo.values()));
        // LinkedHashMap mantém a ordem em que os erros foram encontrados (a ordem do formulário)
        this.errosPorCampo = Collections.unmodifiableMap(new LinkedHashMap<>(errosPorCampo));
    }

    public ValidacaoException(String mensagemGeral) {
        super(mensagemGeral);
        this.errosPorCampo = Map.of();
    }

    public Map<Campo, String> getErrosPorCampo() {
        return errosPorCampo;
    }

    public boolean temErrosDeCampo() {
        return !errosPorCampo.isEmpty();
    }
}
