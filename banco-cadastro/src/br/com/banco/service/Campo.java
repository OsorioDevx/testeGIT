package br.com.banco.service;

/**
 * Identifica cada campo do formulário.
 *
 * O service usa isso para dizer QUAL campo tem erro, e a tela usa
 * para destacar exatamente aquele campo (borda vermelha + mensagem embaixo).
 */
public enum Campo {
    NOME,
    CPF,
    NASCIMENTO,
    EMAIL,
    TELEFONE,
    ENDERECO,
    SENHA,
    CONFIRMACAO_SENHA
}
