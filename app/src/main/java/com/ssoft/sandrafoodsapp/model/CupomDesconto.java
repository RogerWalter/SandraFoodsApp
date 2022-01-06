package com.ssoft.sandrafoodsapp.model;

public class CupomDesconto {
    private int id;
    private int tipo; // 1 - PORCENTAGEM | 2 - VALOR EM REAIS
    private String descricao;
    private String validade; //data máxima de validade
    private double valor;
    private double minimo; //valor minimo do pedido para aplicação do cupom
    private int cupomUnico; //usado para cupos que só devem ser utilizados uma vez por dispositivo

    public CupomDesconto() {
    }

    public CupomDesconto(int id, int tipo, String descricao, String validade, double valor, double minimo, int cupomUnico) {
        this.id = id;
        this.tipo = tipo;
        this.descricao = descricao;
        this.validade = validade;
        this.valor = valor;
        this.minimo = minimo;
        this.cupomUnico = cupomUnico;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getValidade() {
        return validade;
    }

    public void setValidade(String validade) {
        this.validade = validade;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public double getMinimo() {
        return minimo;
    }

    public void setMinimo(double minimo) {
        this.minimo = minimo;
    }

    public int getCupomUnico() {
        return cupomUnico;
    }

    public void setCupomUnico(int cupomUnico) {
        this.cupomUnico = cupomUnico;
    }
}

