/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.dashboard.beans;

import com.tetrapak.dashboard.model.DashboardSalesData;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Stateless;
import javax.faces.bean.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.DateAxis;
import org.primefaces.model.chart.LineChartModel;

/**
 * This bean models the dashboard
 *
 * @author SEPALMM
 */
@Named(value = "dashboardBean")
@Stateless
@SessionScoped
public class DashboardBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    Neo4jBean neo4jBean;

    // ADD CLASS SPECIFIC MAPS AND FIELDS HERE
    private Map<LocalDate, DashboardSalesData> salesMap;
    private LineChartModel model;

    public DashboardBean() {

    }

    @PostConstruct
    public void init() {
        System.out.println("I'm in the 'DashboardBean.init()' method.");

// INITIALIZE CLASS SPECIFIC MAPS AND FIELDS HERE
        // Initialize the Sales map
        salesMap = new LinkedHashMap<>();

//        Pre-Populate the sales map with dates and zeros
        long deltaMonths = utility.Utility.calcMonthsFromStart();
        for (int i = 0; i <= deltaMonths; i++) {
            LocalDate d = utility.Utility.calcStartDate();
            salesMap.put(d.plusMonths(i),
                    new DashboardSalesData(d.plusMonths(i), 0d));
        }

//        Populate sales map with data from database
        populateSalesMap();

//        Populate the Sales Line Chart with Rolling 12 data
        populateR12SalesLineChart();
    }

    @PreDestroy
    public void destroyMe() {
        neo4jBean.closeNeo4jDriver();
        System.out.println(
                "Neo4jDriver in the DashboardBean have been disposed of.");
    }

    /**
     * Populate sales map with data from database
     */
    private void populateSalesMap() {
        System.out.println(" I'm in the populateSalesMap()' method.");

        // code query here
        try (Session session = neo4jBean.getDriver().session()) {

            String tx = "MATCH (s:ServiceCategory)<-[:CATEGORY]-(:Material)-[r:SOLD_ON]->(d:Day)"
                    + " WHERE s.name = {name}"
                    + " RETURN d.year AS Year, d.month AS Month, SUM(r.netSales) AS NetSales"
                    + " ORDER BY Year, Month";

            StatementResult result = session.run(tx, Values.parameters(
                    "name", "Parts"));

            while (result.hasNext()) {
                Record r = result.next();

                int year = r.get("Year").asInt();
                int month = r.get("Month").asInt();
                double netSales = r.get("NetSales").asDouble();

//                Make date
                LocalDate d = utility.Utility.makeDate(year, month);

//            Add results to Map
                salesMap.put(d, new DashboardSalesData(d, netSales));
            }

        } catch (ClientException e) {
            System.err.println("Exception in 'querySales()':" + e);
        } finally {
            neo4jBean.closeNeo4jDriver();

        }
    }

    /**
     * Populate the Sales Line Chart with Rolling 12 data
     */
    private void populateR12SalesLineChart() {
        System.out.println("I'm in the 'populateR12SalesLineChart()' method.");
        //        Initiate model
        model = new LineChartModel();
        ChartSeries r12Sales = new ChartSeries();

//        Count number of key-value mappings in sales map
        int length = salesMap.size();
        LocalDate startDate = utility.Utility.calcStartDate();

//        R12 algorithm
        int rollingPeriod = 12;
        for (int i = 0; i < (length - rollingPeriod); i++) {
            int innerLoopCounter = 1;
            double accR12Sales = 0d;
            for (int j = i; j < (rollingPeriod + i); j++) {

                LocalDate key = startDate.plusMonths(j);
                Double sales = salesMap.get(key).getNetSales();
                if (innerLoopCounter <= rollingPeriod) {
                    accR12Sales = accR12Sales + sales;

                }
                if (innerLoopCounter == rollingPeriod) {
                    String date = key.format(DateTimeFormatter.ISO_DATE);
                    //        Populate model
                    r12Sales.set(date, accR12Sales);
                }
                innerLoopCounter++;
            }
        }

//        Set chart parameters
        r12Sales.setLabel("R12 Net Sales");
        model.addSeries(r12Sales);
        model.setTitle("Sales of Spare Parts");
        model.setLegendPosition("e");
        model.setShowPointLabels(true);
        model.setZoom(true);
        model.getAxis(AxisType.Y).setLabel("EUR");
        DateAxis axis = new DateAxis("Dates");
        axis.setTickAngle(-50);
        axis.setMax(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        axis.setTickFormat("%y-%b-%#d");

        model.getAxes().put(AxisType.X, axis);
    }

    public LineChartModel getModel() {
        return model;
    }

}
