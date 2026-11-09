package gestaomei.ui;

import gestaomei.model.*;
import gestaomei.dao.*;
import gestaomei.util.Util;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class PainelResumo extends JPanel implements Atualizavel {

    private VendaDAO vendaDAO = new VendaDAO();
    private GastoDAO gastoDAO = new GastoDAO();

    private JTextField campoDe = new JTextField(10);
    private JTextField campoAte = new JTextField(10);

    private JLabel valorReceita = criarValor();
    private JLabel valorCustoVendido = criarValor();
    private JLabel valorLucroVendas = criarValor();
    private JLabel valorGastos = criarValor();
    private JLabel valorSaldo = criarValor();

    private DefaultTableModel modeloCategorias;

    public PainelResumo() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ----- Filtro de período -----
        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtro.add(new JLabel("De (dd/mm/aaaa):"));
        filtro.add(campoDe);
        filtro.add(new JLabel("Até:"));
        filtro.add(campoAte);
        JButton botaoCalcular = new JButton("Calcular");
        JButton botaoTudo = new JButton("Ver tudo");
        filtro.add(botaoCalcular);
        filtro.add(botaoTudo);
        add(filtro, BorderLayout.NORTH);

        // ----- Números principais -----
        JPanel numeros = new JPanel(new GridLayout(5, 2, 6, 10));
        numeros.setBorder(BorderFactory.createTitledBorder("Resumo do período"));
        numeros.add(new JLabel("Total vendido (receita):"));
        numeros.add(valorReceita);
        numeros.add(new JLabel("Custo dos produtos vendidos:"));
        numeros.add(valorCustoVendido);
        numeros.add(new JLabel("LUCRO DAS VENDAS (receita − custo dos produtos):"));
        numeros.add(valorLucroVendas);
        numeros.add(new JLabel("Total de gastos registrados:"));
        numeros.add(valorGastos);
        numeros.add(new JLabel("SALDO DO CAIXA (receita − gastos):"));
        numeros.add(valorSaldo);

        // ----- Gastos por categoria -----
        modeloCategorias = new DefaultTableModel(new String[]{"Categoria", "Total gasto"}, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        JScrollPane rolagem = new JScrollPane(new JTable(modeloCategorias));
        rolagem.setBorder(BorderFactory.createTitledBorder("Gastos por categoria"));

        JPanel centro = new JPanel(new GridLayout(1, 2, 10, 0));
        centro.add(numeros);
        centro.add(rolagem);
        add(centro, BorderLayout.CENTER);

        JLabel nota = new JLabel("<html><i>Lucro das vendas usa o custo unitário cadastrado em cada produto. "
                + "Saldo do caixa subtrai todos os gastos lançados. Se você lança a compra de material "
                + "como gasto <b>e</b> também informa o custo unitário do produto, use o saldo do caixa "
                + "para não contar o mesmo custo duas vezes.</i></html>");
        add(nota, BorderLayout.SOUTH);

        botaoCalcular.addActionListener(e -> atualizar());
        botaoTudo.addActionListener(e -> {
            campoDe.setText("");
            campoAte.setText("");
            atualizar();
        });

        atualizar();
    }

    private JLabel criarValor() {
        JLabel rotulo = new JLabel("R$ 0,00");
        rotulo.setFont(rotulo.getFont().deriveFont(Font.BOLD, 15f));
        return rotulo;
    }

    private boolean dentroDoPeriodo(LocalDate data, LocalDate de, LocalDate ate) {
        if (de != null && data.isBefore(de)) {
            return false;
        }
        if (ate != null && data.isAfter(ate)) {
            return false;
        }
        return true;
    }

    @Override
    public void atualizar() {
        LocalDate de = null;
        LocalDate ate = null;
        try {
            if (!campoDe.getText().trim().isEmpty()) {
                de = Util.lerData(campoDe.getText());
            }
            if (!campoAte.getText().trim().isEmpty()) {
                ate = Util.lerData(campoAte.getText());
            }
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Venda> vendas;
        List<Gasto> gastos;
        try {
            vendas = vendaDAO.listar();
            gastos = gastoDAO.listar();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados: " + e.getMessage(),
                    "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double receita = 0;
        double custoVendido = 0;
        for (int i = 0; i < vendas.size(); i++) {
            Venda v = vendas.get(i);
            if (dentroDoPeriodo(v.getData(), de, ate)) {
                receita += v.calcularTotal();
                custoVendido += v.calcularCustoTotal();
            }
        }

        double totalGastos = 0;
        CategoriaGasto[] categorias = CategoriaGasto.values();
        double[] porCategoria = new double[categorias.length];
        for (int i = 0; i < gastos.size(); i++) {
            Gasto g = gastos.get(i);
            if (dentroDoPeriodo(g.getData(), de, ate)) {
                totalGastos += g.getValor();
                porCategoria[g.getCategoria().ordinal()] += g.getValor();
            }
        }

        valorReceita.setText(Util.formatarMoeda(receita));
        valorCustoVendido.setText(Util.formatarMoeda(custoVendido));
        valorLucroVendas.setText(Util.formatarMoeda(receita - custoVendido));
        valorGastos.setText(Util.formatarMoeda(totalGastos));
        valorSaldo.setText(Util.formatarMoeda(receita - totalGastos));

        modeloCategorias.setRowCount(0);
        for (int i = 0; i < categorias.length; i++) {
            if (porCategoria[i] > 0) {
                modeloCategorias.addRow(new Object[]{
                        categorias[i].getDescricao(), Util.formatarMoeda(porCategoria[i])});
            }
        }
    }
}
