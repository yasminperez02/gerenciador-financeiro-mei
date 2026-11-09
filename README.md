# Gerenciador Financeiro para MEI

Sistema desktop em Java para controlar o estoque, as vendas e os gastos de um pequeno negócio e acompanhar o lucro.

Projeto de extensão universitária, pensado a partir de pequenos negócios locais.

## Funcionalidades

- **Estoque:** cadastrar, editar e excluir produtos e materiais
- **Vendas:** montar um carrinho, finalizar a venda (com baixa automática no estoque) e ver o histórico
- **Gastos:** registrar despesas por categoria
- **Resumo:** ver receita, gastos e lucro, com filtro por período

## Tecnologias

Java 17+, Swing, JDBC, SQLite e Maven.

## Como executar

Requisitos: JDK 17 ou superior e Maven.

```
git clone https://github.com/yasminperez02/gerenciador-financeiro-mei.git
cd gerenciador-financeiro-mei
mvn compile exec:java
```

Na IDE (IntelliJ, Eclipse ou VS Code), abra o `pom.xml` **como projeto Maven** e execute a classe `gestaomei.Main`.

O banco de dados (`gestao_mei.db`) é criado automaticamente na primeira execução.

## Estrutura

```
src/main/java/gestaomei/
├── Main.java
├── model/   entidades e regras de negócio
├── dao/     acesso ao banco de dados
├── ui/      interface gráfica
└── util/    funções de apoio
```

## Autoria

Desenvolvido por Yasmin Perez.
