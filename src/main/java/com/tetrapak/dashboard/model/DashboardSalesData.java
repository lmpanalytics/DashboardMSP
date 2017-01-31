/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.dashboard.model;

import java.time.LocalDate;

/**
 * This class models data for the dashboard
 *
 * @author SEPALMM
 */
public class DashboardSalesData {

    private LocalDate date;
    private Double netSales;

    public DashboardSalesData(LocalDate date, Double netSales) {
        this.date = date;
        this.netSales = netSales;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Double getNetSales() {
        return netSales;
    }

    public void setNetSales(Double netSales) {
        this.netSales = netSales;
    }

}
