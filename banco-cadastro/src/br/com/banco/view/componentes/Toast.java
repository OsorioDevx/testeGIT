package br.com.banco.view.componentes;

import br.com.banco.view.Tema;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

/**
 * Notificação que desliza de baixo para cima no canto da janela,
 * fica alguns segundos e some sozinha (ou ao ser clicada).
 *
 * Diferente do JOptionPane, ela não bloqueia a tela: o usuário continua trabalhando.
 *
 * Truque usado: todo JFrame tem um JLayeredPane, uma "pilha de camadas" por cima
 * do conteúdo. Colocamos o toast na camada POPUP_LAYER, acima de tudo.
 */
public class Toast extends JComponent {

    public enum Tipo {
        SUCESSO(Tema.VERDE),
        ERRO(Tema.VERMELHO),
        AVISO(Tema.LARANJA);

        private final Color cor;

        Tipo(Color cor) {
            this.cor = cor;
        }
    }

    private static final int ALTURA_CARTAO = 46;
    private static final int FOLGA_ANIMACAO = 24;   // espaço extra para o cartão deslizar sem ser cortado
    private static final int LARGURA_MAXIMA = 460;
    private static final int TEMPO_VISIVEL_MS = 3200;
    private static final int MARGEM = 28;

    private static Toast atual; // só um toast por vez

    private final String mensagem;
    private final Tipo tipo;

    private float opacidade = 0f;   // 0 = invisível, 1 = totalmente visível
    private int deslocamentoY = 0;  // quantos pixels abaixo da posição final
    private boolean saindo = false;
    private Timer timerAnimacao;
    private Timer timerEspera;

    /** Mostra uma notificação no canto inferior direito da janela. */
    public static void mostrar(JFrame janela, String mensagem, Tipo tipo) {
        if (atual != null) {
            atual.remover();
        }

        JLayeredPane camadas = janela.getLayeredPane();
        Toast toast = new Toast(mensagem, tipo);

        int largura = toast.calcularLargura();
        int altura = ALTURA_CARTAO + FOLGA_ANIMACAO;
        int x = camadas.getWidth() - largura - MARGEM;
        int y = camadas.getHeight() - altura - MARGEM + FOLGA_ANIMACAO;

        toast.setBounds(x, y, largura, altura);
        camadas.add(toast, JLayeredPane.POPUP_LAYER);
        atual = toast;
        toast.entrar();
    }

    private Toast(String mensagem, Tipo tipo) {
        this.mensagem = mensagem;
        this.tipo = tipo;
        setFont(Tema.FONTE);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                sair();
            }
        });
    }

    private int calcularLargura() {
        FontMetrics fm = getFontMetrics(getFont());
        int larguraTexto = fm.stringWidth(mensagem);
        return Math.min(LARGURA_MAXIMA, 16 + 22 + 12 + larguraTexto + 20);
    }

    /** Entrada: sobe 20px enquanto aparece (fade-in). Depois espera e chama sair(). */
    private void entrar() {
        timerAnimacao = Animador.animar(300, t -> {
            float e = Animador.suavizar(t);
            opacidade = e;
            deslocamentoY = Math.round((1 - e) * 20);
            repaint();
        }, () -> {
            timerEspera = new Timer(TEMPO_VISIVEL_MS, ev -> sair());
            timerEspera.setRepeats(false);
            timerEspera.start();
        });
    }

    /** Saída: desce um pouco enquanto desaparece (fade-out) e depois se remove. */
    private void sair() {
        if (saindo) {
            return;
        }
        saindo = true;
        pararTimers();
        float opacidadeInicial = opacidade;
        timerAnimacao = Animador.animar(280, t -> {
            opacidade = opacidadeInicial * (1 - t);
            deslocamentoY = Math.round(t * 12);
            repaint();
        }, this::remover);
    }

    private void remover() {
        pararTimers();
        Container pai = getParent();
        if (pai != null) {
            pai.remove(this);
            pai.repaint(getX(), getY(), getWidth(), getHeight());
        }
        if (atual == this) {
            atual = null;
        }
    }

    private void pararTimers() {
        if (timerAnimacao != null) {
            timerAnimacao.stop();
        }
        if (timerEspera != null) {
            timerEspera.stop();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        // Toda a pintura abaixo sai com a transparência atual
        g2.setComposite(AlphaComposite.SrcOver.derive(opacidade));

        int y = deslocamentoY;
        int largura = getWidth() - 4;
        int altura = ALTURA_CARTAO - 4;

        // Sombra discreta + cartão branco com borda
        g2.setColor(new Color(0, 0, 0, 30));
        g2.fillRoundRect(2, y + 3, largura, altura, 12, 12);
        g2.setColor(Tema.BRANCO);
        g2.fillRoundRect(2, y, largura, altura, 12, 12);
        g2.setColor(Tema.LINHA);
        g2.drawRoundRect(2, y, largura - 1, altura - 1, 12, 12);

        // Ícone: círculo colorido com ✓ (sucesso) ou ! (erro/aviso), desenhado com formas
        int cx = 16;
        int cy = y + altura / 2 - 11;
        g2.setColor(tipo.cor);
        g2.fill(new Ellipse2D.Float(cx, cy, 22, 22));
        g2.setColor(Tema.BRANCO);
        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (tipo == Tipo.SUCESSO) {
            Path2D.Float check = new Path2D.Float();
            check.moveTo(cx + 6.5f, cy + 11.5f);
            check.lineTo(cx + 9.8f, cy + 14.8f);
            check.lineTo(cx + 15.5f, cy + 7.8f);
            g2.draw(check);
        } else {
            g2.drawLine(cx + 11, cy + 6, cx + 11, cy + 12);
            g2.fill(new Ellipse2D.Float(cx + 9.6f, cy + 14.6f, 2.8f, 2.8f));
        }

        // Texto (cortado com "..." se não couber)
        g2.setFont(getFont());
        g2.setColor(Tema.TINTA);
        FontMetrics fm = g2.getFontMetrics();
        int xTexto = cx + 22 + 12;
        String texto = cortar(mensagem, fm, largura - xTexto - 14);
        int yTexto = y + (altura - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(texto, xTexto, yTexto);

        g2.dispose();
    }

    private static String cortar(String texto, FontMetrics fm, int larguraMaxima) {
        if (fm.stringWidth(texto) <= larguraMaxima) {
            return texto;
        }
        String reticencias = "...";
        int fim = texto.length();
        while (fim > 0 && fm.stringWidth(texto.substring(0, fim) + reticencias) > larguraMaxima) {
            fim--;
        }
        return texto.substring(0, fim) + reticencias;
    }
}
