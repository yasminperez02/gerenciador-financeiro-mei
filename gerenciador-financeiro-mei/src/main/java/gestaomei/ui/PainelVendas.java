package gestaomei.ui;

import gestaomei.model.*;
import gestaomei.dao.*;
import gestaomei.util.Util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.table.DefaultTableModel;


public class PainelVendas extends JPanel implements Atualizavel {

    /** Uma linha do carrinho (ainda não gravada no banco). */
    private static class ItemCarrinho {
        Produto produto;
        int quantidade;

        ItemCarrinho(Produto produto, int quantidade) {
            this.produto = produto;
            this.quantidade = quantidade;
        }
    }

    private ProdutoDAO produtoDAO = new ProdutoDAO();
    private VendaDAO vendaDAO = new VendaDAO();

    private List<Produto> produtos = new ArrayList<>();
    private List<ItemCarrinho> carrinho = new ArrayList<>();
    private List<Venda> vendas = new ArrayList<>();

    private JTextField campoCliente = new JTextField();
    private JTextField campoData = new JTextField(Util.formatarData(LocalDate.now()));
    private JComboBox<Produto> comboProduto = new JComboBox<>();
    private JTextField campoQuantidade = new JTextField("1");
    private JLabel rotuloTotalCarrinho = new JLabel("Total do carrinho: R$ 0,00");

    private DefaultTableModel modeloCarrinho;
    private JTable tabelaCarrinho;
    private DefaultTableModel modeloVendas;
    private JTable tabelaVendas;

    public PainelVendas() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ----- Formulário da nova venda -----
        JPanel formulario = new JPanel(new GridLayout(4, 2, 6, 6));
        formulario.add(new JLabel("Cliente (opcional):"));
        formulario.add(campoCliente);
        formulario.add(new JLabel("Data da venda (dd/mm/aaaa):"));
        formulario.add(campoData);
        formulario.add(new JLabel("Produto:"));
        formulario.add(comboProduto);
        formulario.add(new JLabel("Quantidade:"));
        formulario.add(campoQuantidade);

        comboProduto.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                          boolean selecionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, selecionado, foco);
                if (valor instanceof Produto) {
                    Produto p = (Produto) valor;
                    setText(p.getNome() + "  (estoque: " + p.getQuantidadeEstoque() + " "
                            + p.getUnidadeMedida() + " | " + Util.formatarMoeda(p.getPrecoVenda()) + ")");
                }
                return this;
            }
        });

        JButton botaoAdicionar = new JButton("Adicionar ao carrinho");
        JButton botaoRemover = new JButton("Remover item selecionado");
        JButton botaoFinalizar = new JButton("Finalizar venda");

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botoes.add(botaoAdicionar);
        botoes.add(botaoRemover);
        botoes.add(botaoFinalizar);

        JPanel topo = new JPanel(new BorderLayout(0, 8));
        topo.add(formulario, BorderLayout.NORTH);
        topo.add(botoes, BorderLayout.CENTER);
        topo.add(rotuloTotalCarrinho, BorderLayout.SOUTH);

        // ----- Tabela do carrinho -----
        modeloCarrinho = new DefaultTableModel(
                new String[]{"Produto", "Qtd", "Preço unit.", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        tabelaCarrinho = new JTable(modeloCarrinho);
        tabelaCarrinho.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane rolagemCarrinho = new JScrollPane(tabelaCarrinho);
        rolagemCarrinho.setBorder(BorderFactory.createTitledBorder("Carrinho da venda atual"));

        // ----- Histórico -----
        modeloVendas = new DefaultTableModel(
                new String[]{"ID", "Data", "Cliente", "Itens", "Total", "Lucro"}, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        tabelaVendas = new JTable(modeloVendas);
        tabelaVendas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane rolagemVendas = new JScrollPane(tabelaVendas);
        rolagemVendas.setBorder(BorderFactory.createTitledBorder("Histórico de vendas"));

        JButton botaoExcluirVenda = new JButton("Excluir venda selecionada (devolve ao estoque)");
        JPanel painelHistorico = new JPanel(new BorderLayout(0, 6));
        painelHistorico.add(rolagemVendas, BorderLayout.CENTER);
        painelHistorico.add(botaoExcluirVenda, BorderLayout.SOUTH);

        JPanel centro = new JPanel(new GridLayout(2, 1, 0, 10));
        centro.add(rolagemCarrinho);
        centro.add(painelHistorico);

        add(topo, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);

        botaoAdicionar.addActionListener(e -> adicionarAoCarrinho());
        botaoRemover.addActionListener(e -> removerDoCarrinho());
        botaoFinalizar.addActionListener(e -> finalizarVenda());
        botaoExcluirVenda.addActionListener(e -> excluirVendaSelecionada());

        atualizar();
    }

    @Override
    public void atualizar() {
        try {
            produtos = produtoDAO.listar();
            vendas = vendaDAO.listar();
        } catch (SQLException e) {
            mostrarErro("Erro ao carregar dados: " + e.getMessage());
            return;
        }

        // Recarrega o combo de produtos, mantendo o produto escolhido (se ainda existir)
        int idSelecionado = -1;
        Produto atual = (Produto) comboProduto.getSelectedItem();
        if (atual != null) {
            idSelecionado = atual.getId();
        }
        comboProduto.removeAllItems();
        for (int i = 0; i < produtos.size(); i++) {
            comboProduto.addItem(produtos.get(i));
            if (produtos.get(i).getId() == idSelecionado) {
                comboProduto.setSelectedIndex(i);
            }
        }

        // Histórico
        modeloVendas.setRowCount(0);
        for (int i = 0; i < vendas.size(); i++) {
            Venda v = vendas.get(i);
            modeloVendas.addRow(new Object[]{
                    v.getId(),
                    Util.formatarData(v.getData()),
                    v.getCliente() == null ? "" : v.getCliente(),
                    resumirItens(v),
                    Util.formatarMoeda(v.calcularTotal()),
                    Util.formatarMoeda(v.calcularLucro())});
        }
    }

    private String resumirItens(Venda v) {
        String texto = "";
        List<ItemVenda> itens = v.getItens();
        for (int i = 0; i < itens.size(); i++) {
            if (i > 0) {
                texto += ", ";
            }
            texto += itens.get(i).getProduto().getNome() + " x" + itens.get(i).getQuantidade();
        }
        return texto;
    }

    // ---------- Carrinho ----------

    private void adicionarAoCarrinho() {
        Produto produto = (Produto) comboProduto.getSelectedItem();
        if (produto == null) {
            mostrarErro("Cadastre um produto na aba Estoque primeiro.");
            return;
        }
        int quantidade;
        try {
            quantidade = Util.lerInt(campoQuantidade.getText());
        } catch (NumberFormatException e) {
            mostrarErro("Informe a quantidade como um número inteiro.");
            return;
        }
        if (quantidade <= 0) {
            mostrarErro("A quantidade deve ser maior que zero.");
            return;
        }

        // Se o produto já está no carrinho, soma a quantidade na mesma linha
        int posicao = -1;
        for (int i = 0; i < carrinho.size(); i++) {
            if (carrinho.get(i).produto.getId() == produto.getId()) {
                posicao = i;
            }
        }
        int jaNoCarrinho = 0;
        if (posicao >= 0) {
            jaNoCarrinho = carrinho.get(posicao).quantidade;
        }
        if (jaNoCarrinho + quantidade > produto.getQuantidadeEstoque()) {
            mostrarErro("Estoque insuficiente. Disponível: " + produto.getQuantidadeEstoque()
                    + " (já no carrinho: " + jaNoCarrinho + ").");
            return;
        }
        if (posicao >= 0) {
            carrinho.get(posicao).quantidade += quantidade;
        } else {
            carrinho.add(new ItemCarrinho(produto, quantidade));
        }
        campoQuantidade.setText("1");
        redesenharCarrinho();
    }

    private void removerDoCarrinho() {
        int linha = tabelaCarrinho.getSelectedRow();
        if (linha < 0) {
            mostrarErro("Selecione um item do carrinho.");
            return;
        }
        carrinho.remove(linha);
        redesenharCarrinho();
    }

    private void redesenharCarrinho() {
        modeloCarrinho.setRowCount(0);
        double total = 0;
        for (int i = 0; i < carrinho.size(); i++) {
            ItemCarrinho item = carrinho.get(i);
            double subtotal = item.produto.getPrecoVenda() * item.quantidade;
            total += subtotal;
            modeloCarrinho.addRow(new Object[]{
                    item.produto.getNome(),
                    item.quantidade,
                    Util.formatarMoeda(item.produto.getPrecoVenda()),
                    Util.formatarMoeda(subtotal)});
        }
        rotuloTotalCarrinho.setText("Total do carrinho: " + Util.formatarMoeda(total));
    }

    // ---------- Venda ----------

    private void finalizarVenda() {
        if (carrinho.isEmpty()) {
            mostrarErro("O carrinho está vazio.");
            return;
        }
        try {
            LocalDate data = Util.lerData(campoData.getText());
            Venda venda = new Venda(0, data, campoCliente.getText().trim());

            // Busca o produto atualizado no banco para checar o estoque de verdade
            for (int i = 0; i < carrinho.size(); i++) {
                ItemCarrinho item = carrinho.get(i);
                Produto atualizado = produtoDAO.buscarPorId(item.produto.getId());
                if (atualizado == null) {
                    throw new IllegalStateException("Produto não encontrado: " + item.produto.getNome());
                }
                venda.adicionarItem(atualizado, item.quantidade);
            }

            vendaDAO.registrar(venda);

            carrinho.clear();
            redesenharCarrinho();
            campoCliente.setText("");
            campoData.setText(Util.formatarData(LocalDate.now()));
            atualizar();
            JOptionPane.showMessageDialog(this,
                    "Venda registrada!\nTotal: " + Util.formatarMoeda(venda.calcularTotal())
                            + "\nLucro: " + Util.formatarMoeda(venda.calcularLucro()),
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarErro(e.getMessage());
        } catch (SQLException e) {
            mostrarErro("Erro ao registrar a venda: " + e.getMessage());
        }
    }

    private void excluirVendaSelecionada() {
        int linha = tabelaVendas.getSelectedRow();
        if (linha < 0) {
            mostrarErro("Selecione uma venda no histórico.");
            return;
        }
        Venda v = vendas.get(linha);
        int resposta = JOptionPane.showConfirmDialog(this,
                "Excluir a venda #" + v.getId() + "? Os produtos voltam para o estoque.",
                "Confirmar exclusão", JOptionPane.YES_NO_OPTION);
        if (resposta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            vendaDAO.excluir(v.getId());
            atualizar();
        } catch (SQLException e) {
            mostrarErro("Erro ao excluir a venda: " + e.getMessage());
        }
    }

    private void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Atenção", JOptionPane.WARNING_MESSAGE);
    }
}
