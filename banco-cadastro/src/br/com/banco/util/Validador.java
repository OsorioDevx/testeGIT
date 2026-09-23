package br.com.banco.util;

import java.util.regex.Pattern;

/**
 * Funções de validação reutilizáveis.
 * São "static" porque não precisam guardar estado nenhum.
 */
public final class Validador {

    // Algo como: texto@texto.texto  (simples de propósito, para fins didáticos)
    private static final Pattern EMAIL =
            Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    // Pelo menos duas palavras só com letras (aceita acentos, hífen e apóstrofo: "Ana D'Ávila-Souza")
    private static final Pattern NOME =
            Pattern.compile("^\\p{L}+([ '\\-]\\p{L}+)+$");

    private Validador() {
        // classe utilitária: não deve ser instanciada
    }

    /** Remove tudo que não for número: "529.982.247-25" -> "52998224725". */
    public static String somenteDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    public static boolean nomeValido(String nome) {
        return NOME.matcher(nome).matches();
    }

    public static boolean emailValido(String email) {
        return EMAIL.matcher(email).matches();
    }

    /** Telefone brasileiro com DDD: 10 dígitos (fixo) ou 11 (celular). */
    public static boolean telefoneValido(String digitos) {
        return digitos.matches("\\d{10,11}");
    }

    /** Mínimo 8 caracteres, pelo menos 1 letra e 1 número, sem espaços. */
    public static boolean senhaValida(String senha) {
        return senha.length() >= 8
                && senha.matches(".*\\p{L}.*")
                && senha.matches(".*\\d.*")
                && !senha.matches(".*\\s.*");
    }

    /**
     * Valida um CPF pelos dígitos verificadores.
     *
     * O CPF tem 11 dígitos: os 9 primeiros são a "base" e os 2 últimos são
     * calculados a partir deles. Se o cálculo bater com o que foi digitado,
     * o CPF é válido (matematicamente; isso não garante que ele exista na Receita).
     */
    public static boolean cpfValido(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}")) {
            return false;
        }
        // CPFs com todos os dígitos iguais (111.111.111-11) passam na conta, mas são inválidos
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }

        int primeiroDigito = calcularDigito(cpf, 9, 10);   // usa os 9 primeiros, pesos 10..2
        int segundoDigito = calcularDigito(cpf, 10, 11);   // usa os 10 primeiros, pesos 11..2

        return primeiroDigito == (cpf.charAt(9) - '0')
                && segundoDigito == (cpf.charAt(10) - '0');
    }

    private static int calcularDigito(String cpf, int quantidade, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            int numero = cpf.charAt(i) - '0';   // converte o caractere '7' no número 7
            soma += numero * (pesoInicial - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
