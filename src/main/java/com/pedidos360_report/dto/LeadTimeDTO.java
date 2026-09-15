package com.pedidos360_report.dto;

public class LeadTimeDTO {

    private double promedioMinutos;
    private long cantidadPedidosConsiderados;

    public LeadTimeDTO(double promedioMinutos, long cantidadPedidosConsiderados) {
        this.promedioMinutos = promedioMinutos;
        this.cantidadPedidosConsiderados = cantidadPedidosConsiderados;
    }

    public double getPromedioMinutos() { return promedioMinutos; }
    public void setPromedioMinutos(double promedioMinutos) { this.promedioMinutos = promedioMinutos; }

    public long getCantidadPedidosConsiderados() { return cantidadPedidosConsiderados; }
    public void setCantidadPedidosConsiderados(long cantidadPedidosConsiderados) { this.cantidadPedidosConsiderados = cantidadPedidosConsiderados; }
}