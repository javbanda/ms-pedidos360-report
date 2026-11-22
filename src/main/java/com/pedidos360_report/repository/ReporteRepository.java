package com.pedidos360_report.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pedidos360_report.entity.Reporte;

public interface ReporteRepository
        extends JpaRepository<Reporte, Long> {

    List<Reporte> findByEstado(String estado);

    Optional<Reporte> findByPedidoId(Long pedidoId);

    Optional<Reporte> findByEventoVentaId(UUID eventoVentaId);
}