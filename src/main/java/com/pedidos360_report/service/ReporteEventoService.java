package com.pedidos360_report.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pedidos360_report.entity.Reporte;
import com.pedidos360_report.event.PedidoEstadoEvento;
import com.pedidos360_report.event.PedidoVentaEvento;
import com.pedidos360_report.repository.ReporteRepository;

@Service
public class ReporteEventoService {

  private static final Logger log = LoggerFactory.getLogger(ReporteEventoService.class);

  private final ReporteRepository repository;

  public ReporteEventoService(ReporteRepository repository) {
    this.repository = repository;
  }

  @Transactional
  public void procesarEstado(PedidoEstadoEvento evento) {

    validarEstado(evento);

    Reporte reporte = repository.findByPedidoId(evento.pedidoId())
        .orElseGet(() -> {
          Reporte nuevo = new Reporte();
          nuevo.setPedidoId(evento.pedidoId());
          nuevo.setEstado(evento.estado());
          return nuevo;
        });

    // Evitar procesar nuevamente el ultimo evento recibido.
    if (evento.eventoId().equals(reporte.getUltimoEventoEstadoId())) {
      return;
    }

    Long versionActual = reporte.getVersionPedido();

    // No permitir que un evento antiguo revierta informacion.
    if (versionActual != null
        && evento.versionPedido() < versionActual) {

      boolean actualizado = false;

      // Recuperar datos que pudieron faltar
      // si el evento de venta llego primero.
      if (reporte.getFechaCreacion() == null) {
        reporte.setFechaCreacion(evento.fechaCreacion());
        actualizado = true;
      }

      if (reporte.getFechaActualizacion() == null) {
        reporte.setFechaActualizacion(evento.fechaActualizacion());
        actualizado = true;
      }

      if (reporte.getFechaEntrega() == null
          && evento.fechaEntrega() != null) {
        reporte.setFechaEntrega(evento.fechaEntrega());
        actualizado = true;
      }

      if (reporte.getSucursalId() == null) {
        reporte.setSucursalId(evento.sucursalId());
        actualizado = true;
      }

      if (reporte.getMontoTotal() == null) {
        reporte.setMontoTotal(evento.montoTotal());
        actualizado = true;
      }

      if (actualizado) {
        repository.save(reporte);
      }

      return;
    }

    reporte.setEstado(evento.estado());
    reporte.setVersionPedido(evento.versionPedido());
    reporte.setSucursalId(evento.sucursalId());
    reporte.setMontoTotal(evento.montoTotal());
    reporte.setFechaCreacion(evento.fechaCreacion());
    reporte.setFechaActualizacion(evento.fechaActualizacion());

    if (evento.fechaEntrega() != null) {
      reporte.setFechaEntrega(evento.fechaEntrega());
    }

    reporte.setUltimoEventoEstadoId(evento.eventoId());

    repository.save(reporte);

    log.info(
        "Reporte actualizado: pedidoId={}, estado={}, version={}",
        evento.pedidoId(),
        evento.estado(),
        evento.versionPedido());
  }

  @Transactional
  public void procesarVenta(PedidoVentaEvento evento) {

    validarVenta(evento);

    // Evitar duplicar una venta ya registrada.
    if (repository.findByEventoVentaId(evento.eventoId()).isPresent()) {
      return;
    }

    Reporte reporte = repository.findByPedidoId(evento.pedidoId())
        .orElseGet(() -> {
          Reporte nuevo = new Reporte();
          nuevo.setPedidoId(evento.pedidoId());
          nuevo.setEstado("ENTREGADO");
          return nuevo;
        });

    // Un pedido no debe registrar dos eventos de venta distintos.
    if (reporte.getEventoVentaId() != null
        && !reporte.getEventoVentaId().equals(evento.eventoId())) {

      throw new IllegalStateException(
          "El pedido ya tiene registrado otro evento de venta: "
              + evento.pedidoId());
    }

    Long versionActual = reporte.getVersionPedido();

    // No retroceder el estado si existe una version posterior.
    if (versionActual == null
        || evento.versionPedido() >= versionActual) {

      reporte.setEstado("ENTREGADO");
      reporte.setVersionPedido(evento.versionPedido());
    }

    reporte.setSucursalId(evento.sucursalId());
    reporte.setMontoTotal(evento.montoTotal());
    reporte.setFechaEntrega(evento.fechaEntrega());
    reporte.setEventoVentaId(evento.eventoId());

    repository.save(reporte);

    log.info(
        "Venta registrada: pedidoId={}, monto={}",
        evento.pedidoId(),
        evento.montoTotal());
  }

  private void validarEstado(PedidoEstadoEvento evento) {

    if (evento == null
        || evento.eventoId() == null
        || evento.pedidoId() == null
        || evento.pedidoId() <= 0
        || evento.sucursalId() == null
        || evento.sucursalId() <= 0
        || evento.estado() == null
        || evento.estado().isBlank()
        || evento.versionEsquema() != 1
        || evento.versionPedido() < 0
        || evento.montoTotal() == null
        || evento.montoTotal().signum() < 0
        || evento.fechaCreacion() == null
        || evento.fechaActualizacion() == null
        || evento.fechaEvento() == null) {

      throw new IllegalArgumentException(
          "Evento de estado invalido");
    }
  }

  private void validarVenta(PedidoVentaEvento evento) {

    if (evento == null
        || evento.eventoId() == null
        || evento.pedidoId() == null
        || evento.pedidoId() <= 0
        || evento.sucursalId() == null
        || evento.sucursalId() <= 0
        || evento.versionEsquema() != 1
        || evento.versionPedido() < 0
        || evento.montoTotal() == null
        || evento.montoTotal().signum() < 0
        || evento.fechaEntrega() == null
        || evento.fechaEvento() == null) {

      throw new IllegalArgumentException(
          "Evento de venta invalido");
    }
  }
}
