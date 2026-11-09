package gestaomei.dao;

import gestaomei.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO {

    public void inserir(Produto p) throws SQLException {
        String sql = "INSERT INTO produto (nome, unidade_medida, quantidade_estoque, "
                + "custo_unitario, preco_venda) VALUES (?, ?, ?, ?, ?)";
        Connection conn = Database.conectar();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, p.getNome());
        ps.setString(2, p.getUnidadeMedida());
        ps.setInt(3, p.getQuantidadeEstoque());
        ps.setDouble(4, p.getCustoUnitario());
        ps.setDouble(5, p.getPrecoVenda());
        ps.executeUpdate();

        ResultSet chaves = ps.getGeneratedKeys();
        if (chaves.next()) {
            p.setId(chaves.getInt(1));
        }
        chaves.close();
        ps.close();
        conn.close();
    }

    public void atualizar(Produto p) throws SQLException {
        String sql = "UPDATE produto SET nome = ?, unidade_medida = ?, quantidade_estoque = ?, "
                + "custo_unitario = ?, preco_venda = ? WHERE id = ?";
        Connection conn = Database.conectar();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, p.getNome());
        ps.setString(2, p.getUnidadeMedida());
        ps.setInt(3, p.getQuantidadeEstoque());
        ps.setDouble(4, p.getCustoUnitario());
        ps.setDouble(5, p.getPrecoVenda());
        ps.setInt(6, p.getId());
        ps.executeUpdate();
        ps.close();
        conn.close();
    }

    /**
     * Exclui o produto. Se ele já aparece em alguma venda, o banco recusa
     * (chave estrangeira) e uma SQLException é lançada.
     */
    public void excluir(int id) throws SQLException {
        Connection conn = Database.conectar();
        PreparedStatement ps = conn.prepareStatement("DELETE FROM produto WHERE id = ?");
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
        conn.close();
    }

    public Produto buscarPorId(int id) throws SQLException {
        Connection conn = Database.conectar();
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM produto WHERE id = ?");
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        Produto p = null;
        if (rs.next()) {
            p = montar(rs);
        }
        rs.close();
        ps.close();
        conn.close();
        return p;
    }

    public List<Produto> listar() throws SQLException {
        List<Produto> lista = new ArrayList<>();
        Connection conn = Database.conectar();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM produto ORDER BY nome");
        while (rs.next()) {
            lista.add(montar(rs));
        }
        rs.close();
        st.close();
        conn.close();
        return lista;
    }

    private Produto montar(ResultSet rs) throws SQLException {
        return new Produto(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("unidade_medida"),
                rs.getInt("quantidade_estoque"),
                rs.getDouble("custo_unitario"),
                rs.getDouble("preco_venda"));
    }
}
