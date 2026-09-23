package br.com.banco.view;

import br.com.banco.dto.UsuarioDTO;
import br.com.banco.model.Usuario;
import br.com.banco.service.Campo;
import br.com.banco.service.UsuarioService;
import br.com.banco.service.ValidacaoException;
import br.com.banco.util.Formatador;
import br.com.banco.view.componentes.Animador;
import br.com.banco.view.componentes.BotaoAnimado;
import br.com.banco.view.componentes.CampoFormulario;
import br.com.banco.view.componentes.Toast;
import br.com.banco.view.componentes.TransicaoDeCor;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Tela principal: formulário à esquerda, lista de clientes à direita.
 *
 * Responsabilidade da tela: ler o que foi digitado, chamar o service
 * e mostrar o resultado. Nenhuma regra de negócio fica aqui.
 *
 * Efeitos visuais usados (todos no pacote view.componentes):
 * - botões com transição de cor ao passar o mouse e clicar;
 * - campos com borda animada (foco azul, erro vermelho) e mensagem de erro que aparece aos poucos;
 * - formulário "treme" quando há erro de validação;
 * - notificações (toasts) que deslizam no canto da tela;
 * - linha da tabela que brilha em verde ao cadastrar, em azul ao editar e fica vermelha antes de sumir ao excluir;
 * - destaque de linha ao passar o mouse e etiqueta de modo que muda de cor.
 */
public class TelaCadastro extends JFrame {

    private final UsuarioService service;

    // ---------- Formulário ----------
    private final Map<Campo, CampoFormulario> campos = new EnumMap<>(Campo.class);
    private JPanel cardFormulario;
    private JLabel labelModo;
    private TransicaoDeCor fundoModo;
    private final JLabel labelTotal = new JLabel();

    /** null = cadastrando um novo cliente | número = editando o cliente com esse id */
    private Integer idEmEdicao = null;

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

    private final JTable tabela = new JTable(modeloTabela) {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getRowCount() == 0) {
                desenharTabelaVazia((Graphics2D) g, getVisibleRect());
            }
        }
    };

    // Estado do destaque animado de uma linha (brilho ao cadastrar/editar, vermelho ao excluir)
    private Integer idDestacado = null;
    private Color corDestaque = Tema.BRANCO;
    private float intensidadeDestaque = 0f;
    private Timer timerDestaque;

    private int linhaSobMouse = -1;

    public TelaCadastro(UsuarioService service) {
        super("Banco Didático - Cadastro de clientes");
        this.service = service;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1300, 720);
        setMinimumSize(new Dimension(1100, 660));
        setLocationRelativeTo(null); // centraliza na tela

        criarCampos();

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Tema.FUNDO);
        raiz.add(criarCabecalho(), BorderLayout.NORTH);
        raiz.add(criarConteudo(), BorderLayout.CENTER);
        setContentPane(raiz);

        atualizarTabela();
        limparFormulario();
    }

    // =====================================================================
    //  MONTAGEM DA INTERFACE
    // =====================================================================

    private void criarCampos() {
        campos.put(Campo.NOME, new CampoFormulario("Nome completo *", new JTextField()));
        campos.put(Campo.CPF, new CampoFormulario("CPF *", new JTextField()));
        campos.put(Campo.NASCIMENTO, new CampoFormulario("Nascimento * (dd/mm/aaaa)", new JTextField()));
        campos.put(Campo.EMAIL, new CampoFormulario("E-mail *", new JTextField()));
        campos.put(Campo.TELEFONE, new CampoFormulario("Telefone * (com DDD)", new JTextField()));
        campos.put(Campo.ENDERECO, new CampoFormulario("Endereço *", new JTextField()));
        campos.put(Campo.SENHA, new CampoFormulario("Senha *", new JPasswordField()));
        campos.put(Campo.CONFIRMACAO_SENHA, new CampoFormulario("Confirmar senha *", new JPasswordField()));

        campos.get(Campo.CPF).getCampo().setToolTipText("Pode digitar com ou sem pontuação");
        campos.get(Campo.TELEFONE).getCampo().setToolTipText("Ex.: (92) 99123-4567");

        // Apertar Enter em qualquer campo = Cadastrar (ou Salvar, se estiver editando)
        for (CampoFormulario campo : campos.values()) {
            if (campo.getCampo() instanceof JTextField caixa) {
                caixa.addActionListener(e -> enviarFormulario());
            }
        }
    }

    private JPanel criarCabecalho() {
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(Tema.AZUL_MARINHO);
        cabecalho.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel titulo = new JLabel("Banco Didático");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        titulo.setForeground(Color.WHITE);

        JLabel subtitulo = new JLabel("Cadastro e gerenciamento de clientes");
        subtitulo.setFont(Tema.FONTE);
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
        cardFormulario = criarCard();
        cardFormulario.setLayout(new BorderLayout(0, 12));
        cardFormulario.setPreferredSize(new Dimension(440, 0));

        // --- Título + etiqueta de modo (novo cadastro / editando) ---
        JLabel titulo = new JLabel("Dados do cliente");
        titulo.setFont(Tema.FONTE_TITULO);
        titulo.setForeground(Tema.TINTA);

        labelModo = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                // fundo em formato de "pílula", com a cor animada
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fundoModo.getAtual());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        labelModo.setFont(Tema.FONTE_PEQUENA_NEGRITO);
        labelModo.setBorder(new EmptyBorder(3, 10, 3, 10));
        fundoModo = new TransicaoDeCor(Tema.CINZA_CLARO, 260, cor -> labelModo.repaint());

        JPanel linhaModo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linhaModo.setOpaque(false);
        linhaModo.add(labelModo);

        JPanel topo = new JPanel(new GridLayout(2, 1, 0, 4));
        topo.setOpaque(false);
        topo.add(titulo);
        topo.add(linhaModo);

        // --- Campos em duas colunas (GridBagLayout) ---
        JPanel painelCampos = new JPanel(new GridBagLayout());
        painelCampos.setOpaque(false);

        //             painel        campo                    col linha largura
        adicionarCampo(painelCampos, Campo.NOME,              0,  0,    2);
        adicionarCampo(painelCampos, Campo.CPF,               0,  1,    1);
        adicionarCampo(painelCampos, Campo.NASCIMENTO,        1,  1,    1);
        adicionarCampo(painelCampos, Campo.EMAIL,             0,  2,    1);
        adicionarCampo(painelCampos, Campo.TELEFONE,          1,  2,    1);
        adicionarCampo(painelCampos, Campo.ENDERECO,          0,  3,    2);
        adicionarCampo(painelCampos, Campo.SENHA,             0,  4,    1);
        adicionarCampo(painelCampos, Campo.CONFIRMACAO_SENHA, 1,  4,    1);

        JLabel dicaSenha = new JLabel("Senha: mínimo de 8 caracteres, com letras e números.");
        dicaSenha.setFont(Tema.FONTE_PEQUENA);
        dicaSenha.setForeground(Tema.TEXTO_SUAVE);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 15;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(4, 0, 0, 0);
        painelCampos.add(dicaSenha, c);

        // "Mola" que empurra os campos para cima quando sobra espaço
        c = new GridBagConstraints();
        c.gridy = 16;
        c.weighty = 1;
        painelCampos.add(Box.createVerticalGlue(), c);

        // --- Botões ---
        JPanel botoes = new JPanel(new GridLayout(2, 2, 8, 8));
        botoes.setOpaque(false);
        botoes.add(criarBotao("Cadastrar", Tema.AZUL, Color.WHITE, this::cadastrar));
        botoes.add(criarBotao("Salvar alterações", Tema.AZUL, Color.WHITE, this::salvarEdicao));
        botoes.add(criarBotao("Limpar formulário", Tema.CINZA_CLARO, Tema.TINTA, this::limparFormulario));
        botoes.add(criarBotao("Excluir cliente", Tema.VERMELHO, Color.WHITE, this::excluir));

        cardFormulario.add(topo, BorderLayout.NORTH);
        cardFormulario.add(painelCampos, BorderLayout.CENTER);
        cardFormulario.add(botoes, BorderLayout.SOUTH);
        return cardFormulario;
    }

    private JPanel criarPainelLista() {
        JPanel card = criarCard();
        card.setLayout(new BorderLayout(0, 12));

        JLabel titulo = new JLabel("Clientes cadastrados");
        titulo.setFont(Tema.FONTE_TITULO);
        titulo.setForeground(Tema.TINTA);
        labelTotal.setFont(Tema.FONTE);
        labelTotal.setForeground(Tema.TEXTO_SUAVE);

        JLabel dica = new JLabel("Clique em um cliente para editar ou excluir.");
        dica.setFont(Tema.FONTE);
        dica.setForeground(Tema.TEXTO_SUAVE);

        JPanel linhaTitulo = new JPanel(new BorderLayout());
        linhaTitulo.setOpaque(false);
        linhaTitulo.add(titulo, BorderLayout.WEST);
        linhaTitulo.add(labelTotal, BorderLayout.EAST);

        JPanel topo = new JPanel(new GridLayout(2, 1, 0, 4));
        topo.setOpaque(false);
        topo.add(linhaTitulo);
        topo.add(dica);

        configurarTabela();
        JScrollPane rolagem = new JScrollPane(tabela);
        rolagem.setBorder(new LineBorder(Tema.LINHA));
        rolagem.getViewport().setBackground(Color.WHITE);

        card.add(topo, BorderLayout.NORTH);
        card.add(rolagem, BorderLayout.CENTER);
        return card;
    }

    private void configurarTabela() {
        tabela.setFont(Tema.FONTE);
        tabela.setRowHeight(30);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setShowVerticalLines(false);
        tabela.setGridColor(Tema.LINHA);
        tabela.setSelectionBackground(Tema.AZUL_CLARO);
        tabela.setSelectionForeground(Tema.TINTA);
        tabela.setFillsViewportHeight(true);
        tabela.setAutoCreateRowSorter(true); // clicar no título da coluna ordena
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Renderizador das células: espaçamento, linhas alternadas, hover e destaque animado
        DefaultTableCellRenderer celula = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean selecionada,
                                                           boolean foco, int linha, int coluna) {
                super.getTableCellRendererComponent(t, valor, selecionada, false, linha, coluna);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                if (!selecionada) {
                    Color fundo = linha % 2 == 0 ? Tema.BRANCO : Tema.ZEBRA;
                    if (linha == linhaSobMouse) {
                        fundo = Animador.misturar(fundo, Tema.AZUL_CLARO, 0.45f);
                    }
                    if (idDestacado != null && idDestacado.equals(t.getValueAt(linha, 0))) {
                        fundo = Animador.misturar(fundo, corDestaque, intensidadeDestaque);
                    }
                    setBackground(fundo);
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
                setFont(Tema.FONTE_ROTULO);
                setForeground(Tema.TEXTO_SUAVE);
                setBackground(new Color(247, 247, 249));
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.LINHA),
                        new EmptyBorder(8, 10, 8, 10)));
                return this;
            }
        });

        int[] larguras = {40, 170, 125, 110, 190, 130};
        for (int i = 0; i < larguras.length; i++) {
            tabela.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }

        // Destaque da linha sob o mouse
        MouseAdapter hover = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int linha = tabela.rowAtPoint(e.getPoint());
                if (linha != linhaSobMouse) {
                    linhaSobMouse = linha;
                    tabela.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                linhaSobMouse = -1;
                tabela.repaint();
            }
        };
        tabela.addMouseMotionListener(hover);
        tabela.addMouseListener(hover);

        // Ao selecionar uma linha, carrega o cliente no formulário
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                carregarClienteSelecionado();
            }
        });
    }

    /** Mensagem no meio da tabela quando ainda não há clientes. */
    private void desenharTabelaVazia(Graphics2D g, Rectangle area) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        String linha1 = "Nenhum cliente cadastrado ainda";
        String linha2 = "Preencha o formulário ao lado e clique em Cadastrar.";

        g2.setFont(Tema.FONTE_NEGRITO);
        FontMetrics fm1 = g2.getFontMetrics();
        int y = area.y + area.height / 2 - 10;
        g2.setColor(Tema.TINTA);
        g2.drawString(linha1, area.x + (area.width - fm1.stringWidth(linha1)) / 2, y);

        g2.setFont(Tema.FONTE);
        FontMetrics fm2 = g2.getFontMetrics();
        g2.setColor(Tema.TEXTO_SUAVE);
        g2.drawString(linha2, area.x + (area.width - fm2.stringWidth(linha2)) / 2, y + 22);
        g2.dispose();
    }

    // ---------- Pequenos "construtores" de componentes ----------

    private JPanel criarCard() {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Tema.LINHA, 1, true),
                new EmptyBorder(18, 18, 18, 18)));
        return card;
    }

    /**
     * Cada campo ocupa três linhas do GridBagLayout:
     * rótulo, caixa de texto e mensagem de erro.
     */
    private void adicionarCampo(JPanel painel, Campo tipo, int coluna, int linha, int largura) {
        CampoFormulario campo = campos.get(tipo);

        int margemEsquerda = coluna == 1 ? 6 : 0;
        int margemDireita = (coluna == 0 && largura == 1) ? 6 : 0;

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = coluna;
        c.gridwidth = largura;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;

        c.gridy = linha * 3;
        c.insets = new Insets(linha == 0 ? 0 : 4, margemEsquerda, 4, margemDireita);
        painel.add(campo.getRotulo(), c);

        c.gridy = linha * 3 + 1;
        c.insets = new Insets(0, margemEsquerda, 0, margemDireita);
        painel.add(campo.getCampo(), c);

        c.gridy = linha * 3 + 2;
        c.insets = new Insets(3, margemEsquerda + 2, 0, margemDireita);
        painel.add(campo.getMensagemErro(), c);
    }

    private JButton criarBotao(String texto, Color fundo, Color corTexto, Runnable acao) {
        BotaoAnimado botao = new BotaoAnimado(texto, fundo, corTexto);
        botao.setFont(Tema.FONTE_NEGRITO);
        botao.addActionListener(e -> acao.run());
        return botao;
    }

    // =====================================================================
    //  AÇÕES DOS BOTÕES
    // =====================================================================

    private void enviarFormulario() {
        if (idEmEdicao == null) {
            cadastrar();
        } else {
            salvarEdicao();
        }
    }

    private void cadastrar() {
        if (idEmEdicao != null) {
            Toast.mostrar(this, "Você está editando. Clique em Limpar formulário para cadastrar um novo.",
                    Toast.Tipo.AVISO);
            return;
        }
        try {
            Usuario novo = service.cadastrar(lerFormulario());
            atualizarTabela();
            limparFormulario();
            destacarLinha(novo.getId(), Tema.VERDE_CLARO);
            Toast.mostrar(this, "Cliente cadastrado: " + novo.getNomeCompleto(), Toast.Tipo.SUCESSO);
        } catch (ValidacaoException ex) {
            tratarErro(ex);
        }
    }

    private void salvarEdicao() {
        if (idEmEdicao == null) {
            Toast.mostrar(this, "Selecione um cliente na lista para editar.", Toast.Tipo.AVISO);
            return;
        }
        try {
            Usuario atualizado = service.editar(idEmEdicao, lerFormulario());
            atualizarTabela();
            limparFormulario();
            destacarLinha(atualizado.getId(), Tema.AZUL_DESTAQUE);
            Toast.mostrar(this, "Dados atualizados: " + atualizado.getNomeCompleto(), Toast.Tipo.SUCESSO);
        } catch (ValidacaoException ex) {
            tratarErro(ex);
        }
    }

    private void excluir() {
        if (idEmEdicao == null) {
            Toast.mostrar(this, "Selecione um cliente na lista para excluir.", Toast.Tipo.AVISO);
            return;
        }

        int id = idEmEdicao;
        String nome = service.buscarPorId(id)
                .map(Usuario::getNomeCompleto)
                .orElse("selecionado");

        // Para uma ação destrutiva, a confirmação continua sendo um diálogo que bloqueia a tela
        int resposta = JOptionPane.showConfirmDialog(this,
                "Deseja realmente excluir o cliente " + nome + "?\nEssa ação não pode ser desfeita.",
                "Confirmar exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (resposta != JOptionPane.YES_OPTION) {
            return;
        }

        // Tira a seleção para a cor vermelha aparecer, anima e só então remove de verdade
        tabela.clearSelection();
        apagarLinha(id, () -> {
            try {
                service.excluir(id);
                idDestacado = null;
                atualizarTabela();
                limparFormulario();
                Toast.mostrar(this, "Cliente excluído: " + nome, Toast.Tipo.SUCESSO);
            } catch (ValidacaoException ex) {
                tratarErro(ex);
            }
        });
    }

    private void limparFormulario() {
        campos.values().forEach(CampoFormulario::limpar);
        tabela.clearSelection();
        idEmEdicao = null;
        mudarParaModoNovo();
        campos.get(Campo.NOME).getCampo().requestFocusInWindow();
    }

    // =====================================================================
    //  EFEITOS
    // =====================================================================

    /**
     * Mostra os erros de validação:
     * cada campo errado fica vermelho com sua mensagem, o formulário treme
     * e um toast resume o problema.
     */
    private void tratarErro(ValidacaoException ex) {
        if (!ex.temErrosDeCampo()) {
            Toast.mostrar(this, ex.getMessage(), Toast.Tipo.ERRO);
            return;
        }

        Map<Campo, String> erros = ex.getErrosPorCampo();
        for (Map.Entry<Campo, CampoFormulario> item : campos.entrySet()) {
            String mensagem = erros.get(item.getKey());
            if (mensagem != null) {
                item.getValue().mostrarErro(mensagem);
            } else {
                item.getValue().limparErro(); // campo que estava errado e já foi corrigido
            }
        }

        // Leva o cursor para o primeiro campo com problema
        Campo primeiro = erros.keySet().iterator().next();
        campos.get(primeiro).getCampo().requestFocusInWindow();

        Animador.tremer(cardFormulario);

        int total = erros.size();
        String resumo = total == 1
                ? "Corrija o campo destacado em vermelho."
                : "Corrija os " + total + " campos destacados em vermelho.";
        Toast.mostrar(this, resumo, Toast.Tipo.ERRO);
    }

    /** A linha "acende" com a cor e vai voltando ao normal devagar. */
    private void destacarLinha(int id, Color cor) {
        pararDestaque();
        idDestacado = id;
        corDestaque = cor;
        rolarAteCliente(id);

        timerDestaque = Animador.animar(1800, t -> {
            intensidadeDestaque = 1 - t * t; // fica forte no começo e some mais rápido no fim
            tabela.repaint();
        }, () -> {
            idDestacado = null;
            tabela.repaint();
        });
    }

    /** A linha vai ficando vermelha; ao terminar, executa a ação (a exclusão de fato). */
    private void apagarLinha(int id, Runnable depois) {
        pararDestaque();
        idDestacado = id;
        corDestaque = Tema.VERMELHO_SUAVE;

        timerDestaque = Animador.animar(380, t -> {
            intensidadeDestaque = Animador.suavizar(t);
            tabela.repaint();
        }, depois);
    }

    private void pararDestaque() {
        if (timerDestaque != null) {
            timerDestaque.stop();
            timerDestaque = null;
        }
    }

    private void rolarAteCliente(int id) {
        for (int i = 0; i < tabela.getRowCount(); i++) {
            if (Integer.valueOf(id).equals(tabela.getValueAt(i, 0))) {
                tabela.scrollRectToVisible(tabela.getCellRect(i, 0, true));
                return;
            }
        }
    }

    private void mudarParaModoNovo() {
        labelModo.setText("Novo cadastro");
        labelModo.setForeground(Tema.TEXTO_SUAVE);
        fundoModo.irPara(Tema.CINZA_CLARO);
    }

    private void mudarParaModoEdicao(Usuario u) {
        labelModo.setText("Editando: " + u.getNomeCompleto() + " (ID " + u.getId() + ")");
        labelModo.setForeground(Tema.AZUL);
        fundoModo.irPara(Tema.AZUL_CLARO);
    }

    // =====================================================================
    //  AUXILIARES
    // =====================================================================

    /** Junta o que foi digitado em um DTO para enviar ao service. */
    private UsuarioDTO lerFormulario() {
        return new UsuarioDTO(
                valor(Campo.NOME),
                valor(Campo.CPF),
                valor(Campo.NASCIMENTO),
                valor(Campo.EMAIL),
                valor(Campo.TELEFONE),
                valor(Campo.ENDERECO),
                valor(Campo.SENHA),
                valor(Campo.CONFIRMACAO_SENHA)
        );
    }

    private String valor(Campo campo) {
        return campos.get(campo).getTexto();
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
        campos.get(Campo.NOME).setTexto(u.getNomeCompleto());
        campos.get(Campo.CPF).setTexto(Formatador.cpf(u.getCpf()));
        campos.get(Campo.NASCIMENTO).setTexto(Formatador.data(u.getDataNascimento()));
        campos.get(Campo.EMAIL).setTexto(u.getEmail());
        campos.get(Campo.TELEFONE).setTexto(Formatador.telefone(u.getTelefone()));
        campos.get(Campo.ENDERECO).setTexto(u.getEndereco());
        campos.get(Campo.SENHA).setTexto(u.getSenha());
        campos.get(Campo.CONFIRMACAO_SENHA).setTexto(u.getSenha());
        campos.values().forEach(CampoFormulario::limparErro);

        idEmEdicao = u.getId();
        mudarParaModoEdicao(u);
    }
}
