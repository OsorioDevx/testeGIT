package br.com.banco.view.componentes;

import br.com.banco.view.Tema;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Botão com cantos arredondados e cor que muda suavemente:
 * normal -> passar o mouse (um pouco mais escuro) -> clicar (mais escuro ainda).
 *
 * Em vez de deixar o Look and Feel desenhar o fundo, nós mesmos desenhamos
 * em paintComponent, e o texto continua sendo desenhado pelo JButton.
 */
public class BotaoAnimado extends JButton {

    private final Color corNormal;
    private final Color corHover;
    private final Color corPressionado;
    private final TransicaoDeCor fundo;

    public BotaoAnimado(String texto, Color cor, Color corTexto) {
        super(texto);
        this.corNormal = cor;
        this.corHover = Animador.misturar(cor, Color.BLACK, 0.10f);
        this.corPressionado = Animador.misturar(cor, Color.BLACK, 0.22f);
        this.fundo = new TransicaoDeCor(cor, 160, c -> repaint());

        setForeground(corTexto);
        setContentAreaFilled(false); // nós desenhamos o fundo
        setBorderPainted(false);
        setFocusPainted(false);      // o anel de foco também é desenhado por nós
        setOpaque(false);
        setBorder(new EmptyBorder(10, 14, 10, 14));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                fundo.irPara(corHover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                fundo.irPara(corNormal);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                fundo.irPara(corPressionado);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                fundo.irPara(contains(e.getPoint()) ? corHover : corNormal);
            }
        });

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(fundo.getAtual());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

        // Anel de foco, para quem navega pelo teclado (Tab) saber onde está
        if (isFocusOwner()) {
            boolean fundoClaro = corNormal.getRed() + corNormal.getGreen() + corNormal.getBlue() > 600;
            g2.setColor(fundoClaro ? Tema.AZUL : new Color(255, 255, 255, 150));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(3, 3, getWidth() - 7, getHeight() - 7, 8, 8);
        }
        g2.dispose();

        super.paintComponent(g); // desenha o texto
    }
}
