package com.pedidos360_report.repository;

import com.pedidos360_report.entity.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReporteRepository extends JpaRepository<Reporte, Long> {

    List<Reporte> findByEstado(String estado);

}