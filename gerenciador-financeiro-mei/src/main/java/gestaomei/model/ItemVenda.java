package gestaomei.model;

public class ItemVenda {

    private Produto produto;
    private int quantidade;
    private double precoUnitario;
    private double custoUnitario;

    public ItemVenda(Produto produto, int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = produto.getPrecoVenda();
        this.custoUnitario = produto.getCustoUnitario();
    }

    /**
     * Construtor usado ao carregar uma venda antiga do banco de dados:
     * mantém o preço e o custo que valiam na data da venda.
     */
    public ItemVenda(Produto produto, int quantidade, double precoUnitario, double custoUnitario) {
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.custoUnitario = custoUnitario;
    }

    // ---------- Regras de negócio ----------

    public double calcularSubtotal() {
        return precoUnitario * quantidade;
    }

    public double calcularCusto() {
        return custoUnitario * quantidade;
    }

    public double calcularLucro() {
        return calcularSubtotal() - calcularCusto();
    }

    // ---------- Getters ----------

    public Produto getProduto() {
        return produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public double getPrecoUnitario() {
        return precoUnitario;
    }

    public double getCustoUnitario() {
        return custoUnitario;
    }

    @Override
    public String toString() {
        return produto.getNome() + " x" + quantidade + " = R$" + calcularSubtotal();
    }
}
