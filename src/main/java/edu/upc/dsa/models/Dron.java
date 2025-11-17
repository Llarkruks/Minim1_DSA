package edu.upc.dsa.models;

import io.swagger.annotations.ApiModelProperty;

public class Dron {
    String id;
    String nombre;
    String fabricante;
    String modelo;
    double horasVuelo;
    boolean operativo = true;

    public Dron() {}
    public Dron(String id, String nombre, String fabricante, String modelo) {
        this.id = id; this.nombre = nombre; this.fabricante = fabricante; this.modelo = modelo;
    }

    @ApiModelProperty(example = "D1")
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @ApiModelProperty(example = "Eagle")
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    @ApiModelProperty(example = "DJI")
    public String getFabricante() { return fabricante; }
    public void setFabricante(String fabricante) { this.fabricante = fabricante; }

    @ApiModelProperty(example = "Mavic 3")
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public double getHorasVuelo() { return horasVuelo; }
    public void setHorasVuelo(double horasVuelo) { this.horasVuelo = horasVuelo; }

    public boolean isOperativo() { return operativo; }
    public void setOperativo(boolean operativo) { this.operativo = operativo; }

    @Override
    public String toString() {
        return "Dron [id=" + id + ", nombre=" + nombre + ", fabricante=" + fabricante + ", modelo=" + modelo +
                ", horas=" + horasVuelo + ", operativo=" + operativo + "]";
    }
}
