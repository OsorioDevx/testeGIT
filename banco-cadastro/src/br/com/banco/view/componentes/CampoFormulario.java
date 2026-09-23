package br.com.banco.view.componentes;

import br.com.banco.view.Tema;

import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.border.AbstractBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Junta tudo que um campo do formulário precisa: rótulo, caixa de texto e mensagem de erro.
 *
 * Estados visuais (todos com transição suave de cor):
 * - normal: borda cinza
 * - com foco: borda azul
 * - com erro: borda vermelha, fundo rosado e a mensagem aparecendo aos poucos embaixo
 *
 * Quando o usuário começa a corrigir um campo com erro, o erro some sozinho.
 */
public class CampoFormulario {

    private final JTextComponent campo;
    private final JLabel rotulo;
    private final JLabel mensagemErro;

    private final TransicaoDeCor corBorda;
    private final TransicaoDeCor corFundo;
    private final TransicaoDeCor corMensagem;

    private boolean comErro = false;

    public CampoFormulario(String textoRotulo, JTextComponent campo) {
        this.campo = campo;

        rotulo = new JLabel(textoRotulo);
        rotulo.setFont(Tema.FONTE_ROTULO);
        rotulo.setForeground(Tema.TINTA);
        // Largura pequena para o rótulo não alargar a coluna (as colunas ficam iguais)
        rotulo.setPreferredSize(new Dimension(50, rotulo.getPreferredSize().height));

        // O espaço da mensagem fica sempre reservado: assim o formulário não "pula" quando o erro aparece.
        // Sem erro, o texto fica branco (invisível no fundo branco).
        mensagemErro = new JLabel(" ");
        mensagemErro.setFont(Tema.FONTE_PEQUENA_NEGRITO);
        mensagemErro.setPreferredSize(new Dimension(50, 15));

        campo.setFont(Tema.FONTE);
        campo.setForeground(Tema.TINTA);
        campo.setBorder(new BordaAnimada());
        campo.setPreferredSize(new Dimension(100, 34));

        corBorda = new TransicaoDeCor(Tema.LINHA, 180, cor -> campo.repaint());
        corFundo = new TransicaoDeCor(Tema.BRANCO, 220, campo::setBackground);
        corMensagem = new TransicaoDeCor(Tema.BRANCO, 260, mensagemErro::setForeground);

        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (!comErro) {
                    corBorda.irPara(Tema.AZUL);
                }
                campo.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (!comErro) {
                    corBorda.irPara(Tema.LINHA);
                }
                campo.repaint();
            }
        });

        // Digitou algo num campo com erro? O erro vai embora.
        campo.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                limparErro();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                limparErro();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // mudança de estilo, não de texto: nada a fazer
            }
        });
    }

    public void mostrarErro(String mensagem) {
        comErro = true;
        mensagemErro.setText(mensagem);
        corBorda.irPara(Tema.VERMELHO);
        corFundo.irPara(Tema.VERMELHO_CLARO);
        corMensagem.definir(Tema.BRANCO);   // começa invisível...
        corMensagem.irPara(Tema.VERMELHO);  // ...e aparece aos poucos (fade-in)
        campo.repaint();
    }

    public void limparErro() {
        if (!comErro) {
            return;
        }
        comErro = false;
        corBorda.irPara(campo.isFocusOwner() ? Tema.AZUL : Tema.LINHA);
        corFundo.irPara(Tema.BRANCO);
        corMensagem.irPara(Tema.BRANCO, () -> mensagemErro.setText(" ")); // fade-out
        campo.repaint();
    }

    /** Apaga o texto e qualquer erro. */
    public void limpar() {
        campo.setText("");
        limparErro();
    }

    public String getTexto() {
        // JPasswordField devolve char[] por segurança; getText() nele é desaconselhado
        if (campo instanceof JPasswordField senha) {
            return new String(senha.getPassword());
        }
        return campo.getText();
    }

    public void setTexto(String texto) {
        campo.setText(texto);
    }

    public JTextComponent getCampo() {
        return campo;
    }

    public JLabel getRotulo() {
        return rotulo;
    }

    public JLabel getMensagemErro() {
        return mensagemErro;
    }

    /**
     * Borda arredondada cuja cor vem da TransicaoDeCor.
     * Fica 2px mais grossa quando o campo tem foco ou erro.
     */
    private class BordaAnimada extends AbstractBorder {

        private static final int RAIO = 8;

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int largura, int altura) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Pinta de branco os "cantinhos" fora do arredondado, para o fundo
            // colorido do campo (ex.: rosa do erro) não aparecer nas quinas.
            Area cantos = new Area(new Rectangle2D.Float(x, y, largura, altura));
            cantos.subtract(new Area(new RoundRectangle2D.Float(x + 1, y + 1, largura - 2, altura - 2, RAIO, RAIO)));
            g2.setColor(Tema.BRANCO);
            g2.fill(cantos);

            boolean destacado = comErro || campo.isFocusOwner();
            g2.setStroke(new BasicStroke(destacado ? 2f : 1f));
            g2.setColor(corBorda.getAtual());
            g2.drawRoundRect(x + 1, y + 1, largura - 3, altura - 3, RAIO, RAIO);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(7, 10, 7, 10);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(7, 10, 7, 10);
            return insets;
        }
    }
}
