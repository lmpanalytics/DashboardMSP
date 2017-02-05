/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.dashboard.beans;

import com.tetrapak.dashboard.model.DashboardSalesData;
import com.tetrapak.dashboard.model.MarketSalesData;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
import org.primefaces.model.chart.MeterGaugeChartModel;
import utility.Utility;

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
    private Map<String, MarketSalesData> marketSalesMap;
    private LineChartModel r12SalesModel;
    private LineChartModel r12MarginModel;
    private LineChartModel r12MarketSalesModel;
    private LineChartModel r12MarketMarginModel;
    private MeterGaugeChartModel r12GrowthModel;
    private List<MarketSalesData> marketSalesList;
    private int marketCounter;

    public DashboardBean() {

    }

    @PostConstruct
    public void init() {
        System.out.println("I'm in the 'DashboardBean.init()' method.");

// INITIALIZE CLASS SPECIFIC MAPS AND FIELDS HERE
        // Initialize the Sales map
        salesMap = new LinkedHashMap<>();

//        Pre-Populate the sales map with dates and zeros
        /*  long deltaMonths = Utility.calcMonthsFromStart();
        for (int i = 0; i <= deltaMonths; i++) {
            LocalDate d = Utility.calcStartDate();
            salesMap.put(d.plusMonths(i),
                    new DashboardSalesData(d.plusMonths(i), 0d, 0d, 0d));
        }*/
//        Populate sales map with data from database
        populateSalesMap();

//        Populate the Sales Line Chart with Rolling 12 data
        populateR12LineCharts();
        
//        Populate the Sales Line Chart with Rolling 12 data
//        populateR12SalesLineChart();

//        Populate the Margin Line Chart with Rolling 12 data
//        populateR12MarginLineChart();

//        Populate the R12GrowthMeterGauge with Rolling 12 data
//        populateR12GrowthMeterGauge();

//        Initialize the marketSalesMap
        marketSalesMap = new LinkedHashMap<>();

//        Populate Market List with data from database
        populateMarketSalesMap();

//        Populate the Market Sales & Margin Line Charts with Rolling 12 data
        populateR12MarketLineCharts();
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
                    + " RETURN d.year AS Year, d.month AS Month, SUM(r.netSales) AS NetSales, SUM(r.directCost) AS DirectCost, SUM(r.quantity) AS Quantity"
                    + " ORDER BY Year, Month";

            StatementResult result = session.run(tx, Values.parameters(
                    "name", "Parts"));

            while (result.hasNext()) {
                Record r = result.next();

                int year = r.get("Year").asInt();
                int month = r.get("Month").asInt();
                double netSales = r.get("NetSales").asDouble();
                double directCost = r.get("DirectCost").asDouble();
                double quantity = r.get("Quantity").asDouble();

//                Make date
                LocalDate d = Utility.makeDate(year, month);

//            Add results to Map
                salesMap.put(d, new DashboardSalesData(
                        d, netSales, directCost, quantity)
                );
            }

        } catch (ClientException e) {
            System.err.println("Exception in 'populateSalesMap()':" + e);
        } finally {
//            neo4jBean.closeNeo4jDriver();

        }
    }

    /**
     * Populate the Global Sales & Margin Line Charts with Rolling 12 data.
     */
    private void populateR12LineCharts() {
        System.out.println("I'm in the 'populateR12LineCharts()' method.");

//        Initiate r12SalesModel
        r12SalesModel = new LineChartModel();

//        Initiate r12MarginModel
        r12MarginModel = new LineChartModel();

//       R12 algorithm based on dates
//        Accumulate sales and cost over rolling 12 periods
        int rollingPeriod = 12;

//                Initiate chart series 
        ChartSeries r12Sales = new ChartSeries();
        ChartSeries r12Margin = new ChartSeries();

        for (int i = 0; i <= (Utility.calcMonthsFromStart() - rollingPeriod + 1); i++) {
            LocalDate date = Utility.calcStartDate().plusMonths(i).with(
                    TemporalAdjusters.lastDayOfMonth());

//                Collect and sum sales
            Double netSalesR12 = salesMap.values().stream().filter(
                    m -> Utility.isWithinRange(date, m.getDate())).
                    collect(Collectors.summingDouble(
                            DashboardSalesData::getNetSales));

//                Collect and sum cost
            Double costR12 = salesMap.values().stream().filter(
                    m -> Utility.isWithinRange(date, m.getDate())).
                    collect(Collectors.summingDouble(
                            DashboardSalesData::getDirectCost));

//                System.out.printf("%s -> %s, %s", date, date.plusMonths(11).with(TemporalAdjusters.lastDayOfMonth()), netSalesR12);
            String chartDate = date.plusMonths(11).with(
                    TemporalAdjusters.lastDayOfMonth()).format(
                    DateTimeFormatter.ISO_DATE);

            //        Add data to r12Sales series        
            r12Sales.set(chartDate, netSalesR12);

            //        Add data to r12Margin series   
            double margin = Utility.calcMargin(netSalesR12,
                    costR12);
            r12Margin.set(chartDate, margin);
        }

        //        Populate r12SalesModel             
        r12SalesModel.addSeries(r12Sales);
        r12Sales.setLabel("Net Sales");

        //        Populate r12MarginModel             
        r12MarginModel.addSeries(r12Margin);
        r12Margin.setLabel("Net Margin");

//        Set chart parameters for the sales chart
        r12SalesModel.setTitle("Sales of Spare Parts");
        r12SalesModel.setLegendPosition("e");
        r12SalesModel.setShowPointLabels(true);
        r12SalesModel.setZoom(true);
        r12SalesModel.getAxis(AxisType.Y).setLabel("EUR");
        DateAxis axis = new DateAxis("Dates");
        axis.setTickAngle(-50);
        axis.setMax(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        axis.setTickFormat("%y-%b-%#d");
        r12SalesModel.getAxes().put(AxisType.X, axis);

//        Set chart parameters for the margin chart
        r12MarginModel.setTitle("NetMargin of Spare Parts");
        r12MarginModel.setLegendPosition("e");
        r12MarginModel.setShowPointLabels(true);
        r12MarginModel.setZoom(true);
        r12MarginModel.getAxis(AxisType.Y).setLabel("Margin (%)");
        DateAxis axis1 = new DateAxis("Dates");
        axis1.setTickAngle(-50);
        axis1.setMax(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        axis1.setTickFormat("%y-%b-%#d");
        r12MarginModel.getAxes().put(AxisType.X, axis1);
    }

    /**
     * Populate the Sales Line Chart with Rolling 12 data
     */
   /* private void populateR12SalesLineChart() {
        System.out.println("I'm in the 'populateR12SalesLineChart()' method.");
        //        Initiate r12SalesModel
        r12SalesModel = new LineChartModel();
        ChartSeries r12Sales = new ChartSeries();

//        Count number of key-value mappings in sales map
        int length = salesMap.size();
        LocalDate startDate = Utility.calcStartDate();

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
                    //        Populate r12SalesModel
                    r12Sales.set(date, accR12Sales);
                }
                innerLoopCounter++;
            }
        }

//        Set chart parameters
        r12Sales.setLabel("R12 Net Sales");
        r12SalesModel.addSeries(r12Sales);
        r12SalesModel.setTitle("Sales of Spare Parts");
        r12SalesModel.setLegendPosition("e");
        r12SalesModel.setShowPointLabels(true);
        r12SalesModel.setZoom(true);
        r12SalesModel.getAxis(AxisType.Y).setLabel("EUR");
        DateAxis axis = new DateAxis("Dates");
        axis.setTickAngle(-50);
        axis.setMax(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        axis.setTickFormat("%y-%b-%#d");

        r12SalesModel.getAxes().put(AxisType.X, axis);
    } */

    /**
     * Populate the Margin Line Chart with Rolling 12 data
     */
   /* private void populateR12MarginLineChart() {
        System.out.println("I'm in the 'populateR12MarginLineChart()' method.");
        //        Initiate r12MarginModel
        r12MarginModel = new LineChartModel();
        ChartSeries r12Margin = new ChartSeries();

//        Count number of key-value mappings in sales map
        int length = salesMap.size();
        LocalDate startDate = Utility.calcStartDate();

//        R12 algorithm
        int rollingPeriod = 12;
        for (int i = 0; i < (length - rollingPeriod); i++) {
            int innerLoopCounter = 1;
            double accR12Sales = 0d;
            double accR12Cost = 0d;
            for (int j = i; j < (rollingPeriod + i); j++) {

                LocalDate key = startDate.plusMonths(j);
                Double sales = salesMap.get(key).getNetSales();
                Double cost = salesMap.get(key).getDirectCost();
                if (innerLoopCounter <= rollingPeriod) {
                    accR12Sales = accR12Sales + sales;
                    accR12Cost = accR12Cost + cost;

                }
                if (innerLoopCounter == rollingPeriod) {
                    String date = key.format(DateTimeFormatter.ISO_DATE);
                    double margin = Utility.calcMargin(accR12Sales,
                            accR12Cost);
                    r12Margin.set(date, margin);
                }
                innerLoopCounter++;
            }
        }

//        Set chart parameters
        r12Margin.setLabel("R12 NetMargin");
        r12MarginModel.addSeries(r12Margin);
        r12MarginModel.setTitle("NetMargin of Spare Parts");
        r12MarginModel.setLegendPosition("e");
        r12MarginModel.setShowPointLabels(true);
        r12MarginModel.setZoom(true);
        r12MarginModel.getAxis(AxisType.Y).setLabel("Margin (%)");
        DateAxis axis = new DateAxis("Dates");
        axis.setTickAngle(-50);
        axis.setMax(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        axis.setTickFormat("%y-%b-%#d");

        r12MarginModel.getAxes().put(AxisType.X, axis);
    } */

    /**
     * Populate the R12GrowthMeterGauge with data
     */
    private void populateR12GrowthMeterGauge() {
        System.out.println("I'm in the 'populateR12GrowthMeterGauge()' method.");
//        Map<LocalDate, Double> tMap = new HashMap<>();

//        Initiate r12GrowthModel
        r12GrowthModel = new MeterGaugeChartModel();

        double r12t0 = 0d /* Current R12 sales */;
        double r12h12 = 0d /* R12 sales 12 months ago */;
        double growthRate = 0d;

//        Count number of key-value mappings in sales map
        int length = salesMap.size();
        LocalDate startDate = Utility.calcStartDate();

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
                /* if (innerLoopCounter == rollingPeriod) {
//                    Use to create a R12 growth chart
                    tMap.put(key, accR12Sales);
                }*/
                innerLoopCounter++;
            }
//            Current R12 sales        
            if (i == length - rollingPeriod) {
                r12t0 = accR12Sales;
            }
//            R12 sales 12 months ago
            if (i == length - 2 * rollingPeriod) {
                r12h12 = accR12Sales;
            }

//            Calculate the growth
            if (r12h12 != 0d) {
                growthRate = ((r12t0 / r12h12) - 1) * 100d;
            }
        }
//        System.out.println("Growth = ((" + r12t0 + " / " + r12h12 + ")-1)*100 = " + growth);

//        Set gauge segments
        List<Number> intervals = new ArrayList<Number>() {
            {
                add(5.0);
                add(7.5) /* Target */;
                add(15.0);
            }
        };
        r12GrowthModel = new MeterGaugeChartModel(growthRate, intervals);
        r12GrowthModel.setTitle("Growth of Spare Parts");
        r12GrowthModel.setGaugeLabel("%");
        r12GrowthModel.setSeriesColors("cc6666,E7E658,66cc66");
    }

    /**
     * ============================ MARKET CONTROLS ===========================
     * Populate Market Map with data from database. The data is limited to the
     * Top-10 Markets based on NetSales in the last 12-Month period.
     */
    private void populateMarketSalesMap() {
        System.out.println(" I'm in the 'populateMarketSalesMap()' method.");
//        Accumulate sales from this date to determine the largest markets
        String startDateLast12MonthSales
                = LocalDate.now().minusMonths(12).with(TemporalAdjusters.
                        lastDayOfMonth()).toString().replaceAll("-", "");
        // code query here
        try (Session session = neo4jBean.getDriver().session()) {
//  Query the ten biggest markets in terms of net sales over the last 12 months
            String tx = "MATCH (d:Day)<-[r:SOLD_ON]-(m:Material)"
                    + " MATCH (m)-[:CATEGORY]->(s:ServiceCategory)"
                    + " WHERE s.name = {name} AND (d.year + \"\" + d.month + \"01\") >= {date} "
                    + " WITH r.marketNumber AS MarketNumber, SUM(r.netSales) AS TNetSales"
                    + " ORDER BY TNetSales DESC LIMIT 10" /* Here, set the number of top markets */
                    + " WITH collect(MarketNumber) AS MarketNumbers" /* Collect the markets in a list */
                    + " MATCH (d:Day)<-[r:SOLD_ON]-(m:Material)-[:SOLD_IN]->(mkt:Market)"
                    + " MATCH (m)-[:CATEGORY]->(s:ServiceCategory)"
                    + " WHERE r.marketNumber IN MarketNumbers AND r.marketNumber = mkt.id AND s.name = {name}"
                    + " RETURN d.year AS Year, d.month AS Month, mkt.name AS Market, SUM(r.netSales) AS NetSales, SUM(r.directCost) AS DirectCost, SUM(r.quantity) AS Quantity"
                    + " ORDER BY Year, Month";

            StatementResult result = session.run(tx, Values.parameters(
                    "name", "Parts", "date", startDateLast12MonthSales));

            while (result.hasNext()) {
                Record r = result.next();

                int year = r.get("Year").asInt();
                int month = r.get("Month").asInt();
                String market = r.get("Market").asString();
                double netSales = r.get("NetSales").asDouble();
                double directCost = r.get("DirectCost").asDouble();
                double quantity = r.get("Quantity").asDouble();

//                Make date
                LocalDate d = Utility.makeDate(year, month);
//                Make composite key
                String key = d + market;

//            Add results to Map
                marketSalesMap.put(key, new MarketSalesData(d, market, netSales,
                        directCost, quantity));
            }

        } catch (ClientException e) {
            System.err.println("Exception in 'populateMarketSalesMap()':" + e);
        } finally {
            neo4jBean.closeNeo4jDriver();

        }
    }

    /**
     * Populate the Market Sales & Margin Line Charts with Rolling 12 data.
     */
    private void populateR12MarketLineCharts() {
        System.out.println("I'm in the 'populateR12MarketLineCharts()' method.");

        //        Initiate r12SalesModel
        r12MarketSalesModel = new LineChartModel();

//        Initiate r12MarginModel
        r12MarketMarginModel = new LineChartModel();

//       R12 algorithm based on dates
//        Create set of markets contained in the map
        Set<String> marketSet = marketSalesMap.values().stream().map(
                MarketSalesData::getMarket).collect(Collectors.toSet());

//        Accumulate sales and cost for each market over rolling 12 periods
        int rollingPeriod = 12;
        marketCounter = 0;

        for (String mkt : marketSet) {
//        Limit number of markets in the chart
            if (marketCounter < 5) {
//                Initiate chart series 
                ChartSeries r12Sales = new ChartSeries(mkt);
                ChartSeries r12Margin = new ChartSeries(mkt);

                for (int i = 0; i <= (Utility.calcMonthsFromStart() - rollingPeriod + 1); i++) {
                    LocalDate date = Utility.calcStartDate().plusMonths(i).with(
                            TemporalAdjusters.lastDayOfMonth());

//                Collect and sum sales
                    Double netSalesR12 = marketSalesMap.values().stream().
                            filter(
                                    m -> m.getMarket().equals(mkt)
                                    && Utility.isWithinRange(date, m.getDate())).
                            collect(
                                    Collectors.summingDouble(
                                            MarketSalesData::getNetSales));

//                Collect and sum cost
                    Double costR12 = marketSalesMap.values().stream().filter(
                            m -> m.getMarket().equals(mkt)
                            && Utility.isWithinRange(date, m.getDate())).
                            collect(
                                    Collectors.summingDouble(
                                            MarketSalesData::getDirectCost));

//                System.out.printf("%s -> %s, %s, %s", date, date.plusMonths(11).with(TemporalAdjusters.lastDayOfMonth()), mkt, netSalesR12);
                    String chartDate = date.plusMonths(11).with(
                            TemporalAdjusters.
                                    lastDayOfMonth()).format(
                                    DateTimeFormatter.ISO_DATE);

                    //        Add data to r12Sales series        
                    r12Sales.set(chartDate, netSalesR12);

                    //        Add data to r12Margin series   
                    double margin = Utility.calcMargin(netSalesR12,
                            costR12);
                    r12Margin.set(chartDate, margin);
                }

                //        Populate r12MarketSalesModel             
                r12MarketSalesModel.addSeries(r12Sales);
                r12Sales.setLabel(mkt);

                //        Populate r12MarketMarginModel             
                r12MarketMarginModel.addSeries(r12Margin);
                r12Margin.setLabel(mkt);
                marketCounter++;
            }
        }

//        Set chart parameters for the sales chart
        r12MarketSalesModel.setTitle("Sales of Spare Parts");
        r12MarketSalesModel.setLegendPosition("e");
        r12MarketSalesModel.setShowPointLabels(true);
        r12MarketSalesModel.setZoom(true);
        r12MarketSalesModel.getAxis(AxisType.Y).setLabel("EUR");
        DateAxis axis = new DateAxis("Dates");
        axis.setTickAngle(-50);
        axis.setMax(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        axis.setTickFormat("%y-%b-%#d");
        r12MarketSalesModel.getAxes().put(AxisType.X, axis);

//        Set chart parameters for the margin chart
        r12MarketMarginModel.setTitle("NetMargin of Spare Parts");
        r12MarketMarginModel.setLegendPosition("e");
        r12MarketMarginModel.setShowPointLabels(true);
        r12MarketMarginModel.setZoom(true);
        r12MarketMarginModel.getAxis(AxisType.Y).setLabel("Margin (%)");
        DateAxis axis1 = new DateAxis("Dates");
        axis1.setTickAngle(-50);
        axis1.setMax(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        axis1.setTickFormat("%y-%b-%#d");
        r12MarketMarginModel.getAxes().put(AxisType.X, axis1);
    }

    public LineChartModel getR12SalesModel() {
        return r12SalesModel;
    }

    public LineChartModel getR12MarginModel() {
        return r12MarginModel;
    }

    public MeterGaugeChartModel getR12GrowthModel() {
        return r12GrowthModel;
    }

    public LineChartModel getR12MarketSalesModel() {
        return r12MarketSalesModel;
    }

    public LineChartModel getR12MarketMarginModel() {
        return r12MarketMarginModel;
    }
}
