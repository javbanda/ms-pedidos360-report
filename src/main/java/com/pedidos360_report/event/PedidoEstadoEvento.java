package com.pedidos360_report.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PedidoEstadoEvento(
    UUID eventoId,
    int versionEsquema,
    Long pedidoId,
    long versionPedido,
    Long sucursalId,
    String estado,
    BigDecimal montoTotal,

    @JsonFormat(shape = JsonFormat.Shape.STRING) LocalDateTime fechaCreacion,

    @JsonFormat(shape = JsonFormat.Shape.STRING) LocalDateTime fechaActualizacion,

    @JsonFormat(shape = JsonFormat.Shape.STRING) LocalDateTime fechaEntrega,

    @JsonFormat(shape = JsonFormat.Shape.STRING) Instant fechaEvento) {
}
