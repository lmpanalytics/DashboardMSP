/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.dashboard.model;

/**
 * This class models categorized table data
 *
 * @author SEPALMM
 */
public class CategoryTableData {

    private String category;
    private Double sales;
    private Double growth;
    private Double margin;
    private Double potential;

    /**
     * Constructor for the Category Table Data
     *
     * @param category e.g., Market name, Customer name, Assortment name
     * @param sales of sold materials
     * @param growth of sales
     * @param margin of sales
     * @param potential of sales in TecBase
     */
    public CategoryTableData(String category, Double sales, Double growth,
            Double margin, Double potential) {
        this.category = category;
        this.sales = sales;
        this.growth = growth;
        this.margin = margin;
        this.potential = potential;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getSales() {
        return sales;
    }

    public void setSales(Double sales) {
        this.sales = sales;
    }

    public Double getGrowth() {
        return growth;
    }

    public void setGrowth(Double growth) {
        this.growth = growth;
    }

    public Double getMargin() {
        return margin;
    }

    public void setMargin(Double margin) {
        this.margin = margin;
    }

    public Double getPotential() {
        return potential;
    }

    public void setPotential(Double potential) {
        this.potential = potential;
    }

}
