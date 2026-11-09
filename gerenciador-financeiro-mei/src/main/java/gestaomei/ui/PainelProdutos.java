package gestaomei.ui;

import gestaomei.model.*;
import gestaomei.dao.*;
import gestaomei.util.Util;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class PainelProdutos extends JPanel implements Atualizavel {

    private ProdutoDAO dao = new ProdutoDAO();
    private List<Produto> produtos = new ArrayList<>();

    private JTextField campoNome = new JTextField();
    private JTextField campoUnidade = new JTextField("un");
    private JTextField campoEstoque = new JTextField("0");
    private JTextField campoCusto = new JTextField();
    private JTextField campoPreco = new JTextField();

    private DefaultTableModel modelo;
    private JTable tabela;

    public PainelProdutos() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ----- Formulário -----
        JPanel formulario = new JPanel(new GridLayout(5, 2, 6, 6));
        formulario.add(new JLabel("Nome do produto / material:"));
        formulario.add(campoNome);
        formulario.add(new JLabel("Unidade (un, m, cone...):"));
        formulario.add(campoUnidade);
        formulario.add(new JLabel("Quantidade em estoque:"));
        formulario.add(campoEstoque);
        formulario.add(new JLabel("Custo unitário (R$):"));
        formulario.add(campoCusto);
        formulario.add(new JLabel("Preço de venda (R$):"));
        formulario.add(campoPreco);

        JButton botaoSalvar = new JButton("Cadastrar novo");
        JButton botaoAtualizar = new JButton("Atualizar selecionado");
        JButton botaoExcluir = new JButton("Excluir selecionado");
        JButton botaoLimpar = new JButton("Limpar campos");

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botoes.add(botaoSalvar);
        botoes.add(botaoAtualizar);
        botoes.add(botaoExcluir);
        botoes.add(botaoLimpar);

        JPanel topo = new JPanel(new BorderLayout(0, 8));
        topo.add(formulario, BorderLayout.CENTER);
        topo.add(botoes, BorderLayout.SOUTH);
        add(topo, BorderLayout.NORTH);

        // ----- Tabela -----
        String[] colunas = {"ID", "Nome", "Unidade", "Estoque", "Custo", "Preço", "Lucro unit."};
        modelo = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        tabela = new JTable(modelo);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        // Ao clicar numa linha, os dados vão para o formulário
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                carregarSelecionadoNoFormulario();
            }
        });

        botaoSalvar.addActionListener(e -> cadastrar());
        botaoAtualizar.addActionListener(e -> atualizarSelecionado());
        botaoExcluir.addActionListener(e -> excluirSelecionado());
        botaoLimpar.addActionListener(e -> limparCampos());

        atualizar();
    }

    @Override
    public void atualizar() {
        try {
            produtos = dao.listar();
        } catch (SQLException e) {
            mostrarErro("Erro ao carregar produtos: " + e.getMessage());
            return;
        }
        modelo.setRowCount(0);
        for (int i = 0; i < produtos.size(); i++) {
            Produto p = produtos.get(i);
            modelo.addRow(new Object[]{
                    p.getId(),
                    p.getNome(),
                    p.getUnidadeMedida(),
                    p.getQuantidadeEstoque(),
                    Util.formatarMoeda(p.getCustoUnitario()),
                    Util.formatarMoeda(p.getPrecoVenda()),
                    Util.formatarMoeda(p.calcularLucroUnitario())});
        }
    }

    private Produto lerFormulario(int id) {
        String nome = campoNome.getText().trim();
        if (nome.isEmpty()) {
            throw new IllegalArgumentException("Informe o nome do produto.");
        }
        String unidade = campoUnidade.getText().trim();
        if (unidade.isEmpty()) {
            unidade = "un";
        }
        int estoque = Util.lerInt(campoEstoque.getText());
        double custo = Util.lerDouble(campoCusto.getText());
        double preco = Util.lerDouble(campoPreco.getText());
        if (estoque < 0 || custo < 0 || preco < 0) {
            throw new IllegalArgumentException("Os valores não podem ser negativos.");
        }
        return new Produto(id, nome, unidade, estoque, custo, preco);
    }

    private void cadastrar() {
        try {
            Produto p = lerFormulario(0);
            dao.inserir(p);
            limparCampos();
            atualizar();
        } catch (NumberFormatException e) {
            mostrarErro("Confira os números digitados (estoque, custo e preço).");
        } catch (IllegalArgumentException e) {
            mostrarErro(e.getMessage());
        } catch (SQLException e) {
            mostrarErro("Erro ao salvar: " + e.getMessage());
        }
    }

    private void atualizarSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            mostrarErro("Selecione um produto na tabela.");
            return;
        }
        try {
            Produto p = lerFormulario(produtos.get(linha).getId());
            dao.atualizar(p);
            atualizar();
        } catch (NumberFormatException e) {
            mostrarErro("Confira os números digitados (estoque, custo e preço).");
        } catch (IllegalArgumentException e) {
            mostrarErro(e.getMessage());
        } catch (SQLException e) {
            mostrarErro("Erro ao atualizar: " + e.getMessage());
        }
    }

    private void excluirSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            mostrarErro("Selecione um produto na tabela.");
            return;
        }
        Produto p = produtos.get(linha);
        int resposta = JOptionPane.showConfirmDialog(this,
                "Excluir o produto \"" + p.getNome() + "\"?", "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION);
        if (resposta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            dao.excluir(p.getId());
            limparCampos();
            atualizar();
        } catch (SQLException e) {
            mostrarErro("Não foi possível excluir. Esse produto provavelmente já foi usado em "
                    + "uma venda.\nDetalhe: " + e.getMessage());
        }
    }

    private void carregarSelecionadoNoFormulario() {
        int linha = tabela.getSelectedRow();
        if (linha < 0 || linha >= produtos.size()) {
            return;
        }
        Produto p = produtos.get(linha);
        campoNome.setText(p.getNome());
        campoUnidade.setText(p.getUnidadeMedida());
        campoEstoque.setText(String.valueOf(p.getQuantidadeEstoque()));
        campoCusto.setText(String.valueOf(p.getCustoUnitario()).replace(".", ","));
        campoPreco.setText(String.valueOf(p.getPrecoVenda()).replace(".", ","));
    }

    private void limparCampos() {
        campoNome.setText("");
        campoUnidade.setText("un");
        campoEstoque.setText("0");
        campoCusto.setText("");
        campoPreco.setText("");
        tabela.clearSelection();
    }

    private void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Atenção", JOptionPane.WARNING_MESSAGE);
    }
}
