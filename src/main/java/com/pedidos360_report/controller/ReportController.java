package com.pedidos360_report.controller;

import com.pedidos360_report.dto.EstadoActivoDTO;
import com.pedidos360_report.dto.LeadTimeDTO;
import com.pedidos360_report.entity.Reporte;
import com.pedidos360_report.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/kpis")
    public String obtenerKpis() {
        return "Microservicio de reportes funcionando correctamente";
    }

    @GetMapping("/estados-activos")
    public List<EstadoActivoDTO> obtenerEstadosActivos() {
        return reportService.obtenerEstadosActivos();
    }

    @GetMapping("/lead-time")
    public LeadTimeDTO obtenerLeadTimePromedio() {
        return reportService.calcularLeadTimePromedio();
    }

    @PostMapping
    public Reporte crearReporte(@RequestBody Reporte nuevoReporte) {
        return reportService.guardarReporte(nuevoReporte);
    }
}