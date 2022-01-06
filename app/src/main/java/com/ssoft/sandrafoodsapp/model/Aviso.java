package com.ssoft.sandrafoodsapp.model;

public class Aviso {
    private int aviso; // 0 - NÃO POSSUI | 1 - POSSUI
    private String data; //data de validade do aviso
    private String imagem;

    public Aviso() {
    }

    public Aviso(int aviso, String data, String imagem) {
        this.aviso = aviso;
        this.data = data;
        this.imagem = imagem;
    }

    public int getAviso() {
        return aviso;
    }

    public void setAviso(int aviso) {
        this.aviso = aviso;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getImagem() {
        return imagem;
    }

    public void setImagem(String imagem) {
        this.imagem = imagem;
    }
}
