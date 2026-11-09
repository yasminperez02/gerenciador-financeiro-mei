package gestaomei.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Venda {

    private int id;
    private LocalDate data;
    private String cliente;
    private List<ItemVenda> itens;

    public Venda(int id, LocalDate data, String cliente) {
        this.id = id;
        this.data = data;
        this.cliente = cliente;
        this.itens = new ArrayList<>();
    }

    // ---------- Regras de negócio ----------

    /**
     * Adiciona um item à venda e já dá baixa no estoque do produto.
     */
    public void adicionarItem(Produto produto, int quantidade) {
        produto.baixarEstoque(quantidade);
        itens.add(new ItemVenda(produto, quantidade));
    }

    /**
     * Usado ao carregar uma venda do banco: apenas inclui o item,
     * sem mexer no estoque (a baixa já aconteceu quando a venda foi feita).
     */
    public void adicionarItemCarregado(ItemVenda item) {
        itens.add(item);
    }

    public double calcularTotal() {
        double total = 0;
        for (int i = 0; i < itens.size(); i++) {
            total += itens.get(i).calcularSubtotal();
        }
        return total;
    }

    public double calcularCustoTotal() {
        double custo = 0;
        for (int i = 0; i < itens.size(); i++) {
            custo += itens.get(i).calcularCusto();
        }
        return custo;
    }

    public double calcularLucro() {
        return calcularTotal() - calcularCustoTotal();
    }

    // ---------- Getters e setters ----------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public List<ItemVenda> getItens() {
        return itens;
    }

    @Override
    public String toString() {
        return "Venda{id=" + id + ", data=" + data + ", cliente='" + cliente
                + "', total=R$" + calcularTotal() + ", lucro=R$" + calcularLucro() + "}";
    }
}
