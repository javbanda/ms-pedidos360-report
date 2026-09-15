package com.pedidos360_report.dto;

public class VentasPorHoraDTO {

    private int hora;
    private double totalVentas;

    public VentasPorHoraDTO(int hora, double totalVentas) {
        this.hora = hora;
        this.totalVentas = totalVentas;
    }

    public int getHora() { return hora; }
    public void setHora(int hora) { this.hora = hora; }

    public double getTotalVentas() { return totalVentas; }
    public void setTotalVentas(double totalVentas) { this.totalVentas = totalVentas; }
}