package com.pedidos360_report.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @GetMapping("/kpis")
    public String obtenerKpis() {
        return "Microservicio de reportes funcionando correctamente";
    }
}