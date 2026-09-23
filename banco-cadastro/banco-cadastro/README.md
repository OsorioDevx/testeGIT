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
        │   └── ValidacaoException.java        # erro de validação com a lista de problemas
        ├── util/
        │   ├── Validador.java                 # validação de CPF, e-mail, senha, telefone, nome
        │   └── Formatador.java                # formata CPF, telefone e data para exibição
        └── view/
            └── TelaCadastro.java              # interface gráfica (Swing)
```

## Como as partes se conversam

```
TelaCadastro  --(UsuarioDTO)-->  UsuarioService  --(Usuario)-->  UsuarioRepository
   (tela)                        (regras)                         (memória)
      ^                              |
      +----- ValidacaoException -----+   (quando algum dado está errado)
```

Cada camada tem uma única responsabilidade:

- **`TelaCadastro` (view)** monta a janela, lê os campos, chama o service e mostra
  mensagens. Ela não sabe o que é um CPF válido: só pergunta ao service.
- **`UsuarioDTO` (dto)** é um `record` que leva o texto digitado (tudo `String`) da tela
  até o service. Separar isso da entidade evita misturar "dado digitado" com "dado validado".
- **`UsuarioService` (service)** é o coração do sistema. Valida todos os campos de uma vez,
  junta os erros numa lista e, se houver algum, lança `ValidacaoException`. Se estiver tudo
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

Todos os erros aparecem juntos numa única mensagem, para o usuário corrigir tudo de uma vez.

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

| O que testar | Valor | Mensagem esperada |
|---|---|---|
| Campos vazios | Clique em Cadastrar com tudo em branco | Lista de todos os campos obrigatórios |
| CPF inválido | 123.456.789-00 | CPF inválido |
| CPF com dígitos iguais | 111.111.111-11 | CPF inválido |
| CPF repetido | Cadastre o Cliente 1 duas vezes | Já existe um cliente com este CPF |
| E-mail inválido | maria@email ou maria.email.com | E-mail inválido |
| E-mail repetido | MARIA.SOUZA@email.com (com CPF 123.456.789-09) | Já existe um cliente com este e-mail |
| Data impossível | 31/02/1990 | Data de nascimento inválida |
| Data futura | 01/01/2099 | Não pode estar no futuro |
| Menor de idade | 10/05/2012 | Precisa ter pelo menos 18 anos |
| Nome incompleto | Maria | Informe nome e sobrenome |
| Telefone curto | 99123-4567 | Telefone inválido (falta DDD) |
| Senha fraca | 12345678 ou abcdefgh | Mínimo 8 caracteres com letra e número |
| Confirmação diferente | Senha123 / Senha124 | A confirmação não confere |

## Observações importantes (pensando num sistema de verdade)

- **Senha em texto puro:** aqui a senha fica guardada como foi digitada, só para manter o
  exemplo simples. Num sistema real ela é guardada como *hash* (ex.: BCrypt), nunca é exibida
  de volta no formulário e nunca aparece em logs.
- **Dados em memória:** fechou o programa, perdeu os dados. Para persistir, basta criar outra
  implementação do repositório (JDBC com H2/PostgreSQL, por exemplo).
- **CPF válido ≠ CPF existente:** a validação confere só a matemática dos dígitos verificadores.

