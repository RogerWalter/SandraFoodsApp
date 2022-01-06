package com.ssoft.sandrafoodsapp.model;

public class PosicaoMotoboy {
    private Double latitude;
    private Double longitude;

    public PosicaoMotoboy() {
    }

    public PosicaoMotoboy(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
