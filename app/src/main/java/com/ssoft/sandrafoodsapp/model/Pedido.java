package com.ssoft.sandrafoodsapp.model;

public class Pedido {
    private String id;
    private String clt_nome;
    private String clt_celular;
    private String clt_rua;
    private String clt_numero;
    private String clt_bairro;
    private String clt_referencia;
    private Double valor;
    private String data;
    private String observacao;
    private String pagamento;
    private String tipo;
    private Double desconto;
    private String troco;

    public Pedido() {
    }

    public Pedido(String id, String clt_nome, String clt_celular, String clt_rua, String clt_numero, String clt_bairro, String clt_referencia, Double valor, String data, String observacao, String pagamento, String tipo, Double desconto, String troco) {
        this.id = id;
        this.clt_nome = clt_nome;
        this.clt_celular = clt_celular;
        this.clt_rua = clt_rua;
        this.clt_numero = clt_numero;
        this.clt_bairro = clt_bairro;
        this.clt_referencia = clt_referencia;
        this.valor = valor;
        this.data = data;
        this.observacao = observacao;
        this.pagamento = pagamento;
        this.tipo = tipo;
        this.desconto = desconto;
        this.troco = troco;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClt_nome() {
        return clt_nome;
    }

    public void setClt_nome(String clt_nome) {
        this.clt_nome = clt_nome;
    }

    public String getClt_celular() {
        return clt_celular;
    }

    public void setClt_celular(String clt_celular) {
        this.clt_celular = clt_celular;
    }

    public String getClt_rua() {
        return clt_rua;
    }

    public void setClt_rua(String clt_rua) {
        this.clt_rua = clt_rua;
    }

    public String getClt_numero() {
        return clt_numero;
    }

    public void setClt_numero(String clt_numero) {
        this.clt_numero = clt_numero;
    }

    public String getClt_bairro() {
        return clt_bairro;
    }

    public void setClt_bairro(String clt_bairro) {
        this.clt_bairro = clt_bairro;
    }

    public String getClt_referencia() {
        return clt_referencia;
    }

    public void setClt_referencia(String clt_referencia) {
        this.clt_referencia = clt_referencia;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public String getPagamento() {
        return pagamento;
    }

    public void setPagamento(String pagamento) {
        this.pagamento = pagamento;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Double getDesconto() {
        return desconto;
    }

    public void setDesconto(Double desconto) {
        this.desconto = desconto;
    }

    public String getTroco() {
        return troco;
    }

    public void setTroco(String troco) {
        this.troco = troco;
    }
}
