package br.com.banco.view;

import java.awt.Color;
import java.awt.Font;

/**
 * Cores, fontes e configurações visuais em um só lugar.
 * Quer mudar a cara do sistema? Comece por aqui.
 */
public final class Tema {

    /**
     * Liga/desliga todas as animações. Com false, tudo muda na hora, sem transição.
     * Útil para comparar o antes e depois, ou para quem prefere menos movimento na tela.
     */
    public static final boolean ANIMACOES_ATIVADAS = true;

    // ---------- Cores ----------
    public static final Color AZUL_MARINHO = new Color(10, 37, 64);       // cabeçalho
    public static final Color AZUL = new Color(0, 102, 204);              // ação principal, foco
    public static final Color AZUL_CLARO = new Color(222, 235, 250);      // seleção, modo edição
    public static final Color AZUL_DESTAQUE = new Color(200, 222, 248);   // linha recém-editada
    public static final Color VERDE = new Color(30, 132, 73);             // sucesso
    public static final Color VERDE_CLARO = new Color(207, 237, 216);     // linha recém-cadastrada
    public static final Color VERMELHO = new Color(192, 57, 43);          // erro, exclusão
    public static final Color VERMELHO_CLARO = new Color(253, 237, 235);  // fundo de campo com erro
    public static final Color VERMELHO_SUAVE = new Color(246, 200, 194);  // linha sendo excluída
    public static final Color LARANJA = new Color(204, 122, 12);          // aviso
    public static final Color TINTA = new Color(29, 29, 31);              // texto principal
    public static final Color TEXTO_SUAVE = new Color(110, 110, 115);     // textos de apoio
    public static final Color FUNDO = new Color(245, 245, 247);           // fundo da janela
    public static final Color LINHA = new Color(224, 224, 228);           // bordas
    public static final Color CINZA_CLARO = new Color(238, 238, 241);     // botão secundário, etiqueta
    public static final Color ZEBRA = new Color(250, 250, 252);           // linhas alternadas
    public static final Color BRANCO = Color.WHITE;

    // ---------- Fontes ----------
    public static final Font FONTE = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONTE_NEGRITO = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONTE_ROTULO = new Font("SansSerif", Font.BOLD, 12);
    public static final Font FONTE_TITULO = new Font("SansSerif", Font.BOLD, 16);
    public static final Font FONTE_PEQUENA = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONTE_PEQUENA_NEGRITO = new Font("SansSerif", Font.BOLD, 11);

    private Tema() {
    }
}
