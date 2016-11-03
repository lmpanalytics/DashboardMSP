/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.dashboard.model;

/**
 * This class models an invoice report.
 *
 * @author SEPALMM
 */
public class Invoice {

    private String materialNumber;
    private Double sales;

    public Invoice(String materialNumber, Double sales) {
        this.materialNumber = materialNumber;
        this.sales = sales;
    }

    public String getMaterialNumber() {
        return materialNumber;
    }

    public void setMaterialNumber(String materialNumber) {
        this.materialNumber = materialNumber;
    }

    public Double getSales() {
        return sales;
    }

    public void setSales(Double sales) {
        this.sales = sales;
    }

}
