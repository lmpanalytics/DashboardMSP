/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.dashboard.beans;

import com.tetrapak.dashboard.model.Invoice;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.faces.bean.RequestScoped;

import javax.inject.Named;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;

/**
 * This bean makes the sales logic.
 *
 * @author SEPALMM
 */
@Named(value = "salesBean")
@Stateless
//@SessionScoped
@RequestScoped
public class SalesBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private List<Invoice> salesList;
    private double totalSales = 0d;

    private BarChartModel model;

    /**
     * Creates a new instance of salesBean
     */
    public SalesBean() {
    }

    @PostConstruct
    public void init() {
        this.salesList = makeSales();
        this.totalSales = sumSales();

        model = new BarChartModel();
        ChartSeries boys = new ChartSeries();
        boys.setLabel("Boys");
        boys.set("2004", 120);
        boys.set("2005", 100);
        boys.set("2006", 44);
        boys.set("2007", 150);
        boys.set("2008", 25);
        ChartSeries girls = new ChartSeries();
        girls.setLabel("Girls");
        girls.set("2004", 52);
        girls.set("2005", 60);
        girls.set("2006", 110);
        girls.set("2007", 135);
        girls.set("2008", 120);
        model.addSeries(boys);
        model.addSeries(girls);
        model.setTitle("Bar Chart");
        model.setLegendPosition("ne");
        Axis xAxis = model.getAxis(AxisType.X);
        xAxis.setLabel("Gender");
        Axis yAxis = model.getAxis(AxisType.Y);
        yAxis.setLabel("Births");
        yAxis.setMin(0);
        yAxis.setMax(200);
    }

    private List<Invoice> makeSales() {
        List<Invoice> list = new ArrayList<>();
        for (int i = 1; i < 11; i++) {
            list.add(new Invoice("mat_" + i, 100d * i));
        }
        return list;
    }

    private double sumSales() {
        double s = this.salesList.stream().mapToDouble(Invoice::getSales).sum();
        return s;
    }

    public List<Invoice> getSalesList() {
        return salesList;
    }

    public void setSalesList(List<Invoice> salesList) {
        this.salesList = salesList;
    }

    public double getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(double totalSales) {
        this.totalSales = totalSales;
    }

    public BarChartModel getModel() {
        return model;
    }
}
