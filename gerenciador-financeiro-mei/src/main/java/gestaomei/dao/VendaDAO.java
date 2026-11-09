package gestaomei.dao;

import gestaomei.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VendaDAO {

    /**
     * Registra a venda. Espera que os produtos dos itens já tenham tido o
     * estoque baixado em memória (é o que Venda.adicionarItem faz);
     * aqui o novo estoque é gravado no banco.
     */
    public void registrar(Venda venda) throws SQLException {
        Connection conn = Database.conectar();
        try {
            conn.setAutoCommit(false);

            PreparedStatement psVenda = conn.prepareStatement(
                    "INSERT INTO venda (data, cliente) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            psVenda.setString(1, venda.getData().toString());
            psVenda.setString(2, venda.getCliente());
            psVenda.executeUpdate();
            ResultSet chaves = psVenda.getGeneratedKeys();
            if (chaves.next()) {
                venda.setId(chaves.getInt(1));
            }
            chaves.close();
            psVenda.close();

            PreparedStatement psItem = conn.prepareStatement(
                    "INSERT INTO item_venda (venda_id, produto_id, quantidade, "
                            + "preco_unitario, custo_unitario) VALUES (?, ?, ?, ?, ?)");
            PreparedStatement psEstoque = conn.prepareStatement(
                    "UPDATE produto SET quantidade_estoque = ? WHERE id = ?");

            List<ItemVenda> itens = venda.getItens();
            for (int i = 0; i < itens.size(); i++) {
                ItemVenda item = itens.get(i);

                psItem.setInt(1, venda.getId());
                psItem.setInt(2, item.getProduto().getId());
                psItem.setInt(3, item.getQuantidade());
                psItem.setDouble(4, item.getPrecoUnitario());
                psItem.setDouble(5, item.getCustoUnitario());
                psItem.executeUpdate();

                psEstoque.setInt(1, item.getProduto().getQuantidadeEstoque());
                psEstoque.setInt(2, item.getProduto().getId());
                psEstoque.executeUpdate();
            }
            psItem.close();
            psEstoque.close();

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.close();
        }
    }

    /**
     * Exclui a venda e devolve ao estoque as quantidades vendidas.
     */
    public void excluir(int vendaId) throws SQLException {
        Connection conn = Database.conectar();
        try {
            conn.setAutoCommit(false);

            PreparedStatement psDevolve = conn.prepareStatement(
                    "UPDATE produto SET quantidade_estoque = quantidade_estoque + "
                            + "(SELECT SUM(quantidade) FROM item_venda WHERE venda_id = ? "
                            + "AND item_venda.produto_id = produto.id) "
                            + "WHERE id IN (SELECT produto_id FROM item_venda WHERE venda_id = ?)");
            psDevolve.setInt(1, vendaId);
            psDevolve.setInt(2, vendaId);
            psDevolve.executeUpdate();
            psDevolve.close();

            PreparedStatement psItens = conn.prepareStatement(
                    "DELETE FROM item_venda WHERE venda_id = ?");
            psItens.setInt(1, vendaId);
            psItens.executeUpdate();
            psItens.close();

            PreparedStatement psVenda = conn.prepareStatement("DELETE FROM venda WHERE id = ?");
            psVenda.setInt(1, vendaId);
            psVenda.executeUpdate();
            psVenda.close();

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.close();
        }
    }

    public List<Venda> listar() throws SQLException {
        // Carrega os produtos uma vez para ligar cada item ao seu produto
        List<Produto> produtos = new ProdutoDAO().listar();

        List<Venda> vendas = new ArrayList<>();
        Connection conn = Database.conectar();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM venda ORDER BY data DESC, id DESC");
        while (rs.next()) {
            vendas.add(new Venda(
                    rs.getInt("id"),
                    LocalDate.parse(rs.getString("data")),
                    rs.getString("cliente")));
        }
        rs.close();
        st.close();

        PreparedStatement ps = conn.prepareStatement("SELECT * FROM item_venda WHERE venda_id = ?");
        for (int i = 0; i < vendas.size(); i++) {
            Venda venda = vendas.get(i);
            ps.setInt(1, venda.getId());
            ResultSet rsItens = ps.executeQuery();
            while (rsItens.next()) {
                int produtoId = rsItens.getInt("produto_id");
                Produto produto = null;
                for (int j = 0; j < produtos.size(); j++) {
                    if (produtos.get(j).getId() == produtoId) {
                        produto = produtos.get(j);
                    }
                }
                venda.adicionarItemCarregado(new ItemVenda(
                        produto,
                        rsItens.getInt("quantidade"),
                        rsItens.getDouble("preco_unitario"),
                        rsItens.getDouble("custo_unitario")));
            }
            rsItens.close();
        }
        ps.close();
        conn.close();
        return vendas;
    }
}
