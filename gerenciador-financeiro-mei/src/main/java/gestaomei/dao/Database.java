package gestaomei.dao;

import gestaomei.model.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class Database {

    private static String url = "jdbc:sqlite:gestao_mei.db";

    /** Permite trocar o arquivo do banco (útil para testes). */
    public static void setArquivo(String caminho) {
        url = "jdbc:sqlite:" + caminho;
    }

    public static Connection conectar() throws SQLException {
        Connection conn = DriverManager.getConnection(url);
        Statement st = conn.createStatement();
        st.execute("PRAGMA foreign_keys = ON");
        st.close();
        return conn;
    }

    public static void criarTabelas() throws SQLException {
        Connection conn = conectar();
        Statement st = conn.createStatement();

        st.execute("CREATE TABLE IF NOT EXISTS produto ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "nome TEXT NOT NULL, "
                + "unidade_medida TEXT NOT NULL, "
                + "quantidade_estoque INTEGER NOT NULL, "
                + "custo_unitario REAL NOT NULL, "
                + "preco_venda REAL NOT NULL)");

        st.execute("CREATE TABLE IF NOT EXISTS gasto ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "descricao TEXT NOT NULL, "
                + "valor REAL NOT NULL, "
                + "data TEXT NOT NULL, "
                + "categoria TEXT NOT NULL)");

        st.execute("CREATE TABLE IF NOT EXISTS venda ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "data TEXT NOT NULL, "
                + "cliente TEXT)");

        st.execute("CREATE TABLE IF NOT EXISTS item_venda ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "venda_id INTEGER NOT NULL, "
                + "produto_id INTEGER NOT NULL, "
                + "quantidade INTEGER NOT NULL, "
                + "preco_unitario REAL NOT NULL, "
                + "custo_unitario REAL NOT NULL, "
                + "FOREIGN KEY (venda_id) REFERENCES venda(id), "
                + "FOREIGN KEY (produto_id) REFERENCES produto(id))");

        st.close();
        conn.close();
    }
}
