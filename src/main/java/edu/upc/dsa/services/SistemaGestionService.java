package edu.upc.dsa.services;

import edu.upc.dsa.SistemaGestion;
import edu.upc.dsa.SistemaGestionImpl;
import edu.upc.dsa.models.*;
import io.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;

@Api(value = "/Operaciones", description = "Endpoint SistemaGestion Drones")
@Path("/Operaciones")
public class SistemaGestionService {

    private final SistemaGestion sg;

    public SistemaGestionService() {
        this.sg = SistemaGestionImpl.getInstance();
        if (sg.sizePilotos()==0) { // datos de arranque opcionales
            sg.addDron("D1","Eagle","DJI","Mavic 3");
            sg.addDron("D2","Hawk","Parrot","Anafi");
            sg.addPiloto(new Piloto("P1","Joel","Moreno"));
            sg.addPiloto(new Piloto("P2","David","Sanchez"));
        }
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private static boolean solapa(Date aIni, Date aFin, Date bIni, Date bFin) {
        return aIni.before(bFin) && bIni.before(aFin);
    }

    private static boolean latOk(double lat) { return lat >= -90 && lat <= 90; }
    private static boolean lonOk(double lon) { return lon >= -180 && lon <= 180; }

    @POST @Path("/dron")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value="crear/actualizar dron")
    @ApiResponses({@ApiResponse(code=201,message="Creado",response=Dron.class),
            @ApiResponse(code=200,message="Actualizado",response=Dron.class),
            @ApiResponse(code=409,message="Mismos parámetros con mismo id"),
            @ApiResponse(code=400,message="Validation error")})
    public Response newDron(Dron d) {
        if (d==null || isBlank(d.getId()) || isBlank(d.getNombre()) || isBlank(d.getFabricante()) || isBlank(d.getModelo()))
            return Response.status(400).entity("Faltan campos obligatorios: id, nombre, fabricante, modelo").build();

        boolean existed = sg.getDron(d.getId())!=null;
        Dron out = sg.addDron(d.getId().trim(), d.getNombre().trim(), d.getFabricante().trim(), d.getModelo().trim());
        if (out==null) return Response.status(409).entity("Ya existe ese ID con los mismos parámetros").build();
        if (d.getHorasVuelo() > 0) {
            out.setHorasVuelo(d.getHorasVuelo());
        }
        return Response.status(existed?200:201).entity(out).build();
    }

    @POST @Path("/piloto")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiResponses({@ApiResponse(code=201,message="Creado",response=Piloto.class),
            @ApiResponse(code=400,message="Validation error")})
    public Response newPiloto(Piloto p) {
        if (p==null || isBlank(p.getId()) || isBlank(p.getNombre()))
            return Response.status(400).entity("Faltan campos obligatorios: id, nombre").build();
        return Response.status(201).entity(sg.addPiloto(p)).build();
    }

    // Listados
    @GET @Path("/drones")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDronesByHoras() {
        List<Dron> list = sg.getAllDronesByHoras();
        GenericEntity<List<Dron>> entity = new GenericEntity<List<Dron>>(list){};
        return Response.ok(entity).build();
    }

    @GET @Path("/pilotos")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPilotosByHoras() {
        List<Piloto> list = sg.getAllPilotosByHoras();
        GenericEntity<List<Piloto>> entity = new GenericEntity<List<Piloto>>(list){};
        return Response.ok(entity).build();
    }

    // Almacén
    @POST @Path("/almacen/{dronId}")
    @ApiResponses({@ApiResponse(code=201,message="Dron en almacén"),
            @ApiResponse(code=404,message="Dron no encontrado")})
    public Response guardarEnAlmacen(@PathParam("dronId") String dronId) {
        Dron d = sg.getDron(dronId);
        if (d == null) return Response.status(404).entity("Dron no encontrado: "+dronId).build();
        sg.guardarEnAlmacen(dronId);
        return Response.status(201).entity("Dron en almacén").build();
    }

    @POST @Path("/almacen/SacarDron")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({@ApiResponse(code=200,message="OK",response=Dron.class),
            @ApiResponse(code=204,message="Almacén vacío")})
    public Response mantenerSiguiente() {
        Dron d = sg.mantenerSiguienteDron();
        if (d==null) return Response.status(204).build();
        return Response.ok(d).build();
    }

    // Reservas
    @POST
    @Path("/reserva")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "crear reserva")
    @ApiResponses({
            @ApiResponse(code=201, message="Creada", response=Reserva.class),
            @ApiResponse(code=400, message="Validación de campos"),
            @ApiResponse(code=404, message="Dron/Piloto no existen"),
            @ApiResponse(code=409, message="Conflicto: dron no operativo o solape")
    })
    public Response nuevaReserva(Reserva r) {
        if (r == null)
            return Response.status(400).entity("Body vacío").build();
        if (r.getDron()==null || isBlank(r.getDron().getId()))
            return Response.status(400).entity("Falta dron.id").build();
        if (r.getPiloto()==null || isBlank(r.getPiloto().getId()))
            return Response.status(400).entity("Falta piloto.id").build();
        if (r.getInicio()==null)
            return Response.status(400).entity("Falta inicio (ISO-8601)").build();
        if (r.getDurHoras()<=0)
            return Response.status(400).entity("durHoras debe ser > 0").build();
        if (!latOk(r.getLatIni()) || !latOk(r.getLatDst()) || !lonOk(r.getLonIni()) || !lonOk(r.getLonDst()))
            return Response.status(400).entity("Coordenadas fuera de rango (lat: -90..90, lon: -180..180)").build();

        String dronId = r.getDron().getId();
        String pilotoId = r.getPiloto().getId();

        // Existencia de dron/piloto
        Dron dron = sg.getDron(dronId);
        if (dron == null)
            return Response.status(404).entity("Dron no encontrado: "+dronId).build();

        Piloto piloto = sg.getPiloto(pilotoId);
        if (piloto == null)
            return Response.status(404).entity("Piloto no encontrado: "+pilotoId).build();

        // Estado operativo
        if (!dron.isOperativo())
            return Response.status(409).entity("Dron no operativo (en almacén)").build();

        Date ini = r.getInicio();
        Date fin = new Date(ini.getTime() + (long)(r.getDurHoras() * 3600_000L));

        // Dron
        for (Reserva x : sg.getReservasDeDron(dronId)) {
            if (solapa(ini, fin, x.getInicio(), x.getFin())) {
                return Response.status(409).entity("Solape con otra reserva del dron: "+x.getId()).build();
            }
        }
        // Piloto
        for (Reserva x : sg.getReservasDePiloto(pilotoId)) {
            if (solapa(ini, fin, x.getInicio(), x.getFin())) {
                return Response.status(409).entity("Solape con otra reserva del piloto: "+x.getId()).build();
            }
        }

        Reserva out = sg.addReserva(
                r.getId(), dronId, pilotoId, r.getInicio(), r.getDurHoras(),
                r.getLatIni(), r.getLonIni(), r.getLatDst(), r.getLonDst()
        );

        if (out==null)
            return Response.status(500).entity("Error interno creando la reserva").build();

        return Response.status(201).entity(out).build();
    }

    // Consultas de reservas
    @GET @Path("/reservas/dron/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({@ApiResponse(code=200,message="OK",response=Reserva.class,responseContainer="List"),
            @ApiResponse(code=404,message="Dron no encontrado")})
    public Response reservasDron(@PathParam("id") String id) {
        if (sg.getDron(id) == null)
            return Response.status(404).entity("Dron no encontrado: "+id).build();
        List<Reserva> list = sg.getReservasDeDron(id);
        GenericEntity<List<Reserva>> entity = new GenericEntity<List<Reserva>>(list){};
        return Response.ok(entity).build();
    }

    @GET @Path("/reservas/piloto/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({@ApiResponse(code=200,message="OK",response=Reserva.class,responseContainer="List"),
            @ApiResponse(code=404,message="Piloto no encontrado")})
    public Response reservasPiloto(@PathParam("id") String id) {
        if (sg.getPiloto(id) == null)
            return Response.status(404).entity("Piloto no encontrado: "+id).build();
        List<Reserva> list = sg.getReservasDePiloto(id);
        GenericEntity<List<Reserva>> entity = new GenericEntity<List<Reserva>>(list){};
        return Response.ok(entity).build();
    }
}
