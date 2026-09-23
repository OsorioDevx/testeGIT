package br.com.banco.view;

import br.com.banco.dto.UsuarioDTO;
import br.com.banco.model.Usuario;
import br.com.banco.service.UsuarioService;
import br.com.banco.service.ValidacaoException;
import br.com.banco.util.Formatador;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Tela principal: formulário à esquerda, lista de clientes à direita.
 *
 * Responsabilidade da tela: ler o que foi digitado, chamar o service
 * e mostrar o resultado. Nenhuma regra de negócio fica aqui.
 */
public class TelaCadastro extends JFrame {

    // ---------- Paleta e fontes (tudo centralizado para facilitar mudanças) ----------
    private static final Color AZUL_MARINHO = new Color(10, 37, 64);    // cabeçalho
    private static final Color AZUL = new Color(0, 102, 204);           // ação principal
    private static final Color VERMELHO = new Color(192, 57, 43);       // ação destrutiva
    private static final Color TINTA = new Color(29, 29, 31);           // texto principal
    private static final Color TEXTO_SUAVE = new Color(110, 110, 115);  // textos de apoio
    private static final Color FUNDO = new Color(245, 245, 247);        // fundo da janela
    private static final Color LINHA = new Color(224, 224, 228);        // bordas
    private static final Color SELECAO = new Color(222, 235, 250);      // linha selecionada

    private static final Font FONTE = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FONTE_NEGRITO = new Font("SansSerif", Font.BOLD, 13);
    private static final Font FONTE_ROTULO = new Font("SansSerif", Font.BOLD, 12);
    private static final Font FONTE_TITULO = new Font("SansSerif", Font.BOLD, 16);

    private final UsuarioService service;

    // ---------- Campos do formulário ----------
    private final JTextField campoNome = new JTextField();
    private final JTextField campoCpf = new JTextField();
    private final JTextField campoNascimento = new JTextField();
    private final JTextField campoEmail = new JTextField();
    private final JTextField campoTelefone = new JTextField();
    private final JTextField campoEndereco = new JTextField();
    private final JPasswordField campoSenha = new JPasswordField();
    private final JPasswordField campoConfirmarSenha = new JPasswordField();

    private final JLabel labelModo = new JLabel();
    private final JLabel labelTotal = new JLabel();

    // ---------- Tabela ----------
    private final DefaultTableModel modeloTabela = new DefaultTableModel(
            new Object[]{"ID", "Nome", "CPF", "Nascimento", "E-mail", "Telefone"}, 0) {

        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return false; // a tabela é só para visualizar; edição é pelo formulário
        }

        @Override
        public Class<?> getColumnClass(int coluna) {
            return coluna == 0 ? Integer.class : String.class; // faz o ID ordenar como número
        }
    };
    private final JTable tabela = new JTable(modeloTabela);

    /** null = cadastrando um novo cliente | número = editando o cliente com esse id */
    private Integer idEmEdicao = null;

    public TelaCadastro(UsuarioService service) {
        super("Banco Didático - Cadastro de clientes");
        this.service = service;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1300, 700);
        setMinimumSize(new Dimension(1100, 640));
        setLocationRelativeTo(null); // centraliza na tela

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(FUNDO);
        raiz.add(criarCabecalho(), BorderLayout.NORTH);
        raiz.add(criarConteudo(), BorderLayout.CENTER);
        setContentPane(raiz);

        atualizarTabela();
        limparFormulario();
    }

    // =====================================================================
    //  MONTAGEM DA INTERFACE
    // =====================================================================

    private JPanel criarCabecalho() {
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(AZUL_MARINHO);
        cabecalho.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel titulo = new JLabel("Banco Didático");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        titulo.setForeground(Color.WHITE);

        JLabel subtitulo = new JLabel("Cadastro e gerenciamento de clientes");
        subtitulo.setFont(FONTE);
        subtitulo.setForeground(new Color(180, 196, 216));

        JPanel textos = new JPanel(new GridLayout(2, 1, 0, 2));
        textos.setOpaque(false);
        textos.add(titulo);
        textos.add(subtitulo);

        cabecalho.add(textos, BorderLayout.WEST);
        return cabecalho;
    }

    private JPanel criarConteudo() {
        JPanel conteudo = new JPanel(new BorderLayout(16, 0));
        conteudo.setOpaque(false);
        conteudo.setBorder(new EmptyBorder(16, 16, 16, 16));
        conteudo.add(criarPainelFormulario(), BorderLayout.WEST);
        conteudo.add(criarPainelLista(), BorderLayout.CENTER);
        return conteudo;
    }

    private JPanel criarPainelFormulario() {
        JPanel card = criarCard();
        card.setLayout(new BorderLayout(0, 14));
        card.setPreferredSize(new Dimension(440, 0));

        // --- Título + indicador de modo (novo cadastro / editando) ---
        JLabel titulo = new JLabel("Dados do cliente");
        titulo.setFont(FONTE_TITULO);
        titulo.setForeground(TINTA);
        labelModo.setFont(FONTE);

        JPanel topo = new JPanel(new GridLayout(2, 1, 0, 2));
        topo.setOpaque(false);
        topo.add(titulo);
        topo.add(labelModo);

        // --- Campos em duas colunas (GridBagLayout) ---
        JPanel campos = new JPanel(new GridBagLayout());
        campos.setOpaque(false);

        //             painel  rótulo                         campo                col linha largura
        adicionarCampo(campos, "Nome completo *",             campoNome,           0,  0,    2);
        adicionarCampo(campos, "CPF *",                       campoCpf,            0,  1,    1);
        adicionarCampo(campos, "Nascimento * (dd/mm/aaaa)",   campoNascimento,     1,  1,    1);
        adicionarCampo(campos, "E-mail *",                    campoEmail,          0,  2,    1);
        adicionarCampo(campos, "Telefone * (com DDD)",        campoTelefone,       1,  2,    1);
        adicionarCampo(campos, "Endereço *",                  campoEndereco,       0,  3,    2);
        adicionarCampo(campos, "Senha *",                     campoSenha,          0,  4,    1);
        adicionarCampo(campos, "Confirmar senha *",           campoConfirmarSenha, 1,  4,    1);

        JLabel dicaSenha = new JLabel("Senha: mínimo de 8 caracteres, com letras e números.");
        dicaSenha.setFont(FONTE.deriveFont(11.5f));
        dicaSenha.setForeground(TEXTO_SUAVE);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 10;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(6, 0, 0, 0);
        campos.add(dicaSenha, c);

        // "Mola" que empurra os campos para cima quando sobra espaço
        c = new GridBagConstraints();
        c.gridy = 11;
        c.weighty = 1;
        campos.add(Box.createVerticalGlue(), c);

        campoCpf.setToolTipText("Pode digitar com ou sem pontuação");
        campoTelefone.setToolTipText("Ex.: (92) 99123-4567");

        // --- Botões ---
        JPanel botoes = new JPanel(new GridLayout(2, 2, 8, 8));
        botoes.setOpaque(false);
        botoes.add(criarBotao("Cadastrar", AZUL, Color.WHITE, this::cadastrar));
        botoes.add(criarBotao("Salvar alterações", AZUL, Color.WHITE, this::salvarEdicao));
        botoes.add(criarBotao("Limpar formulário", new Color(232, 232, 237), TINTA, this::limparFormulario));
        botoes.add(criarBotao("Excluir cliente", VERMELHO, Color.WHITE, this::excluir));

        card.add(topo, BorderLayout.NORTH);
        card.add(campos, BorderLayout.CENTER);
        card.add(botoes, BorderLayout.SOUTH);
        return card;
    }

    private JPanel criarPainelLista() {
        JPanel card = criarCard();
        card.setLayout(new BorderLayout(0, 12));

        JLabel titulo = new JLabel("Clientes cadastrados");
        titulo.setFont(FONTE_TITULO);
        titulo.setForeground(TINTA);
        labelTotal.setFont(FONTE);
        labelTotal.setForeground(TEXTO_SUAVE);

        JLabel dica = new JLabel("Clique em um cliente para editar ou excluir.");
        dica.setFont(FONTE);
        dica.setForeground(TEXTO_SUAVE);

        JPanel linhaTitulo = new JPanel(new BorderLayout());
        linhaTitulo.setOpaque(false);
        linhaTitulo.add(titulo, BorderLayout.WEST);
        linhaTitulo.add(labelTotal, BorderLayout.EAST);

        JPanel topo = new JPanel(new GridLayout(2, 1, 0, 2));
        topo.setOpaque(false);
        topo.add(linhaTitulo);
        topo.add(dica);

        configurarTabela();
        JScrollPane rolagem = new JScrollPane(tabela);
        rolagem.setBorder(new LineBorder(LINHA));
        rolagem.getViewport().setBackground(Color.WHITE);

        card.add(topo, BorderLayout.NORTH);
        card.add(rolagem, BorderLayout.CENTER);
        return card;
    }

    private void configurarTabela() {
        tabela.setFont(FONTE);
        tabela.setRowHeight(30);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setShowVerticalLines(false);
        tabela.setGridColor(LINHA);
        tabela.setSelectionBackground(SELECAO);
        tabela.setSelectionForeground(TINTA);
        tabela.setFillsViewportHeight(true);
        tabela.setAutoCreateRowSorter(true); // clicar no título da coluna ordena
        tabela.getTableHeader().setReorderingAllowed(false);

        // Renderizador das células: espaçamento interno + linhas alternadas
        DefaultTableCellRenderer celula = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean selecionada,
                                                           boolean foco, int linha, int coluna) {
                super.getTableCellRendererComponent(t, valor, selecionada, false, linha, coluna);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                if (!selecionada) {
                    setBackground(linha % 2 == 0 ? Color.WHITE : new Color(250, 250, 252));
                }
                return this;
            }
        };
        tabela.setDefaultRenderer(Object.class, celula);
        tabela.setDefaultRenderer(String.class, celula);
        tabela.setDefaultRenderer(Integer.class, celula);

        // Renderizador do cabeçalho
        tabela.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean selecionada,
                                                           boolean foco, int linha, int coluna) {
                super.getTableCellRendererComponent(t, valor, false, false, linha, coluna);
                setFont(FONTE_ROTULO);
                setForeground(TEXTO_SUAVE);
                setBackground(new Color(247, 247, 249));
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, LINHA),
                        new EmptyBorder(8, 10, 8, 10)));
                return this;
            }
        });

        // Larguras iniciais das colunas
        int[] larguras = {40, 170, 125, 110, 190, 130};
        for (int i = 0; i < larguras.length; i++) {
            tabela.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }

        // Ao selecionar uma linha, carrega o cliente no formulário
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                carregarClienteSelecionado();
            }
        });
    }

    // ---------- Pequenos "construtores" de componentes estilizados ----------

    private JPanel criarCard() {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(LINHA, 1, true),
                new EmptyBorder(18, 18, 18, 18)));
        return card;
    }

    /**
     * Adiciona um rótulo e, logo abaixo, o campo. Cada "linha lógica" ocupa
     * duas linhas do GridBagLayout (uma para o rótulo, outra para o campo).
     */
    private void adicionarCampo(JPanel painel, String rotulo, JComponent campo,
                                int coluna, int linha, int largura) {
        estilizarCampo(campo);

        int margemEsquerda = coluna == 1 ? 6 : 0;
        int margemDireita = (coluna == 0 && largura == 1) ? 6 : 0;

        JLabel label = new JLabel(rotulo);
        label.setFont(FONTE_ROTULO);
        label.setForeground(TINTA);
        // Largura "mínima" pequena para o rótulo não alargar sua coluna:
        // assim as duas colunas do formulário ficam sempre do mesmo tamanho.
        label.setPreferredSize(new Dimension(50, label.getPreferredSize().height));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = coluna;
        c.gridwidth = largura;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;

        c.gridy = linha * 2;
        c.insets = new Insets(linha == 0 ? 0 : 10, margemEsquerda, 4, margemDireita);
        painel.add(label, c);

        c.gridy = linha * 2 + 1;
        c.insets = new Insets(0, margemEsquerda, 0, margemDireita);
        painel.add(campo, c);
    }

    private void estilizarCampo(JComponent campo) {
        Border normal = BorderFactory.createCompoundBorder(
                new LineBorder(LINHA, 1, true), new EmptyBorder(6, 8, 6, 8));
        Border focado = BorderFactory.createCompoundBorder(
                new LineBorder(AZUL, 1, true), new EmptyBorder(6, 8, 6, 8));

        campo.setFont(FONTE);
        campo.setForeground(TINTA);
        campo.setBorder(normal);
        // Mesma ideia do rótulo: largura pequena e fixa para as colunas não
        // mudarem de tamanho quando um campo recebe um texto longo.
        campo.setPreferredSize(new Dimension(100, 34));

        // Borda azul no campo que está com o cursor
        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                campo.setBorder(focado);
            }

            @Override
            public void focusLost(FocusEvent e) {
                campo.setBorder(normal);
            }
        });
    }

    private JButton criarBotao(String texto, Color fundo, Color cor, Runnable acao) {
        JButton botao = new JButton(texto);
        botao.setFont(FONTE_NEGRITO);
        botao.setBackground(fundo);
        botao.setForeground(cor);
        botao.setOpaque(true);
        botao.setFocusPainted(false);
        botao.setBorder(new EmptyBorder(10, 14, 10, 14));
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botao.addActionListener(e -> acao.run());

        // Escurece um pouco ao passar o mouse
        Color hover = fundo.darker();
        botao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                botao.setBackground(hover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                botao.setBackground(fundo);
            }
        });
        return botao;
    }

    // =====================================================================
    //  AÇÕES DOS BOTÕES
    // =====================================================================

    private void cadastrar() {
        if (idEmEdicao != null) {
            mostrarAviso("Você está editando um cliente.\n"
                    + "Para cadastrar um novo, clique em \"Limpar formulário\" primeiro.");
            return;
        }
        try {
            Usuario novo = service.cadastrar(lerFormulario());
            atualizarTabela();
            limparFormulario();
            mostrarSucesso("Cliente " + novo.getNomeCompleto() + " cadastrado com sucesso!");
        } catch (ValidacaoException ex) {
            mostrarErros(ex);
        }
    }

    private void salvarEdicao() {
        if (idEmEdicao == null) {
            mostrarAviso("Selecione um cliente na lista para editar.");
            return;
        }
        try {
            Usuario atualizado = service.editar(idEmEdicao, lerFormulario());
            atualizarTabela();
            limparFormulario();
            mostrarSucesso("Dados de " + atualizado.getNomeCompleto() + " atualizados com sucesso!");
        } catch (ValidacaoException ex) {
            mostrarErros(ex);
        }
    }

    private void excluir() {
        if (idEmEdicao == null) {
            mostrarAviso("Selecione um cliente na lista para excluir.");
            return;
        }

        String nome = service.buscarPorId(idEmEdicao)
                .map(Usuario::getNomeCompleto)
                .orElse("selecionado");

        int resposta = JOptionPane.showConfirmDialog(this,
                "Deseja realmente excluir o cliente " + nome + "?\nEssa ação não pode ser desfeita.",
                "Confirmar exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (resposta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            service.excluir(idEmEdicao);
            atualizarTabela();
            limparFormulario();
            mostrarSucesso("Cliente " + nome + " excluído com sucesso.");
        } catch (ValidacaoException ex) {
            mostrarErros(ex);
        }
    }

    private void limparFormulario() {
        campoNome.setText("");
        campoCpf.setText("");
        campoNascimento.setText("");
        campoEmail.setText("");
        campoTelefone.setText("");
        campoEndereco.setText("");
        campoSenha.setText("");
        campoConfirmarSenha.setText("");

        tabela.clearSelection();
        idEmEdicao = null;

        labelModo.setText("Novo cadastro");
        labelModo.setForeground(TEXTO_SUAVE);
        campoNome.requestFocusInWindow();
    }

    // =====================================================================
    //  AUXILIARES
    // =====================================================================

    /** Junta o que foi digitado em um DTO para enviar ao service. */
    private UsuarioDTO lerFormulario() {
        return new UsuarioDTO(
                campoNome.getText(),
                campoCpf.getText(),
                campoNascimento.getText(),
                campoEmail.getText(),
                campoTelefone.getText(),
                campoEndereco.getText(),
                new String(campoSenha.getPassword()),
                new String(campoConfirmarSenha.getPassword())
        );
    }

    /** Recarrega a tabela com os dados atuais do service (é a "listagem" de clientes). */
    private void atualizarTabela() {
        modeloTabela.setRowCount(0);

        List<Usuario> usuarios = service.listar();
        for (Usuario u : usuarios) {
            modeloTabela.addRow(new Object[]{
                    u.getId(),
                    u.getNomeCompleto(),
                    Formatador.cpf(u.getCpf()),
                    Formatador.data(u.getDataNascimento()),
                    u.getEmail(),
                    Formatador.telefone(u.getTelefone())
            });
        }

        int total = usuarios.size();
        labelTotal.setText(total == 1 ? "1 cliente" : total + " clientes");
    }

    private void carregarClienteSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            return;
        }
        int id = (Integer) tabela.getValueAt(linha, 0);
        service.buscarPorId(id).ifPresent(this::preencherFormulario);
    }

    private void preencherFormulario(Usuario u) {
        campoNome.setText(u.getNomeCompleto());
        campoCpf.setText(Formatador.cpf(u.getCpf()));
        campoNascimento.setText(Formatador.data(u.getDataNascimento()));
        campoEmail.setText(u.getEmail());
        campoTelefone.setText(Formatador.telefone(u.getTelefone()));
        campoEndereco.setText(u.getEndereco());
        campoSenha.setText(u.getSenha());
        campoConfirmarSenha.setText(u.getSenha());

        idEmEdicao = u.getId();
        labelModo.setText("Editando: " + u.getNomeCompleto() + " (ID " + u.getId() + ")");
        labelModo.setForeground(AZUL);
    }

    private void mostrarSucesso(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarAviso(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    private void mostrarErros(ValidacaoException ex) {
        StringBuilder texto = new StringBuilder("Não foi possível salvar. Corrija os itens abaixo:\n\n");
        for (String erro : ex.getErros()) {
            texto.append("•  ").append(erro).append("\n");
        }
        JOptionPane.showMessageDialog(this, texto.toString(), "Dados inválidos", JOptionPane.ERROR_MESSAGE);
    }
}
