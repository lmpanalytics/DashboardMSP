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
    private Double directCost;
    private Double quantity;

    /**
     * Constructor for the Dashboard sales data
     *
     * @param date the transaction date from the Special Ledger report in BO
     * @param netSales of sold materials
     * @param directCost of sold materials
     * @param quantity of sold materials
     */
    public DashboardSalesData(LocalDate date, Double netSales, Double directCost,
            Double quantity) {
        this.date = date;
        this.netSales = netSales;
        this.directCost = directCost;
        this.quantity = quantity;
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

    public Double getDirectCost() {
        return directCost;
    }

    public void setDirectCost(Double directCost) {
        this.directCost = directCost;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

}
