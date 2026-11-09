package gestaomei.ui;

import gestaomei.model.*;
import gestaomei.dao.*;
import gestaomei.util.Util;

import java.awt.BorderLayout;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;


public class PainelGastos extends JPanel implements Atualizavel {

    private GastoDAO dao = new GastoDAO();
    private List<Gasto> gastos = new ArrayList<>();

    private JTextField campoDescricao = new JTextField();
    private JTextField campoValor = new JTextField();
    private JTextField campoData = new JTextField(Util.formatarData(LocalDate.now()));
    private JComboBox<CategoriaGasto> comboCategoria = new JComboBox<>(CategoriaGasto.values());

    private DefaultTableModel modelo;
    private JTable tabela;

    public PainelGastos() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ----- Formulário -----
        JPanel formulario = new JPanel(new GridLayout(4, 2, 6, 6));
        formulario.add(new JLabel("Descrição:"));
        formulario.add(campoDescricao);
        formulario.add(new JLabel("Valor (R$):"));
        formulario.add(campoValor);
        formulario.add(new JLabel("Data (dd/mm/aaaa):"));
        formulario.add(campoData);
        formulario.add(new JLabel("Categoria:"));
        formulario.add(comboCategoria);

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
        String[] colunas = {"ID", "Data", "Descrição", "Categoria", "Valor"};
        modelo = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        tabela = new JTable(modelo);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

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
            gastos = dao.listar();
        } catch (SQLException e) {
            mostrarErro("Erro ao carregar gastos: " + e.getMessage());
            return;
        }
        modelo.setRowCount(0);
        for (int i = 0; i < gastos.size(); i++) {
            Gasto g = gastos.get(i);
            modelo.addRow(new Object[]{
                    g.getId(),
                    Util.formatarData(g.getData()),
                    g.getDescricao(),
                    g.getCategoria().getDescricao(),
                    Util.formatarMoeda(g.getValor())});
        }
    }

    private Gasto lerFormulario(int id) {
        String descricao = campoDescricao.getText().trim();
        if (descricao.isEmpty()) {
            throw new IllegalArgumentException("Informe a descrição do gasto.");
        }
        double valor = Util.lerDouble(campoValor.getText());
        LocalDate data = Util.lerData(campoData.getText());
        CategoriaGasto categoria = (CategoriaGasto) comboCategoria.getSelectedItem();
        return new Gasto(id, descricao, valor, data, categoria);
    }

    private void cadastrar() {
        try {
            Gasto g = lerFormulario(0);
            dao.inserir(g);
            limparCampos();
            atualizar();
        } catch (NumberFormatException e) {
            mostrarErro("Confira o valor digitado.");
        } catch (IllegalArgumentException e) {
            mostrarErro(e.getMessage());
        } catch (SQLException e) {
            mostrarErro("Erro ao salvar: " + e.getMessage());
        }
    }

    private void atualizarSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            mostrarErro("Selecione um gasto na tabela.");
            return;
        }
        try {
            Gasto g = lerFormulario(gastos.get(linha).getId());
            dao.atualizar(g);
            atualizar();
        } catch (NumberFormatException e) {
            mostrarErro("Confira o valor digitado.");
        } catch (IllegalArgumentException e) {
            mostrarErro(e.getMessage());
        } catch (SQLException e) {
            mostrarErro("Erro ao atualizar: " + e.getMessage());
        }
    }

    private void excluirSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            mostrarErro("Selecione um gasto na tabela.");
            return;
        }
        Gasto g = gastos.get(linha);
        int resposta = JOptionPane.showConfirmDialog(this,
                "Excluir o gasto \"" + g.getDescricao() + "\"?", "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION);
        if (resposta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            dao.excluir(g.getId());
            limparCampos();
            atualizar();
        } catch (SQLException e) {
            mostrarErro("Erro ao excluir: " + e.getMessage());
        }
    }

    private void carregarSelecionadoNoFormulario() {
        int linha = tabela.getSelectedRow();
        if (linha < 0 || linha >= gastos.size()) {
            return;
        }
        Gasto g = gastos.get(linha);
        campoDescricao.setText(g.getDescricao());
        campoValor.setText(String.valueOf(g.getValor()).replace(".", ","));
        campoData.setText(Util.formatarData(g.getData()));
        comboCategoria.setSelectedItem(g.getCategoria());
    }

    private void limparCampos() {
        campoDescricao.setText("");
        campoValor.setText("");
        campoData.setText(Util.formatarData(LocalDate.now()));
        comboCategoria.setSelectedIndex(0);
        tabela.clearSelection();
    }

    private void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Atenção", JOptionPane.WARNING_MESSAGE);
    }
}
