package com.pedidos360_report.metrics;

import com.pedidos360_report.service.ReportService;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ReporteMetricas {

    public ReporteMetricas(MeterRegistry registry, ReportService service) {

        Gauge.builder("pedidos360.pedidos.registrados", service,
                s -> s.obtenerKpis().totalPedidos())
                .register(registry);

        Gauge.builder("pedidos360.pedidos.entregados", service,
                s -> s.obtenerKpis().pedidosEntregados())
                .register(registry);

        Gauge.builder("pedidos360.pedidos.en.proceso", service,
                s -> s.obtenerKpis().pedidosEnProceso())
                .register(registry);

        Gauge.builder("pedidos360.ventas.monto", service,
                s -> s.obtenerKpis().totalVentas().doubleValue())
                .register(registry);

        Gauge.builder("pedidos360.lead.time.minutos", service,
                s -> s.obtenerKpis().promedioLeadTimeMinutos())
                .register(registry);
    }
}
