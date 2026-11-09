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

public class GastoDAO {

    public void inserir(Gasto g) throws SQLException {
        String sql = "INSERT INTO gasto (descricao, valor, data, categoria) VALUES (?, ?, ?, ?)";
        Connection conn = Database.conectar();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, g.getDescricao());
        ps.setDouble(2, g.getValor());
        ps.setString(3, g.getData().toString());
        ps.setString(4, g.getCategoria().name());
        ps.executeUpdate();

        ResultSet chaves = ps.getGeneratedKeys();
        if (chaves.next()) {
            g.setId(chaves.getInt(1));
        }
        chaves.close();
        ps.close();
        conn.close();
    }

    public void atualizar(Gasto g) throws SQLException {
        String sql = "UPDATE gasto SET descricao = ?, valor = ?, data = ?, categoria = ? WHERE id = ?";
        Connection conn = Database.conectar();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, g.getDescricao());
        ps.setDouble(2, g.getValor());
        ps.setString(3, g.getData().toString());
        ps.setString(4, g.getCategoria().name());
        ps.setInt(5, g.getId());
        ps.executeUpdate();
        ps.close();
        conn.close();
    }

    public void excluir(int id) throws SQLException {
        Connection conn = Database.conectar();
        PreparedStatement ps = conn.prepareStatement("DELETE FROM gasto WHERE id = ?");
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
        conn.close();
    }

    public List<Gasto> listar() throws SQLException {
        List<Gasto> lista = new ArrayList<>();
        Connection conn = Database.conectar();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM gasto ORDER BY data DESC, id DESC");
        while (rs.next()) {
            lista.add(new Gasto(
                    rs.getInt("id"),
                    rs.getString("descricao"),
                    rs.getDouble("valor"),
                    LocalDate.parse(rs.getString("data")),
                    CategoriaGasto.valueOf(rs.getString("categoria"))));
        }
        rs.close();
        st.close();
        conn.close();
        return lista;
    }
}
