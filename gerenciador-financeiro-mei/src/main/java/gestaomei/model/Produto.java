package gestaomei.model;

public class Produto {

    private int id;
    private String nome;
    private String unidadeMedida;   // ex.: "un", "m", "cone"
    private int quantidadeEstoque;
    private double custoUnitario;   // quanto custa para a MEI produzir/comprar
    private double precoVenda;      // quanto ela cobra do cliente

    public Produto(int id, String nome, String unidadeMedida,
                   int quantidadeEstoque, double custoUnitario, double precoVenda) {
        this.id = id;
        this.nome = nome;
        this.unidadeMedida = unidadeMedida;
        this.quantidadeEstoque = quantidadeEstoque;
        this.custoUnitario = custoUnitario;
        this.precoVenda = precoVenda;
    }

    // ---------- Regras de negócio ----------

    public double calcularLucroUnitario() {
        return precoVenda - custoUnitario;
    }

    public boolean temEstoque(int quantidade) {
        return quantidadeEstoque >= quantidade;
    }

    public void adicionarEstoque(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        quantidadeEstoque += quantidade;
    }

    public void baixarEstoque(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        if (!temEstoque(quantidade)) {
            throw new IllegalStateException("Estoque insuficiente para o produto: " + nome);
        }
        quantidadeEstoque -= quantidade;
    }

    // ---------- Getters e setters ----------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getUnidadeMedida() {
        return unidadeMedida;
    }

    public void setUnidadeMedida(String unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }

    public int getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public void setQuantidadeEstoque(int quantidadeEstoque) {
        this.quantidadeEstoque = quantidadeEstoque;
    }

    public double getCustoUnitario() {
        return custoUnitario;
    }

    public void setCustoUnitario(double custoUnitario) {
        this.custoUnitario = custoUnitario;
    }

    public double getPrecoVenda() {
        return precoVenda;
    }

    public void setPrecoVenda(double precoVenda) {
        this.precoVenda = precoVenda;
    }

    @Override
    public String toString() {
        return "Produto{id=" + id + ", nome='" + nome + "', estoque=" + quantidadeEstoque
                + " " + unidadeMedida + ", custo=R$" + custoUnitario
                + ", preço=R$" + precoVenda + "}";
    }
}
