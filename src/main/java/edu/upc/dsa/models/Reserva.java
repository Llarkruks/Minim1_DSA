package edu.upc.dsa.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

public class Reserva {
    @ApiModelProperty(example = "R1")
    String id;

    @ApiModelProperty(value = "Dron (en el POST basta con indicar su id)", required = true)
    Dron dron;

    @ApiModelProperty(value = "Piloto (en el POST basta con indicar su id)", required = true)
    Piloto piloto;

    @ApiModelProperty(example = "2025-01-01T10:00:00.000Z", required = true)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    Date inicio;

    @ApiModelProperty(example = "2.0", required = true)
    double durHoras;

    @ApiModelProperty(example = "0")
    double latIni;
    @ApiModelProperty(example = "0")
    double lonIni;
    @ApiModelProperty(example = "1")
    double latDst;
    @ApiModelProperty(example = "1")
    double lonDst;

    public Reserva() {}

    public Reserva(String id, Dron dron, Piloto piloto, Date inicio, double durHoras,
                   double latIni, double lonIni, double latDst, double lonDst) {
        this.id = id;
        this.dron = dron;
        this.piloto = piloto;
        this.inicio = inicio;
        this.durHoras = durHoras;
        this.latIni = latIni;
        this.lonIni = lonIni;
        this.latDst = latDst;
        this.lonDst = lonDst;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Dron getDron() { return dron; }
    public void setDron(Dron dron) { this.dron = dron; }

    public Piloto getPiloto() { return piloto; }
    public void setPiloto(Piloto piloto) { this.piloto = piloto; }

    public Date getInicio() { return inicio; }
    public void setInicio(Date inicio) { this.inicio = inicio; }

    public double getDurHoras() { return durHoras; }
    public void setDurHoras(double durHoras) { this.durHoras = durHoras; }

    public double getLatIni() { return latIni; }
    public void setLatIni(double latIni) { this.latIni = latIni; }

    public double getLonIni() { return lonIni; }
    public void setLonIni(double lonIni) { this.lonIni = lonIni; }

    public double getLatDst() { return latDst; }
    public void setLatDst(double latDst) { this.latDst = latDst; }

    public double getLonDst() { return lonDst; }
    public void setLonDst(double lonDst) { this.lonDst = lonDst; }

    @ApiModelProperty(readOnly = true, value = "Calculado: inicio + durHoras")
    public Date getFin() {
        if (inicio == null) return null;
        return new Date(inicio.getTime() + (long)(durHoras * 3600_000L));
    }

    @Override
    public String toString() {
        return "Reserva[id="+id+", dron="+(dron!=null?dron.getId():null)+", piloto="+(piloto!=null?piloto.getId():null)+
                ", inicio="+inicio+", dur="+durHoras+"]";
    }
}
