package com.pedidos360_report.dto;

import java.math.BigDecimal;

public record KpisDTO(
    long totalPedidos,
    long pedidosEntregados,
    long pedidosEnProceso,
    BigDecimal totalVentas,
    double promedioLeadTimeMinutos) {
}
