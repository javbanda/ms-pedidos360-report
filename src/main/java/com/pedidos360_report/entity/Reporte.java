package com.pedidos360_report.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reportes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_reportes_pedido_id", columnNames = "pedido_id"),
        @UniqueConstraint(name = "uk_reportes_evento_venta_id", columnNames = "evento_venta_id")
})
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;

    @Column(name = "sucursal_id")
    private Long sucursalId;

    @Column(name = "estado", nullable = false)
    private String estado;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    @Column(name = "monto_total")
    private BigDecimal montoTotal;

    @Column(name = "version_pedido")
    private Long versionPedido;

    @Column(name = "ultimo_evento_estado_id")
    private UUID ultimoEventoEstadoId;

    @Column(name = "evento_venta_id")
    private UUID eventoVentaId;

    // Constructores

    public Reporte() {
    }

    public Reporte(
            Long pedidoId,
            String estado,
            LocalDateTime fechaCreacion,
            LocalDateTime fechaEntrega,
            BigDecimal montoTotal) {

        this.pedidoId = pedidoId;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.fechaEntrega = fechaEntrega;
        this.montoTotal = montoTotal;
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public Long getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(Long sucursalId) {
        this.sucursalId = sucursalId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public LocalDateTime getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(LocalDateTime fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public Long getVersionPedido() {
        return versionPedido;
    }

    public void setVersionPedido(Long versionPedido) {
        this.versionPedido = versionPedido;
    }

    public UUID getUltimoEventoEstadoId() {
        return ultimoEventoEstadoId;
    }

    public void setUltimoEventoEstadoId(UUID ultimoEventoEstadoId) {
        this.ultimoEventoEstadoId = ultimoEventoEstadoId;
    }

    public UUID getEventoVentaId() {
        return eventoVentaId;
    }

    public void setEventoVentaId(UUID eventoVentaId) {
        this.eventoVentaId = eventoVentaId;
    }
}
