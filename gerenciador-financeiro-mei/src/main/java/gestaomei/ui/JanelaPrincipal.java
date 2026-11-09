package gestaomei.ui;

import gestaomei.model.*;
import gestaomei.dao.*;
import gestaomei.util.Util;

import java.awt.Component;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;


public class JanelaPrincipal extends JFrame {

    public JanelaPrincipal() {
        super("Gerenciador Financeiro para MEI");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);

        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Estoque", new PainelProdutos());
        abas.addTab("Vendas", new PainelVendas());
        abas.addTab("Gastos", new PainelGastos());
        abas.addTab("Resumo / Lucro", new PainelResumo());

        abas.addChangeListener(e -> {
            Component aba = abas.getSelectedComponent();
            if (aba instanceof Atualizavel) {
                ((Atualizavel) aba).atualizar();
            }
        });

        add(abas);
    }
}
