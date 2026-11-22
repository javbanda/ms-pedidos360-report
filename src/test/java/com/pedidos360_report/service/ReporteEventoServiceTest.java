package com.pedidos360_report.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pedidos360_report.entity.Reporte;
import com.pedidos360_report.event.PedidoEstadoEvento;
import com.pedidos360_report.event.PedidoVentaEvento;
import com.pedidos360_report.repository.ReporteRepository;

@ExtendWith(MockitoExtension.class)
class ReporteEventoServiceTest {

    private static final Long PEDIDO_ID = 31L;
    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 11, 21, 18, 22);
    private static final Instant FECHA_EVENTO = Instant.parse("2026-11-21T21:22:00Z");

    @Mock
    private ReporteRepository repository;

    @InjectMocks
    private ReporteEventoService service;

    private PedidoEstadoEvento eventoEstado(UUID id, long version, String estado) {
        return new PedidoEstadoEvento(
                id, 1, PEDIDO_ID, version, 1L, estado,
                new BigDecimal("2490.00"), FECHA, FECHA, null, FECHA_EVENTO);
    }

    private PedidoVentaEvento eventoVenta(UUID id, long version) {
        return new PedidoVentaEvento(
                id, 1, PEDIDO_ID, version, 1L,
                new BigDecimal("2490.00"), FECHA.plusMinutes(8), FECHA_EVENTO);
    }

    @Test
    void crearReporteAlConsumirEstado() {
        UUID id = UUID.randomUUID();
        when(repository.findByPedidoId(PEDIDO_ID)).thenReturn(Optional.empty());

        service.procesarEstado(eventoEstado(id, 0, "CREADO"));

        ArgumentCaptor<Reporte> captor = ArgumentCaptor.forClass(Reporte.class);
        verify(repository).save(captor.capture());
        Reporte guardado = captor.getValue();
        assertEquals(PEDIDO_ID, guardado.getPedidoId());
        assertEquals("CREADO", guardado.getEstado());
        assertEquals(0L, guardado.getVersionPedido());
        assertEquals(id, guardado.getUltimoEventoEstadoId());
        assertEquals(new BigDecimal("2490.00"), guardado.getMontoTotal());
    }

    @Test
    void noGuardarDosVecesElMismoEventoDeEstado() {
        UUID id = UUID.randomUUID();
        Reporte reporte = new Reporte();
        reporte.setPedidoId(PEDIDO_ID);
        reporte.setUltimoEventoEstadoId(id);
        when(repository.findByPedidoId(PEDIDO_ID)).thenReturn(Optional.of(reporte));

        service.procesarEstado(eventoEstado(id, 0, "CREADO"));

        verify(repository, never()).save(any(Reporte.class));
    }

    @Test
    void unEventoAntiguoNoRevierteElEstado() {
        Reporte reporte = new Reporte();
        reporte.setPedidoId(PEDIDO_ID);
        reporte.setEstado("DESPACHADO");
        reporte.setVersionPedido(3L);
        when(repository.findByPedidoId(PEDIDO_ID)).thenReturn(Optional.of(reporte));

        service.procesarEstado(eventoEstado(UUID.randomUUID(), 1, "ACEPTADO"));

        assertEquals("DESPACHADO", reporte.getEstado());
        assertEquals(3L, reporte.getVersionPedido());
    }

    @Test
    void registrarVentaMarcaPedidoEntregado() {
        UUID ventaId = UUID.randomUUID();
        Reporte reporte = new Reporte();
        reporte.setPedidoId(PEDIDO_ID);
        reporte.setEstado("DESPACHADO");
        reporte.setVersionPedido(3L);
        when(repository.findByEventoVentaId(ventaId)).thenReturn(Optional.empty());
        when(repository.findByPedidoId(PEDIDO_ID)).thenReturn(Optional.of(reporte));

        service.procesarVenta(eventoVenta(ventaId, 4));

        verify(repository).save(reporte);
        assertEquals("ENTREGADO", reporte.getEstado());
        assertEquals(4L, reporte.getVersionPedido());
        assertEquals(ventaId, reporte.getEventoVentaId());
        assertEquals(new BigDecimal("2490.00"), reporte.getMontoTotal());
    }

    @Test
    void noDuplicarVentaYaRegistrada() {
        UUID ventaId = UUID.randomUUID();
        when(repository.findByEventoVentaId(ventaId))
                .thenReturn(Optional.of(new Reporte()));

        service.procesarVenta(eventoVenta(ventaId, 4));

        verify(repository, never()).save(any(Reporte.class));
        verify(repository, never()).findByPedidoId(any());
    }

    @Test
    void rechazarSegundaVentaDistintaParaMismoPedido() {
        UUID ventaId = UUID.randomUUID();
        Reporte reporte = new Reporte();
        reporte.setPedidoId(PEDIDO_ID);
        reporte.setEventoVentaId(UUID.randomUUID());
        when(repository.findByEventoVentaId(ventaId)).thenReturn(Optional.empty());
        when(repository.findByPedidoId(PEDIDO_ID)).thenReturn(Optional.of(reporte));

        PedidoVentaEvento evento = eventoVenta(ventaId, 4);

        assertThrows(
                IllegalStateException.class,
                () -> service.procesarVenta(evento));

        verify(repository, never()).save(any(Reporte.class));
    }

    @Test
    void rechazarEventosNulosSinAccederABaseDeDatos() {
        assertThrows(IllegalArgumentException.class,
                () -> service.procesarEstado(null));
        assertThrows(IllegalArgumentException.class,
                () -> service.procesarVenta(null));
        verifyNoInteractions(repository);
    }
}
