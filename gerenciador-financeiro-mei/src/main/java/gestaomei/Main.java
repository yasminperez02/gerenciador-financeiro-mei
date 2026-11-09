package gestaomei;

import gestaomei.dao.Database;
import gestaomei.ui.JanelaPrincipal;

import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Se não der, segue com o visual padrão do Swing
        }

        try {
            Database.criarTabelas();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Não foi possível abrir o banco de dados:\n" + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> new JanelaPrincipal().setVisible(true));
    }
}
