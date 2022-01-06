package com.ssoft.sandrafoodsapp.model;

public class Andamento {
    private String id;
    private String status;
    private String data;
    private String cliente;
    private String valor;
    private String endereco;
    private String enderecoPesquisa;


    public Andamento() {
    }

    public Andamento(String id, String status, String data, String cliente, String valor, String endereco, String enderecoPesquisa) {
        this.id = id;
        this.status = status;
        this.data = data;
        this.cliente = cliente;
        this.valor = valor;
        this.endereco = endereco;
        this.enderecoPesquisa = enderecoPesquisa;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getEnderecoPesquisa() {
        return enderecoPesquisa;
    }

    public void setEnderecoPesquisa(String enderecoPesquisa) {
        this.enderecoPesquisa = enderecoPesquisa;
    }
}
