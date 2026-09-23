package br.com.banco.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Deixa os dados "bonitos" para exibir na tela.
 * Os dados ficam guardados só com números; a formatação é só visual.
 */
public final class Formatador {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Formatador() {
    }

    /** "52998224725" -> "529.982.247-25" */
    public static String cpf(String digitos) {
        if (digitos == null || digitos.length() != 11) {
            return digitos;
        }
        return digitos.substring(0, 3) + "." + digitos.substring(3, 6) + "."
                + digitos.substring(6, 9) + "-" + digitos.substring(9);
    }

    /** "92991234567" -> "(92) 99123-4567"  |  "9233214567" -> "(92) 3321-4567" */
    public static String telefone(String digitos) {
        if (digitos == null) {
            return "";
        }
        if (digitos.length() == 11) {
            return "(" + digitos.substring(0, 2) + ") " + digitos.substring(2, 7) + "-" + digitos.substring(7);
        }
        if (digitos.length() == 10) {
            return "(" + digitos.substring(0, 2) + ") " + digitos.substring(2, 6) + "-" + digitos.substring(6);
        }
        return digitos;
    }

    public static String data(LocalDate data) {
        return data == null ? "" : data.format(DATA);
    }
}
