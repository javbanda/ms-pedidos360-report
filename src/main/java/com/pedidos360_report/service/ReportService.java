package com.pedidos360_report.service;

import com.pedidos360_report.dto.EstadoActivoDTO;
import com.pedidos360_report.entity.Reporte;
import com.pedidos360_report.repository.ReporteRepository;
import org.springframework.stereotype.Service;

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
}