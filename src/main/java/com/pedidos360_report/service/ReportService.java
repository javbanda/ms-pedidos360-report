package com.pedidos360_report.service;

import com.pedidos360_report.dto.EstadoActivoDTO;
import com.pedidos360_report.dto.KpisDTO;
import com.pedidos360_report.dto.LeadTimeDTO;
import com.pedidos360_report.dto.VentasPorHoraDTO;
import com.pedidos360_report.entity.Reporte;
import com.pedidos360_report.repository.ReporteRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

        private static final ZoneId ZONA_HORARIA = ZoneId.of("America/Santiago");

        private final ReporteRepository reporteRepository;

        public ReportService(ReporteRepository reporteRepository) {
                this.reporteRepository = reporteRepository;
        }

        public KpisDTO obtenerKpis() {

                List<Reporte> reportes = reporteRepository.findAll();

                long totalPedidos = reportes.size();

                long pedidosEntregados = reportes.stream()
                                .filter(r -> "ENTREGADO".equals(r.getEstado()))
                                .count();

                long pedidosEnProceso = reportes.stream()
                                .filter(r -> r.getEstado() != null)
                                .filter(r -> !"ENTREGADO".equals(r.getEstado()))
                                .filter(r -> !"CANCELADO".equals(r.getEstado()))
                                .count();

                BigDecimal totalVentas = reportes.stream()
                                .filter(r -> r.getEventoVentaId() != null)
                                .filter(r -> r.getMontoTotal() != null)
                                .map(Reporte::getMontoTotal)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                List<Long> minutosPorPedido = obtenerMinutosEntrega(reportes);

                double promedioLeadTime = minutosPorPedido.stream()
                                .mapToLong(Long::longValue)
                                .average()
                                .orElse(0.0);

                return new KpisDTO(
                                totalPedidos,
                                pedidosEntregados,
                                pedidosEnProceso,
                                totalVentas,
                                promedioLeadTime);
        }

        public List<EstadoActivoDTO> obtenerEstadosActivos() {

                List<Reporte> reportes = reporteRepository.findAll();

                Map<String, Long> conteoPorEstado = reportes.stream()
                                .collect(Collectors.groupingBy(
                                                Reporte::getEstado,
                                                Collectors.counting()));

                return conteoPorEstado.entrySet().stream()
                                .map(entry -> new EstadoActivoDTO(
                                                entry.getKey(),
                                                entry.getValue()))
                                .toList();
        }

        public Reporte guardarReporte(Reporte reporte) {
                return reporteRepository.save(reporte);
        }

        public List<VentasPorHoraDTO> calcularVentasPorHora() {

                List<Reporte> reportes = reporteRepository.findAll();

                Map<Integer, Double> ventasPorHora = reportes.stream()
                                .filter(r -> "ENTREGADO".equals(r.getEstado()))
                                .filter(r -> r.getEventoVentaId() != null)
                                .filter(r -> r.getFechaEntrega() != null
                                                && r.getMontoTotal() != null)
                                .collect(Collectors.groupingBy(
                                                r -> r.getFechaEntrega().getHour(),
                                                Collectors.summingDouble(
                                                                r -> r.getMontoTotal().doubleValue())));

                return ventasPorHora.entrySet().stream()
                                .map(entry -> new VentasPorHoraDTO(
                                                entry.getKey(),
                                                entry.getValue()))
                                .sorted(Comparator.comparingInt(
                                                VentasPorHoraDTO::getHora))
                                .toList();
        }

        public LeadTimeDTO calcularLeadTimePromedio() {

                List<Reporte> entregados = reporteRepository.findByEstado("ENTREGADO");

                List<Long> minutosPorPedido = obtenerMinutosEntrega(entregados);

                double promedio = minutosPorPedido.stream()
                                .mapToLong(Long::longValue)
                                .average()
                                .orElse(0.0);

                return new LeadTimeDTO(
                                promedio,
                                minutosPorPedido.size());
        }

        private List<Long> obtenerMinutosEntrega(
                        List<Reporte> reportes) {

                return reportes.stream()
                                .filter(r -> "ENTREGADO".equals(r.getEstado()))
                                .filter(r -> r.getFechaCreacion() != null
                                                && r.getFechaEntrega() != null)
                                .map(r -> Duration.between(
                                                r.getFechaCreacion()
                                                                .atZone(ZONA_HORARIA)
                                                                .toInstant(),
                                                r.getFechaEntrega()
                                                                .atZone(ZONA_HORARIA)
                                                                .toInstant())
                                                .toMinutes())
                                .toList();
        }
}
