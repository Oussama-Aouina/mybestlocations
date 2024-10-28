package com.oussamaaouina.mybestlocation;

public class Position {
    public int id;
    public String pseudo;
    public String longitude;
    public String latitude;
    public String numero;

    public Position( int id,String pseudo, String longitude, String latitude, String numero) {
        this.pseudo = pseudo;
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.numero = numero;
    }
    public Position(String pseudo, String longitude, String latitude, String numero) {
        this.pseudo = pseudo;
        this.longitude = longitude;
        this.latitude = latitude;
        this.numero = numero;
    }

    @Override
    public String toString() {
        return "Position{" +
                "id=" + id +
                ", pseudo='" + pseudo + '\'' +
                ", longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                '}';
    }
}
