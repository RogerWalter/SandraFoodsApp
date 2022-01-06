package com.ssoft.sandrafoodsapp.model;

public class Cliente {

    private int id;
    private String nome;
    private String celular;
    private String rua;
    private String numero;
    private String bairro;
    private String referencia;

    public Cliente() {
    }

    public Cliente(int id, String nome, String celular, String rua, String numero, String bairro, String referencia) {
        this.id = id;
        this.nome = nome;
        this.celular = celular;
        this.rua = rua;
        this.numero = numero;
        this.bairro = bairro;
        this.referencia = referencia;
    }

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

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }
}
