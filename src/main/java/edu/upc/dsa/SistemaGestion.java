package edu.upc.dsa;

import edu.upc.dsa.models.*;
import java.util.Date;
import java.util.List;

public interface SistemaGestion {
    Dron addDron(String id, String nombre, String fabricante, String modelo);
    Piloto addPiloto(Piloto p);

    // Listados por horas (desc)
    List<Dron> getAllDronesByHoras();
    List<Piloto> getAllPilotosByHoras();

    // Almacén
    void guardarEnAlmacen(String dronId);
    Dron mantenerSiguienteDron();

    // Reservas
    Reserva addReserva(String id, String dronId, String pilotoId, Date inicio, double durHoras,
                       double latIni, double lonIni, double latDst, double lonDst);

    List<Reserva> getReservasDePiloto(String pilotoId);
    List<Reserva> getReservasDeDron(String dronId);

    // util
    Dron getDron(String id);
    Piloto getPiloto(String id);
    void clear();
    int sizePilotos();
}
