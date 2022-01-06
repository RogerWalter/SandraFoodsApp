package com.ssoft.sandrafoodsapp.model;

public class ItemCardapio {

    /*
    IMPORTANTE: SE FOR DO GRUPO 0, É ADICIONAL.
    SE FOR ADICIONAL, O TIPO PODE SER:
    9 -> ADICIONAL DE LANCHES
    8 -> ADICIONAL DE PASTÉIS
    7 -> ADICIONAL DE PORÇÕES
     */

    private int id;
    private int grupo;
    private int tipo; //1 - salgado ou 2 - doce
    private String nome;
    private String descricao;
    private Double valor;

    public ItemCardapio() {
    }

    public ItemCardapio(int id, int grupo, int tipo, String nome, String descricao, Double valor) {
        this.id = id;
        this.grupo = grupo;
        this.tipo = tipo;
        this.nome = nome;
        this.descricao = descricao;
        this.valor = valor;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGrupo() {
        return grupo;
    }

    public void setGrupo(int grupo) {
        this.grupo = grupo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }
}
