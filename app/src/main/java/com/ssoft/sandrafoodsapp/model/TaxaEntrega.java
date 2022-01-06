package com.ssoft.sandrafoodsapp.model;

public class TaxaEntrega {
    private int id;
    private String bairro;
    private double valor;

    public TaxaEntrega() {
    }

    public TaxaEntrega(int id, String bairro, double valor) {
        this.id = id;
        this.bairro = bairro;
        this.valor = valor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }
}
