package gestaomei.model;

public enum CategoriaGasto {
    MATERIA_PRIMA("Matéria-prima"),
    EMBALAGEM("Embalagem"),
    EQUIPAMENTO("Equipamento"),
    TRANSPORTE("Transporte"),
    DIVULGACAO("Divulgação"),
    IMPOSTOS("Impostos (DAS-MEI)"),
    OUTROS("Outros");

    private final String descricao;

    CategoriaGasto(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
