package edu.upc.dsa;

import edu.upc.dsa.models.*;
import org.junit.*;

import java.util.*;

public class SistemaGestionDronesTest {
    SistemaGestion sg;

    @Before
    public void setUp() {
        sg = SistemaGestionImpl.getInstance();
        sg.clear();
        sg.addDron("D1","Eagle","DJI","Mavic 3");
        sg.addDron("D2","Hawk","Parrot","Anafi");
        sg.addPiloto(new Piloto("P1","Joel","Moreno"));
        sg.addPiloto(new Piloto("P2","David","Sanchez"));
    }

    @After public void tearDown(){ sg.clear(); }

    @Test
    public void altaYListadoDronesPorHoras() {
        sg.addReserva("R1","D1","P1", new Date(), 2.0, 0,0,1,1);
        sg.addReserva("R2","D2","P1", new Date(System.currentTimeMillis()+10_000), 1.0, 0,0,1,1);
        Assert.assertEquals("D1", sg.getAllDronesByHoras().get(0).getId());
    }

    @Test
    public void entradaYSalidaAlmacenLIFO() {
        sg.guardarEnAlmacen("D1");
        sg.guardarEnAlmacen("D2");
        Assert.assertFalse(((SistemaGestionImpl)sg).getDron("D1").isOperativo());
        Assert.assertEquals("D2", sg.mantenerSiguienteDron().getId()); // LIFO: sale el último que entró
        Assert.assertTrue(((SistemaGestionImpl)sg).getDron("D2").isOperativo());
        Assert.assertEquals("D1", sg.mantenerSiguienteDron().getId()); // luego D1
    }

    @Test
    public void creacionReservaSinSolape() {
        Date t = new Date();
        Reserva r1 = sg.addReserva("R1","D1","P1", t, 1.0, 0,0,1,1);
        Reserva r2 = sg.addReserva("R2","D1","P1", new Date(t.getTime()+3600_000), 0.5, 0,0,1,1);
        Assert.assertNotNull(r1);
        Assert.assertNotNull(r2);
        Assert.assertEquals(2, sg.getReservasDePiloto("P1").size());
    }

    @Test
    public void bloqueoPorSolapeOPorAlmacen() {
        Date t = new Date();
        sg.addReserva("R1","D1","P1", t, 1.0, 0,0,1,1);
        Assert.assertNull(sg.addReserva("R2","D1","P2", new Date(t.getTime()+30*60_000), 1.0, 0,0,1,1));
        sg.guardarEnAlmacen("D2");
        Assert.assertNull(sg.addReserva("R3","D2","P2", new Date(t.getTime()+3_600_000), 1.0, 0,0,1,1));
    }

    @Test
    public void horasVueloOrden() {
        Date t = new Date();
        sg.addReserva("R1","D1","P1", t, 2.0, 0,0,1,1);
        sg.addReserva("R2","D2","P2", new Date(t.getTime()+3*3600_000), 1.0, 0,0,1,1);
        Assert.assertEquals("D1", sg.getAllDronesByHoras().get(0).getId());
    }
}
