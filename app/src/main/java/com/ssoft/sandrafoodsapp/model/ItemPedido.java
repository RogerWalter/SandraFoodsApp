package com.ssoft.sandrafoodsapp.model;

public class ItemPedido {
    private int id_item;
    private String id_pedido;
    private String desc_item;
    private String adicionais_item;
    private String obs_item;
    private double valor_item;
    private int qtd_item;
    private int grupo_item;

    public ItemPedido() {
    }

    public ItemPedido(int id_item, String id_pedido, String desc_item, String adicionais_item, String obs_item, double valor_item, int qtd_item, int grupo_item) {
        this.id_item = id_item;
        this.id_pedido = id_pedido;
        this.desc_item = desc_item;
        this.adicionais_item = adicionais_item;
        this.obs_item = obs_item;
        this.valor_item = valor_item;
        this.qtd_item = qtd_item;
        this.grupo_item = grupo_item;
    }

    public int getGrupo_item() {
        return grupo_item;
    }

    public void setGrupo_item(int grupo_item) {
        this.grupo_item = grupo_item;
    }

    public int getId_item() {
        return id_item;
    }

    public void setId_item(int id_item) {
        this.id_item = id_item;
    }

    public String getId_pedido() {
        return id_pedido;
    }

    public void setId_pedido(String id_pedido) {
        this.id_pedido = id_pedido;
    }

    public String getDesc_item() {
        return desc_item;
    }

    public void setDesc_item(String desc_item) {
        this.desc_item = desc_item;
    }

    public String getAdicionais_item() {
        return adicionais_item;
    }

    public void setAdicionais_item(String adicionais_item) {
        this.adicionais_item = adicionais_item;
    }

    public String getObs_item() {
        return obs_item;
    }

    public void setObs_item(String obs_item) {
        this.obs_item = obs_item;
    }

    public double getValor_item() {
        return valor_item;
    }

    public void setValor_item(double valor_item) {
        this.valor_item = valor_item;
    }

    public int getQtd_item() {
        return qtd_item;
    }

    public void setQtd_item(int qtd_item) {
        this.qtd_item = qtd_item;
    }
}
