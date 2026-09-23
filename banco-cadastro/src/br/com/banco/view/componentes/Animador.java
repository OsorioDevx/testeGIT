package br.com.banco.view.componentes;

import br.com.banco.view.Tema;

import javax.swing.JComponent;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Point;
import java.util.function.Consumer;

/**
 * O "motor" de todas as animações do projeto.
 *
 * Como funciona uma animação no Swing:
 * um javax.swing.Timer dispara várias vezes por segundo (aqui, a cada ~15 ms ≈ 60 quadros/s).
 * A cada disparo calculamos o "progresso" t, que vai de 0.0 (início) a 1.0 (fim),
 * e atualizamos alguma coisa na tela (uma cor, uma posição, uma transparência).
 *
 * Por que javax.swing.Timer e não Thread.sleep? Porque o Timer roda na
 * Event Dispatch Thread, a única thread que pode mexer em componentes Swing.
 */
public final class Animador {

    private static final int INTERVALO_MS = 15;

    private Animador() {
    }

    /**
     * Executa uma animação.
     *
     * @param duracaoMs  quanto tempo a animação dura
     * @param quadro     chamado a cada quadro com o progresso t (0.0 a 1.0)
     * @param aoTerminar chamado uma vez no final (pode ser null)
     * @return o Timer, para quem quiser interromper a animação no meio (null se animações estão desligadas)
     */
    public static Timer animar(int duracaoMs, Consumer<Float> quadro, Runnable aoTerminar) {
        if (!Tema.ANIMACOES_ATIVADAS) {
            // Pula direto para o estado final
            quadro.accept(1f);
            if (aoTerminar != null) {
                aoTerminar.run();
            }
            return null;
        }

        long inicio = System.nanoTime();
        Timer timer = new Timer(INTERVALO_MS, null);
        timer.addActionListener(e -> {
            float decorrido = (System.nanoTime() - inicio) / 1_000_000f;
            float t = Math.min(1f, decorrido / duracaoMs);
            quadro.accept(t);
            if (t >= 1f) {
                timer.stop();
                if (aoTerminar != null) {
                    aoTerminar.run();
                }
            }
        });
        timer.start();
        return timer;
    }

    /**
     * "Easing" (suavização): em vez de andar em velocidade constante, a animação
     * começa rápida e desacelera no final, o que parece mais natural ao olho.
     * Fórmula ease-out cúbica: 1 - (1 - t)³
     */
    public static float suavizar(float t) {
        float inverso = 1f - t;
        return 1f - inverso * inverso * inverso;
    }

    /** Mistura duas cores. t = 0 devolve "de", t = 1 devolve "para", t = 0.5 fica no meio. */
    public static Color misturar(Color de, Color para, float t) {
        t = Math.max(0f, Math.min(1f, t));
        return new Color(
                Math.round(de.getRed() + (para.getRed() - de.getRed()) * t),
                Math.round(de.getGreen() + (para.getGreen() - de.getGreen()) * t),
                Math.round(de.getBlue() + (para.getBlue() - de.getBlue()) * t),
                Math.round(de.getAlpha() + (para.getAlpha() - de.getAlpha()) * t));
    }

    /**
     * Faz o componente "tremer" para os lados, como quem diz "não".
     * Usa uma onda seno cuja força vai diminuindo até parar.
     */
    public static void tremer(JComponent componente) {
        if (!Tema.ANIMACOES_ATIVADAS || Boolean.TRUE.equals(componente.getClientProperty("tremendo"))) {
            return; // já está tremendo: não empilha animações
        }
        componente.putClientProperty("tremendo", true);
        Point original = componente.getLocation();

        animar(420, t -> {
            double forca = 1 - t;                                   // 1 -> 0
            int dx = (int) Math.round(Math.sin(t * Math.PI * 6) * 9 * forca);
            componente.setLocation(original.x + dx, original.y);
        }, () -> {
            componente.setLocation(original);
            componente.putClientProperty("tremendo", false);
        });
    }
}
