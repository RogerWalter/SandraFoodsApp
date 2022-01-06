package com.ssoft.sandrafoodsapp.model;

public class Parametros {
    private int entrega;
    private int retirada;
    private String inicio;
    private String fim;
    private int motoboy;
    private int manutencao;
    private int seg;
    private int ter;
    private int qua;
    private int qui;
    private int sex;
    private int sab;
    private int dom;

    public Parametros() {
    }

    public Parametros(int entrega, int retirada, String inicio, String fim, int motoboy, int manutencao, int seg, int ter, int qua, int qui, int sex, int sab, int dom) {
        this.entrega = entrega;
        this.retirada = retirada;
        this.inicio = inicio;
        this.fim = fim;
        this.motoboy = motoboy;
        this.manutencao = manutencao;
        this.seg = seg;
        this.ter = ter;
        this.qua = qua;
        this.qui = qui;
        this.sex = sex;
        this.sab = sab;
        this.dom = dom;
    }

    public int getEntrega() {
        return entrega;
    }

    public void setEntrega(int entrega) {
        this.entrega = entrega;
    }

    public int getRetirada() {
        return retirada;
    }

    public void setRetirada(int retirada) {
        this.retirada = retirada;
    }

    public String getInicio() {
        return inicio;
    }

    public void setInicio(String inicio) {
        this.inicio = inicio;
    }

    public String getFim() {
        return fim;
    }

    public void setFim(String fim) {
        this.fim = fim;
    }

    public int getMotoboy() {
        return motoboy;
    }

    public void setMotoboy(int motoboy) {
        this.motoboy = motoboy;
    }

    public int getManutencao() {
        return manutencao;
    }

    public void setManutencao(int manutencao) {
        this.manutencao = manutencao;
    }

    public int getSeg() {
        return seg;
    }

    public void setSeg(int seg) {
        this.seg = seg;
    }

    public int getTer() {
        return ter;
    }

    public void setTer(int ter) {
        this.ter = ter;
    }

    public int getQua() {
        return qua;
    }

    public void setQua(int qua) {
        this.qua = qua;
    }

    public int getQui() {
        return qui;
    }

    public void setQui(int qui) {
        this.qui = qui;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getSab() {
        return sab;
    }

    public void setSab(int sab) {
        this.sab = sab;
    }

    public int getDom() {
        return dom;
    }

    public void setDom(int dom) {
        this.dom = dom;
    }
}
