package gestaomei.model;

import java.time.LocalDate;

public class Gasto {

    private int id;
    private String descricao;
    private double valor;
    private LocalDate data;
    private CategoriaGasto categoria;

    public Gasto(int id, String descricao, double valor, LocalDate data, CategoriaGasto categoria) {
        if (valor <= 0) {
            throw new IllegalArgumentException("O valor do gasto deve ser maior que zero.");
        }
        this.id = id;
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        this.categoria = categoria;
    }

    // ---------- Getters e setters ----------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("O valor do gasto deve ser maior que zero.");
        }
        this.valor = valor;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public CategoriaGasto getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaGasto categoria) {
        this.categoria = categoria;
    }

    @Override
    public String toString() {
        return "Gasto{id=" + id + ", descricao='" + descricao + "', valor=R$" + valor
                + ", data=" + data + ", categoria=" + categoria.getDescricao() + "}";
    }
}
