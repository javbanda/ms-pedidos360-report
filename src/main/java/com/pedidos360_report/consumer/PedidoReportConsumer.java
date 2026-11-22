package com.pedidos360_report.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import com.pedidos360_report.event.PedidoEstadoEvento;
import com.pedidos360_report.event.PedidoVentaEvento;
import com.pedidos360_report.service.ReporteEventoService;

@Component
public class PedidoReportConsumer {

  private static final Logger log = LoggerFactory.getLogger(PedidoReportConsumer.class);

  private final JsonMapper jsonMapper;
  private final ReporteEventoService reporteEventoService;

  public PedidoReportConsumer(
      JsonMapper jsonMapper,
      ReporteEventoService reporteEventoService) {

    this.jsonMapper = jsonMapper;
    this.reporteEventoService = reporteEventoService;
  }

  @KafkaListener(topics = "${app.kafka.topics.estados}", groupId = "${spring.kafka.consumer.group-id}")
  public void consumirEstado(String mensaje) {

    try {
      PedidoEstadoEvento evento = jsonMapper.readValue(
          mensaje,
          PedidoEstadoEvento.class);

      reporteEventoService.procesarEstado(evento);

      log.info(
          "Evento de estado recibido: pedidoId={}, estado={}",
          evento.pedidoId(),
          evento.estado());

    } catch (JacksonException error) {

      throw new IllegalArgumentException(
          "Error al convertir evento de estado",
          error);
    }
  }

  @KafkaListener(topics = "${app.kafka.topics.ventas}", groupId = "${spring.kafka.consumer.group-id}")
  public void consumirVenta(String mensaje) {

    try {
      PedidoVentaEvento evento = jsonMapper.readValue(
          mensaje,
          PedidoVentaEvento.class);

      reporteEventoService.procesarVenta(evento);

      log.info(
          "Evento de venta recibido: pedidoId={}",
          evento.pedidoId());

    } catch (JacksonException error) {

      throw new IllegalArgumentException(
          "Error al convertir evento de venta",
          error);
    }
  }
}
