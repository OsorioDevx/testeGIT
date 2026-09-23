package br.com.banco.view.componentes;

import javax.swing.Timer;
import java.awt.Color;
import java.util.function.Consumer;

/**
 * Guarda uma cor "atual" e sabe ir suavemente até outra cor.
 *
 * Exemplo: um botão azul que escurece aos poucos quando o mouse passa por cima.
 * Se uma nova transição começa antes da anterior acabar, ela parte da cor
 * em que a anterior parou, então nunca há "saltos".
 */
public class TransicaoDeCor {

    private final int duracaoMs;
    private final Consumer<Color> aoMudar;
    private Color atual;
    private Timer timer;

    /**
     * @param inicial   cor de partida
     * @param duracaoMs duração de cada transição
     * @param aoMudar   o que fazer a cada nova cor (ex.: campo::setBackground ou apenas repaint)
     */
    public TransicaoDeCor(Color inicial, int duracaoMs, Consumer<Color> aoMudar) {
        this.atual = inicial;
        this.duracaoMs = duracaoMs;
        this.aoMudar = aoMudar;
        aoMudar.accept(inicial);
    }

    public void irPara(Color destino) {
        irPara(destino, null);
    }

    public void irPara(Color destino, Runnable aoTerminar) {
        parar();
        Color origem = atual;
        timer = Animador.animar(duracaoMs, t -> {
            atual = Animador.misturar(origem, destino, Animador.suavizar(t));
            aoMudar.accept(atual);
        }, aoTerminar);
    }

    /** Troca a cor na hora, sem animação. */
    public void definir(Color cor) {
        parar();
        atual = cor;
        aoMudar.accept(cor);
    }

    public Color getAtual() {
        return atual;
    }

    private void parar() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }
}
