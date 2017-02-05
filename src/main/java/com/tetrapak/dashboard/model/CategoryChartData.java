/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.dashboard.model;

import java.time.LocalDate;

/**
 * This class models market data
 *
 * @author SEPALMM
 */
public class CategoryChartData {

    private LocalDate date;
    private String market;
    private Double netSales;
    private Double directCost;
    private Double quantity;

    /**
     * Constructor for the Market Sales Data
     *
     * @param date the transaction date from the Special Ledger report in BO
     * @param market the market where the customer is located
     * @param netSales of sold materials
     * @param directCost of sold materials
     * @param quantity of sold materials
     */
    public CategoryChartData(LocalDate date, String market, Double netSales,
            Double directCost, Double quantity) {
        this.date = date;
        this.market = market;
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

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
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
