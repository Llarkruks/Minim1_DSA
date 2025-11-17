package edu.upc.dsa.models;

import io.swagger.annotations.ApiModelProperty;

public class Piloto {
    String id;
    String nombre;
    String apellidos;
    double horasVuelo;

    public Piloto() {}
    public Piloto(String id, String nombre, String apellidos) {
        this.id = id; this.nombre = nombre; this.apellidos = apellidos;
    }

    @ApiModelProperty(example = "P1")
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @ApiModelProperty(example = "Joel")
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    @ApiModelProperty(example = "Moreno")
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public double getHorasVuelo() { return horasVuelo; }
    public void setHorasVuelo(double horasVuelo) { this.horasVuelo = horasVuelo; }

    @Override
    public String toString() { return "Piloto [id=" + id + ", nombre=" + nombre + ", apellidos=" + apellidos + "]"; }
}
