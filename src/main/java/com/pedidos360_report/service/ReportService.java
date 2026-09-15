package com.pedidos360_report.service;

import com.pedidos360_report.dto.EstadoActivoDTO;
import com.pedidos360_report.entity.Reporte;
import com.pedidos360_report.repository.ReporteRepository;
import org.springframework.stereotype.Service;
import com.pedidos360_report.dto.LeadTimeDTO;
import java.time.Duration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final ReporteRepository reporteRepository;

    public ReportService(ReporteRepository reporteRepository) {
        this.reporteRepository = reporteRepository;
    }

    public List<EstadoActivoDTO> obtenerEstadosActivos() {
        List<Reporte> reportes = reporteRepository.findAll();

        Map<String, Long> conteoPorEstado = reportes.stream()
            .collect(Collectors.groupingBy(Reporte::getEstado, Collectors.counting()));

        return conteoPorEstado.entrySet().stream()
            .map(entry -> new EstadoActivoDTO(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
        }
    
        public LeadTimeDTO calcularLeadTimePromedio() {
        List<Reporte> entregados = reporteRepository.findByEstado("ENTREGADO");

        List<Long> minutosPorPedido = entregados.stream()
            .filter(r -> r.getFechaCreacion() != null && r.getFechaEntrega() != null)
            .map(r -> Duration.between(r.getFechaCreacion(), r.getFechaEntrega()).toMinutes())
            .collect(Collectors.toList());

        double promedio = minutosPorPedido.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);

        return new LeadTimeDTO(promedio, minutosPorPedido.size());
    }
}