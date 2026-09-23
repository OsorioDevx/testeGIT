package br.com.banco;

import br.com.banco.repository.UsuarioRepository;
import br.com.banco.service.UsuarioService;
import br.com.banco.view.TelaCadastro;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Ponto de entrada da aplicação.
 *
 * Aqui as camadas são "montadas" na ordem certa:
 * Repository (armazenamento) -> Service (regras) -> Tela (interface).
 */
public class Main {

    public static void main(String[] args) {
        // Toda criação/alteração de componentes Swing deve rodar na
        // Event Dispatch Thread (EDT). O invokeLater garante isso.
        SwingUtilities.invokeLater(() -> {
            try {
                // Look and feel padrão do Java (igual em Windows, Linux e macOS),
                // o que garante que as cores personalizadas funcionem em qualquer sistema.
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
                // Se falhar, o Swing usa o padrão dele. Não é um erro grave.
            }

            UsuarioRepository repository = new UsuarioRepository();
            UsuarioService service = new UsuarioService(repository);

            TelaCadastro tela = new TelaCadastro(service);
            tela.setVisible(true);
        });
    }
}
