# Banco Didático — Cadastro de Clientes (Java + Swing)

Projeto educacional que simula a tela de cadastro de clientes de um banco.
Feito em Java puro (Swing), sem bibliotecas externas e sem banco de dados: os
clientes ficam guardados em memória enquanto o programa está aberto.

## Estrutura de pastas

```
banco-cadastro/
├── README.md
├── run.sh                         # compila e executa (Linux/macOS)
├── run.bat                        # compila e executa (Windows)
└── src/
    └── br/com/banco/
        ├── Main.java                          # ponto de entrada: monta as camadas e abre a tela
        ├── model/
        │   └── Usuario.java                   # entidade (os dados de um cliente)
        ├── dto/
        │   └── UsuarioDTO.java                # dados crus digitados no formulário
        ├── repository/
        │   └── UsuarioRepository.java         # armazenamento em memória
        ├── service/
        │   ├── UsuarioService.java            # regras de negócio e validações
        │   ├── ValidacaoException.java        # erro de validação: qual campo + mensagem
        │   └── Campo.java                     # enum que identifica cada campo do formulário
        ├── util/
        │   ├── Validador.java                 # validação de CPF, e-mail, senha, telefone, nome
        │   └── Formatador.java                # formata CPF, telefone e data para exibição
        └── view/
            ├── TelaCadastro.java              # interface gráfica (Swing)
            ├── Tema.java                      # cores, fontes e liga/desliga das animações
            └── componentes/
                ├── Animador.java              # motor das animações (Timer + easing + tremor)
                ├── TransicaoDeCor.java        # leva uma cor suavemente até outra
                ├── BotaoAnimado.java          # botão arredondado com hover/clique animados
                ├── CampoFormulario.java       # rótulo + campo + mensagem de erro animada
                └── Toast.java                 # notificação que desliza no canto da tela
```

## Como as partes se conversam

```
TelaCadastro  --(UsuarioDTO)-->  UsuarioService  --(Usuario)-->  UsuarioRepository
   (tela)                        (regras)                         (memória)
      ^                              |
      +----- ValidacaoException -----+   (quando algum dado está errado: diz o campo e o motivo)
```

Cada camada tem uma única responsabilidade:

- **`TelaCadastro` (view)** monta a janela, lê os campos, chama o service e mostra
  mensagens. Ela não sabe o que é um CPF válido: só pergunta ao service.
- **`UsuarioDTO` (dto)** é um `record` que leva o texto digitado (tudo `String`) da tela
  até o service. Separar isso da entidade evita misturar "dado digitado" com "dado validado".
- **`UsuarioService` (service)** é o coração do sistema. Valida todos os campos de uma vez,
  junta os erros num mapa `Campo -> mensagem` e, se houver algum, lança `ValidacaoException`.
  É esse mapa que permite à tela destacar exatamente os campos errados. Se estiver tudo
  certo, converte o DTO num `Usuario` (ex.: texto da data vira `LocalDate`, CPF fica só com
  números) e manda salvar. Também impede CPF e e-mail repetidos.
- **`UsuarioRepository` (repository)** guarda os clientes num `LinkedHashMap<Integer, Usuario>`
  e gera o ID automaticamente. Se um dia você trocar por um banco de dados, só essa classe muda.
- **`Usuario` (model)** é a entidade: só atributos, construtor, getters e setters.
- **`Validador` e `Formatador` (util)** são funções estáticas reutilizáveis. O cálculo dos
  dígitos verificadores do CPF está comentado passo a passo no `Validador`.
- **`Main`** cria repository → service → tela, nessa ordem (injeção de dependência "na mão"),
  e abre a janela dentro da Event Dispatch Thread, como o Swing exige.

### Funcionalidades

| Ação | Como usar |
|---|---|
| Cadastrar | Preencha o formulário e clique em **Cadastrar** |
| Listar | A tabela à direita mostra todos os clientes e se atualiza sozinha. Clique no título de uma coluna para ordenar |
| Editar | Clique num cliente da tabela, altere os campos e clique em **Salvar alterações** |
| Excluir | Clique num cliente da tabela e em **Excluir cliente** (pede confirmação) |
| Limpar | **Limpar formulário** zera os campos e volta para o modo "Novo cadastro" |

O texto abaixo de "Dados do cliente" indica o modo atual: **Novo cadastro** ou
**Editando: Nome (ID x)**.

### Validações implementadas

| Campo | Regra |
|---|---|
| Todos | Obrigatórios |
| Nome completo | Pelo menos nome e sobrenome, só letras (aceita acentos, hífen e apóstrofo) |
| CPF | 11 dígitos, dígitos verificadores corretos, não pode ser tudo igual (111.111.111-11), não pode repetir |
| Nascimento | Formato dd/mm/aaaa, data real (31/02 é recusado), não pode ser futura, idade mínima de 18 anos |
| E-mail | Formato `nome@dominio.com`, não pode repetir |
| Telefone | DDD + número: 10 ou 11 dígitos (aceita com ou sem pontuação) |
| Endereço | Mínimo de 10 caracteres |
| Senha | Mínimo 8 caracteres, pelo menos 1 letra e 1 número, sem espaços; confirmação precisa bater |

Todos os erros aparecem de uma vez, cada um embaixo do seu campo. Ao começar a digitar num
campo com erro, o destaque some sozinho. Apertar **Enter** em qualquer campo cadastra (ou salva, no modo edição).

## Efeitos e animações

| Onde | O que acontece |
|---|---|
| Botões | A cor escurece suavemente ao passar o mouse e mais um pouco ao clicar. Com a tecla Tab aparece um anel de foco |
| Campos | Borda fica azul ao focar. Com erro: borda vermelha, fundo rosado e a mensagem surge embaixo com fade-in |
| Formulário | Treme para os lados quando há erro de validação, como quem diz "não" |
| Notificações | Sucesso, erro e aviso aparecem num "toast" que sobe no canto inferior direito, fica 3 s e some (ou some ao ser clicado). Não bloqueiam a tela como o `JOptionPane` |
| Tabela | Linha nova brilha em verde, linha editada brilha em azul, e o brilho vai apagando. Ao excluir, a linha fica vermelha e só então desaparece. A linha sob o mouse fica levemente destacada |
| Etiqueta de modo | "Novo cadastro" (cinza) muda para "Editando: ..." (azul) com transição de cor |
| Tabela vazia | Mostra uma mensagem orientando o que fazer |

**Como as animações funcionam.** O Swing não tem um sistema de animação pronto, então o
`Animador` usa um `javax.swing.Timer` que dispara a cada ~15 ms (cerca de 60 quadros por segundo).
A cada disparo ele calcula o progresso `t` de 0.0 a 1.0 e a tela atualiza algo com base nele:
uma cor (`Animador.misturar`), uma posição (o tremor) ou uma transparência (o toast, com `AlphaComposite`).
A função `suavizar` aplica um *easing* para o movimento começar rápido e desacelerar no fim, o que parece
mais natural. O `Timer` é usado em vez de `Thread.sleep` porque roda na Event Dispatch Thread, a única
que pode mexer em componentes Swing.

**Quer desligar?** Mude `ANIMACOES_ATIVADAS` para `false` em `Tema.java`. Tudo continua funcionando,
só que as mudanças acontecem na hora. É um jeito bom de comparar o antes e depois, e também de
respeitar quem prefere menos movimento na tela.

A confirmação de exclusão continua sendo um diálogo (`JOptionPane`) de propósito: em ações destrutivas,
é bom obrigar o usuário a parar e confirmar.

## Como executar

**Pré-requisito:** JDK 17 ou superior (o projeto usa `record`). Confira com `java -version` e `javac -version`.

### Opção 1: script pronto

- **Windows:** dê dois cliques em `run.bat` (ou rode `run.bat` no terminal, dentro da pasta).
- **Linux/macOS:** `./run.sh` (se precisar, antes: `chmod +x run.sh`).

### Opção 2: comandos manuais (dentro da pasta `banco-cadastro`)

```bash
javac -encoding UTF-8 -d out -sourcepath src src/br/com/banco/Main.java
java -cp out br.com.banco.Main
```

No Windows, troque `/` por `\` no caminho do `Main.java`. O `-sourcepath src` faz o
`javac` encontrar e compilar sozinho todas as outras classes. O `-encoding UTF-8` garante
que os acentos apareçam certos (importante no Windows com Java 17).

### Opção 3: IDE (IntelliJ, Eclipse, VS Code, NetBeans)

1. Abra a pasta `banco-cadastro` como projeto.
2. Marque a pasta `src` como *Sources Root* (no IntelliJ: botão direito → *Mark Directory as* → *Sources Root*).
3. Abra `Main.java` e clique em *Run*.

## Dados para testar

### Cadastros válidos

Os CPFs abaixo são matematicamente válidos e muito usados em testes (não são de pessoas reais).

| Campo | Cliente 1 | Cliente 2 | Cliente 3 |
|---|---|---|---|
| Nome completo | Maria Fernanda Souza | João Pedro D'Ávila | Ana Beatriz Lima-Costa |
| CPF | 529.982.247-25 | 11144477735 | 935.411.347-80 |
| Nascimento | 15/03/1995 | 02/11/1988 | 29/02/2000 |
| E-mail | maria.souza@email.com | joao.pedro@banco.com.br | ana.lima@teste.com |
| Telefone | (92) 99123-4567 | 9233214567 | 11 98765-4321 |
| Endereço | Rua das Flores, 120 - Adrianópolis, Manaus/AM | Av. Djalma Batista, 1500 - Chapada | Rua Augusta, 900, ap. 12 - Consolação, São Paulo/SP |
| Senha / Confirmar | Senha123 | Banco2024 | Segura2026 |

Repare que o Cliente 2 usa CPF e telefone sem pontuação e o sistema aceita e formata
sozinho. O Cliente 3 nasceu em 29/02 de um ano bissexto, que é uma data válida.

### Casos para ver os erros

| O que testar | Valor | Mensagem embaixo do campo |
|---|---|---|
| Campos vazios | Clique em Cadastrar com tudo em branco | "Informe o ..." em cada campo |
| CPF inválido | 123.456.789-00 | CPF inválido. |
| CPF com dígitos iguais | 111.111.111-11 | CPF inválido. |
| CPF repetido | Cadastre o Cliente 1 duas vezes | CPF já cadastrado. |
| E-mail inválido | maria@email ou maria.email.com | E-mail inválido. |
| E-mail repetido | MARIA.SOUZA@email.com (com CPF 123.456.789-09) | E-mail já cadastrado. |
| Data impossível | 31/02/1990 | Data inválida. |
| Data futura | 01/01/2099 | A data não pode ser futura. |
| Menor de idade | 10/05/2012 | Idade mínima: 18 anos. |
| Nome incompleto | Maria | Informe nome e sobrenome, usando apenas letras. |
| Telefone curto | 99123-4567 | Informe DDD + número. |
| Senha fraca | 12345678 ou abcdefgh | Mín. 8, com letras e números. |
| Confirmação diferente | Senha123 / Senha124 | As senhas não conferem. |

Dica: preencha tudo errado de uma vez para ver vários campos ficarem vermelhos e o formulário tremer.
Depois vá corrigindo e repare o vermelho sumindo em cada campo.

## Observações importantes (pensando num sistema de verdade)

- **Senha em texto puro:** aqui a senha fica guardada como foi digitada, só para manter o
  exemplo simples. Num sistema real ela é guardada como *hash* (ex.: BCrypt), nunca é exibida
  de volta no formulário e nunca aparece em logs.
- **Dados em memória:** fechou o programa, perdeu os dados. Para persistir, basta criar outra
  implementação do repositório (JDBC com H2/PostgreSQL, por exemplo).
- **CPF válido ≠ CPF existente:** a validação confere só a matemática dos dígitos verificadores.

## Ideias para evoluir o projeto

1. Criar testes com JUnit para `Validador` e `UsuarioService` (são classes sem tela, fáceis de testar).
2. Transformar `UsuarioRepository` numa interface com duas implementações: memória e banco de dados.
3. Guardar a senha com hash e, na edição, deixar o campo de senha vazio para "manter a atual".
4. Adicionar um campo de busca por nome ou CPF acima da tabela.
5. Reaproveitar o `UsuarioService` numa API REST com Spring Boot, trocando só a camada de tela.
