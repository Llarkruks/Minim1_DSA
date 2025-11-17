package edu.upc.dsa;

import edu.upc.dsa.exceptions.*;
import edu.upc.dsa.models.*;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class SistemaGestionImpl implements SistemaGestion {
    private static SistemaGestionImpl instance;
    final static Logger logger = Logger.getLogger(SistemaGestionImpl.class);

    private Map<String, Dron> drones = new HashMap<>();
    private Map<String, Piloto> pilotos = new HashMap<>();
    private Map<String, List<Reserva>> reservasPorDron = new HashMap<>();
    private Map<String, List<Reserva>> reservasPorPiloto = new HashMap<>();
    private Deque<String> almacen = new ArrayDeque<>();

    private SistemaGestionImpl() {}
    public static SistemaGestion getInstance() {
        if (instance == null) instance = new SistemaGestionImpl();
        return instance;
    }

    @Override
    public Dron addDron(String id, String nombre, String fabricante, String modelo) {
        logger.info("addDron id="+id+" nombre="+nombre+" fabricante="+fabricante+" modelo="+modelo);
        try {
            if (drones.containsKey(id)) {
                Dron d = drones.get(id);
                if (Objects.equals(d.getNombre(), nombre) && Objects.equals(d.getFabricante(), fabricante)
                        && Objects.equals(d.getModelo(), modelo)) {
                    throw new MismosParamConMismoIdException("Mismos valores con mismo ID");
                }
                d.setNombre(nombre); d.setFabricante(fabricante); d.setModelo(modelo);
                logger.info("Dron actualizado: "+d);
                return d;
            } else {
                Dron d = new Dron(id, nombre, fabricante, modelo);
                drones.put(id, d);
                logger.info("Dron añadido: "+d);
                return d;
            }
        } catch (MismosParamConMismoIdException e) {
            logger.error("Error addDron: "+e.getMessage());
            return null;
        }
    }

    @Override
    public Piloto addPiloto(Piloto p) {
        logger.info("addPiloto "+p);
        pilotos.put(p.getId(), p);
        return p;
    }

    @Override
    public List<Dron> getAllDronesByHoras() {
        logger.info("getAllDronesByHoras");
        return drones.values().stream()
                .sorted(Comparator.comparingDouble(Dron::getHorasVuelo).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Piloto> getAllPilotosByHoras() {
        logger.info("getAllPilotosByHoras");
        return pilotos.values().stream()
                .sorted(Comparator.comparingDouble(Piloto::getHorasVuelo).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public void guardarEnAlmacen(String dronId) {
        logger.info("guardarEnAlmacen dronId="+dronId);
        try {
            Dron d = getDron(dronId);
            if (d == null) throw new DronNoExisteException("No existe dron "+dronId);
            d.setOperativo(false);
            almacen.addLast(dronId);
            logger.info("Dron guardado en almacén (cola ahora): "+almacen);
        } catch (Exception e) {
            logger.error("Error guardarEnAlmacen: "+e.getMessage());
        }
    }

    @Override
    public Dron mantenerSiguienteDron() {
        logger.info("mantenerSiguienteDron (LIFO)");
        String dronId = almacen.pollLast();
        if (dronId == null) {
            logger.info("Almacén vacío");
            return null;
        }
        Dron d = drones.get(dronId);
        d.setOperativo(true);
        logger.info("Dron mantenido y operativo (LIFO): " + d.getId());
        return d;
    }

    private static boolean solapa(Date aIni, Date aFin, Date bIni, Date bFin) {
        return aIni.before(bFin) && bIni.before(aFin);
    }

    @Override
    public Reserva addReserva(String id, String dronId, String pilotoId, Date inicio, double durHoras,
                              double latIni, double lonIni, double latDst, double lonDst) {
        logger.info("addReserva id="+id+" dron="+dronId+" piloto="+pilotoId+" inicio="+inicio+" dur="+durHoras);
        try {
            Dron dron = getDron(dronId);
            Piloto piloto = getPiloto(pilotoId);
            if (dron == null) throw new DronNoExisteException("Dron no existe");
            if (piloto == null) throw new PilotoNoExisteException("Piloto no existe");
            if (!dron.isOperativo()) throw new DronNoOperativoException("Dron en almacén / no operativo");

            Date fin = new Date(inicio.getTime() + (long)(durHoras*3600_000L));

            // solapes dron
            for (Reserva r : reservasPorDron.getOrDefault(dronId, Collections.emptyList())) {
                if (solapa(inicio, fin, r.getInicio(), r.getFin()))
                    throw new SolapamientoReservaException("Solape con otra reserva del dron");
            }
            // solapes piloto
            for (Reserva r : reservasPorPiloto.getOrDefault(pilotoId, Collections.emptyList())) {
                if (solapa(inicio, fin, r.getInicio(), r.getFin()))
                    throw new SolapamientoReservaException("Solape con otra reserva del piloto");
            }

            Reserva res = new Reserva(id, dron, piloto, inicio, durHoras, latIni, lonIni, latDst, lonDst);
            reservasPorDron.computeIfAbsent(dronId, k->new ArrayList<>()).add(res);
            reservasPorPiloto.computeIfAbsent(pilotoId, k->new ArrayList<>()).add(res);

            dron.setHorasVuelo(dron.getHorasVuelo()+durHoras);
            piloto.setHorasVuelo(piloto.getHorasVuelo()+durHoras);

            logger.info("Reserva creada: "+res);
            return res;
        } catch (Exception e) {
            logger.error("Error addReserva: "+e.getMessage());
            return null;
        }
    }

    @Override
    public List<Reserva> getReservasDePiloto(String pilotoId) {
        logger.info("getReservasDePiloto "+pilotoId);
        return new ArrayList<>( reservasPorPiloto.getOrDefault(pilotoId, Collections.emptyList()) );
    }

    @Override
    public List<Reserva> getReservasDeDron(String dronId) {
        logger.info("getReservasDeDron "+dronId);
        return new ArrayList<>( reservasPorDron.getOrDefault(dronId, Collections.emptyList()) );
    }

    @Override
    public Dron getDron(String id) {
        logger.info("getDron id="+id);
        return drones.get(id);
    }

    @Override
    public Piloto getPiloto(String id) {
        logger.info("getPiloto id="+id);
        return pilotos.get(id);
    }

    @Override
    public void clear() {
        drones.clear(); pilotos.clear();
        reservasPorDron.clear(); reservasPorPiloto.clear();
        almacen.clear();
        logger.info("clear SistemaGestion");
    }

    @Override
    public int sizePilotos() { return pilotos.size(); }
}
