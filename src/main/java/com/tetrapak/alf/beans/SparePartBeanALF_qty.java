/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.alf.beans;

import com.tetrapak.dashboard.beans.CheckboxViewCluster;
import com.tetrapak.dashboard.beans.CheckboxViewCustGroup;
import com.tetrapak.dashboard.beans.Neo4jBean;
import com.tetrapak.dashboard.model.CategoryTableData;
import com.tetrapak.dashboard.model.GlobalChartData;
import com.tetrapak.dashboard.model.CategoryChartData;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
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
@Named(value = "sparePartBeanALF_qty")
@Stateful
@RequestScoped

@DeclareRoles(
        {"CENTRAL_TEAM", "BULF_DB", "BUICF_DB", "CPS_DB", "ALF_DB", "ECA_DB", "GC_DB", "GMEA_DB", "NCSA_DB", "SAEAO_DB"})
@RolesAllowed(
        {"CENTRAL_TEAM", "ALF_DB", "ECA_DB", "GC_DB", "GMEA_DB", "NCSA_DB", "SAEAO_DB"})
public class SparePartBeanALF_qty implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    Neo4jBean neo4jBean;

    @Inject
    CheckboxViewCluster cc;

    @Inject
    CheckboxViewCustGroup cg;

    @Resource
    SessionContext ctx;

    // ADD CLASS SPECIFIC MAPS AND FIELDS HERE
    private List<Object> top10Markets;
    private List<Object> top10CustomerGrps;
    private List<Object> top10AssortmentGrps;
    private Map<LocalDate, GlobalChartData> salesMap;
    private Map<String, CategoryChartData> marketSalesMap;
    private Map<String, CategoryChartData> custGrpSalesMap;
    private Map<String, CategoryChartData> assortmentSalesMap;
    private LineChartModel r12SalesModel;
    private LineChartModel r12MarketSalesModel;
    private LineChartModel r12CustGrpSalesModel;
    private LineChartModel r12AssortmentSalesModel;
    private MeterGaugeChartModel r12GrowthModel;
    private List<CategoryTableData> marketTableList;
    private List<CategoryTableData> custGrpTableList;
    private List<CategoryTableData> assortmentTableList;
    private int marketCounter;
    private int assortmentCounter;
    private Double globalGrowth;
    private Double globalSales;
    private Double totTop10MarketSales;
    private Double totTop10MarketGrowth;
    private Double totTop10CustGrpSales;
    private Double totTop10CustGrpGrowth;
    private Double totTop10AssortmentSales;
    private Double totTop10AssortmentGrowth;
    private Session session;
    private Set<String> setOfCustGrps;
    private final String CHART_COLORS;
    private String[] clusters;
    private String[] customerGroups;
    private final String SERVICE_CATEGORY;
    private final String[] ASSORTMENT_GRPS_BU = {"Al flow parts", "Al flow"};
    private String selectedClustersInfo;
    private String selectedCustomerGroupInfo;
    private boolean isCustGrpSelected;

    public SparePartBeanALF_qty() {
        this.CHART_COLORS = "d7191c,fdae61,ffffbf,abd9e9,2c7bb6";
        this.SERVICE_CATEGORY = "Parts";

    }

    @PostConstruct
    public void init() {
        System.out.println("I'm in the 'SparePartBeanALF_qty.init()' method.");

// INITIALIZE CLASS SPECIFIC MAPS AND FIELDS HERE
//      Initialize driver
        this.session = neo4jBean.getDriver().session();

//        Initialize the top-10 Markets list
        this.top10Markets = new LinkedList<>();

//        Initialize the top-10 Customer group list
        this.top10CustomerGrps = new LinkedList<>();

//        Initialize the set of Customer group list   
        this.setOfCustGrps = new LinkedHashSet<>();

//        Initialize the top-10 Assortment group list
        this.top10AssortmentGrps = new LinkedList<>();

//        Initialize the Sales map
        this.salesMap = new LinkedHashMap<>();

//        Initialize the marketSalesMap
        this.marketSalesMap = new LinkedHashMap<>();

//        Initialize the custGrpSalesMap
        this.custGrpSalesMap = new LinkedHashMap<>();

//        Initialize the assortmentSalesMap
        this.assortmentSalesMap = new LinkedHashMap<>();

//        Initialize the Market Table List
        this.marketTableList = new LinkedList<>();

//        Initialize the Customer Group Table List
        this.custGrpTableList = new LinkedList<>();

//        Initialize the Assortment Table List
        this.assortmentTableList = new LinkedList<>();

//        Initialize and get cluster selections from the index page
        initiateClusterSelection();

//        Initialize and get customer group selections from the index page
        initiateCustomerGroupSelection();

//        Populate sales map with data from database
        populateSalesMap();

//        Populate the Global Sales Volume Line Charts with Rolling 12 data
        populateR12LineCharts();

//        Populate Market Map
        populateMarketSalesMap();

//        Populate Customer Group Map
        populateCustomerGrpSalesMap();

//        Populate Assortment Group Map
        populateAssortmentGrpSalesMap();

//        Populate the Market Sales Volume Line Charts with Rolling 12 data
        populateR12MarketLineChartsAndTable();

//        Populate the Customer Group Sales Volume Line Charts with Rolling 12 data
        populateR12CustomerGrpLineChartsAndTable();

//        Populate the Assortment Group Sales Volume Line Charts with Rolling 12 data
        populateR12AssortmentGrpLineChartsAndTable();

    }

    private void initiateClusterSelection() {
        //        Initiate String builder and Array of clusters
        StringBuilder sb = new StringBuilder("Viewing ");
        List<String> clusterList = cc.getClusters();
        this.clusters = new String[clusterList.size()];
        clusterList.toArray(clusters);
//           Get Array of selected clusters and Handle skipped selection
        String[] testArray = cc.getSelectedClusters();
        if (testArray.length > 0 && !testArray[0].equals("0")) {
            this.clusters = cc.getSelectedClusters();

//            Add selected cluster(s) to Info string
            for (String c : this.clusters) {
                sb.append(c);
                sb.append(", ");
            }

        } else {
//            System.out.println("No cluster selection, using all clusters...");
//            Add selected cluster(s) to Info string
            for (String c : this.clusters) {
                sb.append(c);
                sb.append(", ");
            }
        }
        String s = sb.toString();
        if (s.endsWith(", ")) {
            s = s.substring(0, s.length() - 2);
        }
        this.selectedClustersInfo = s;
    }

    private void initiateCustomerGroupSelection() {
        isCustGrpSelected = false;
        //        Initiate String builder and Array of customer groups
        StringBuilder sb = new StringBuilder("Viewing ");
        List<String> custGroupList = cg.getCustomerGroups();
        this.customerGroups = new String[custGroupList.size()];
//           Get Array of selected customer groups and Handle skipped selection
        String[] testArray = cg.getSelectedCustGroups();
        if (testArray.length > 0) {
            if (!testArray[0].equals("ALL CUSTOMER GROUPS")) {
                isCustGrpSelected = true;
            }
            this.customerGroups = cg.getSelectedCustGroups();

//            Add selected customer group(s) to Info string
            for (String c : this.customerGroups) {
                sb.append(c);
                sb.append(", ");
            }

        } else {
            isCustGrpSelected = false;
//            System.out.println("No customer group selection, using all customer groups...");
//            NPE handling
            this.customerGroups[0] = "";
            this.customerGroups[1] = "";
            this.customerGroups[2] = "";
            this.customerGroups[3] = "";
            this.customerGroups[4] = "";
            this.customerGroups[5] = "";
            sb.append("ALL CUSTOMER GROUPS");
        }
        String s = sb.toString();
        if (s.endsWith(", ")) {
            s = s.substring(0, s.length() - 2);
        }
        this.selectedCustomerGroupInfo = s;
    }

    @PreDestroy
    public void destroyMe() {

    }

    /**
     * Makes cypher 'WHERE statement' used in methods 'populateSalesMap',
     * mapMarketPotentials, and mapAssortmentGrpPotentials to select among
     * combinations of Clusters and Customer groups.
     *
     * @return statement
     */
    private String makeCypherWhereStatementType1() {
        String whereStatement = "";
        if (this.clusters.length == 5 && (this.customerGroups[0].equals("")
                || this.customerGroups[0].equals("ALL CUSTOMER GROUPS"))) {
//                Use all clusters and customer groups
            whereStatement = "where a.name IN {assortmentGrpsBU}";
        } else if (this.customerGroups[0].equals("")
                || this.customerGroups[0].equals("ALL CUSTOMER GROUPS")) {
            whereStatement = " WHERE cl.name IN {Clusters} AND a.name IN {assortmentGrpsBU}";
        } else {
            whereStatement = " WHERE cl.name IN {Clusters} AND c.custGroup IN {CustGrps} AND a.name IN {assortmentGrpsBU}";
        }
        return whereStatement;
    }

    /**
     * Makes cypher 'WHERE statement' used in methods 'populateMarketSalesMap',
     * 'populateCustomerGrpSalesMap', and populateAssortmentGrpSalesMap to
     * select among combinations of Clusters and Customer groups.
     *
     * @return statement
     */
    private String makeCypherWhereStatementType2a() {
        String whereStatement = "";
        if (this.clusters.length == 5
                && (this.customerGroups[0].equals("")
                || this.customerGroups[0].equals("ALL CUSTOMER GROUPS"))) {
//                Use all clusters and all customer groups
            whereStatement = " WHERE (t.year + \"\" + t.month + \"\" + 01) >= {date} AND a.name IN {assortmentGrpsBU}";
        } else if (this.customerGroups[0].equals("")
                || this.customerGroups[0].equals("ALL CUSTOMER GROUPS")) {
//            Use specific clusters but all customer groups
            whereStatement = " WHERE (t.year + \"\" + t.month + \"\" + 01) >= {date} AND m.mktName = m.countryName AND cl.name IN {Clusters} AND a.name IN {assortmentGrpsBU}";
        } else {
//            Use specific clusters and specific customer groups
            whereStatement = " WHERE (t.year + \"\" + t.month + \"\" + 01) >= {date} AND m.mktName = m.countryName AND cl.name IN {Clusters} AND c.custGroup IN {CustGrps} AND a.name IN {assortmentGrpsBU}";
        }
        return whereStatement;
    }

    /**
     * Makes cypher 'WHERE statement' used in method 'populateMarketSalesMap' to
     * select among combinations of Clusters and Customer groups. An active
     * selection of Customer groups turns off the compulsory viewing of customer
     * types of type 'Global Accounts'.
     *
     * @return statement
     */
    private String makeCypherWhereStatementType2b() {
        String whereStatement = "";
        if (this.isCustGrpSelected) {
//           Use Top10 markets, Include all selected customer groups, and model based on Special Ledger
            whereStatement = " WHERE c.custGroup IN {CustGrps} AND m.mktName IN {Markets} AND m.mktName = m.countryName AND a.name IN {assortmentGrpsBU}";
        } else {
//        Use Top10 markets and model based on Special Ledger
            whereStatement = " WHERE m.mktName IN {Markets} AND m.mktName = m.countryName AND a.name IN {assortmentGrpsBU}";
        }
        return whereStatement;
    }

    /**
     * Makes cypher 'WHERE statement' used in method
     * 'populateCustomerGrpSalesMap' to select among combinations of Clusters
     * and Customer groups. An active selection of Customer groups turns off the
     * compulsory viewing of customer types of type 'Global Accounts'.
     *
     * @return statement
     */
    private String makeCypherWhereStatementType2c() {
        String whereStatement = "";
        if (this.isCustGrpSelected) {
//            Model based on Special Ledger
            whereStatement = " WHERE c.custGroup IN {CustGroups} AND m.mktName = m.countryName AND cl.name IN {Clusters} AND a.name IN {assortmentGrpsBU}";
        } else {
//       Include all Global Accounts as well, and Model based on Special Ledger
            whereStatement = " WHERE (c.custGroup IN {CustGroups} OR c.custType = 'Global Account') AND m.mktName = m.countryName AND cl.name IN {Clusters} AND a.name IN {assortmentGrpsBU}";
        }
        return whereStatement;
    }

    /**
     * Makes cypher 'WHERE statement' used in method
     * 'populateAssortmentGrpSalesMap' to select among combinations of Clusters
     * and Customer groups. An active selection of Customer groups turns off the
     * compulsory viewing of customer types of type 'Global Accounts'.
     *
     * @return statement
     */
    private String makeCypherWhereStatementType2d() {
        String whereStatement = "";
        if (this.isCustGrpSelected) {
//           Use Top10 clusters, Include all selected customer groups, and model based on Special Ledger
            whereStatement = " WHERE c.custGroup IN {CustGrps} AND a.name IN {Assortments} AND m.mktName = m.countryName AND cl.name IN {Clusters}";
        } else {
//        Use Top10 markets and model based on Special Ledger
            whereStatement = " WHERE a.name IN {Assortments} AND m.mktName = m.countryName AND cl.name IN {Clusters}";
        }
        return whereStatement;
    }

    /**
     * Populate sales map with data from database
     */
    private void populateSalesMap() {
        System.out.println(" I'm in the populateSalesMap()' method.");

        // code query here
        try {
            String whereStatement = makeCypherWhereStatementType1();

            String tx = "MATCH (cl:ClusterDB)<-[:MEMBER_OF]-(:MarketGroup)<-[:MEMBER_OF]-(:MarketDB)-[:MADE]->(t:Transaction)-[:BOOKED_AS]->(s:ServiceCategory {name: {name}}),"
                    + " (a:Assortment)-[:IN]->(t),"
                    + " q = (t)-[r:FOR]->(c:Customer)"
                    + whereStatement
                    + " WITH DISTINCT q AS q, r, t"
                    + " RETURN t.year AS Year, t.month AS Month, SUM(r.netSales)/1E6 AS NetSales, SUM(r.directCost)/1E6 AS DirectCost, SUM(r.quantity)/1E3 AS Quantity"
                    + " ORDER BY Year, Month";

            StatementResult result = this.session.run(tx, Values.parameters(
                    "name", this.SERVICE_CATEGORY, "Clusters", this.clusters,
                    "assortmentGrpsBU", this.ASSORTMENT_GRPS_BU,
                    "CustGrps", this.customerGroups));

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
                salesMap.put(d, new GlobalChartData(
                        d, netSales, directCost, quantity)
                );
            }

        } catch (ClientException e) {
            System.err.println("Exception in 'populateSalesMap()':" + e);
        }
    }

    /**
     * Populate the Global Sales Volume Line Charts with Rolling 12 data.
     */
    private void populateR12LineCharts() {
        System.out.println("I'm in the 'populateR12LineCharts()' method.");

//        Initiate r12SalesModel
        r12SalesModel = new LineChartModel();

//        Initiate r12GrowthModel
        r12GrowthModel = new MeterGaugeChartModel();

//        Calculate historical sales volume start dates to use in Growth calculation
        LocalDate dateT0 = Utility.makeDate(LocalDate.now().minusYears(1).
                getYear(), LocalDate.now().getMonthValue()
        );
        LocalDate dateH12 = Utility.makeDate(LocalDate.now().minusYears(2).
                getYear(), LocalDate.now().getMonthValue()
        );

//       R12 algorithm based on dates
//        Accumulate sales volume over rolling 12 periods
        int rollingPeriod = 12;

//                Initiate chart series 
        ChartSeries r12Sales = new ChartSeries();

        for (int i = 0; i <= (Utility.calcMonthsFromStart() - rollingPeriod + 1); i++) {
            LocalDate date = Utility.calcStartDate().plusMonths(i).with(
                    TemporalAdjusters.lastDayOfMonth());

//                Collect and sum sales volume
            Double salesVolumeR12 = salesMap.values().stream().filter(
                    m -> Utility.isWithinRange(date, m.getDate())).
                    collect(Collectors.summingDouble(
                            GlobalChartData::getQuantity));

//                System.out.printf("%s -> %s, %s", date, date.plusMonths(11).with(TemporalAdjusters.lastDayOfMonth()), salesVolumeR12);
            String chartDate = date.plusMonths(11).with(
                    TemporalAdjusters.lastDayOfMonth()).format(
                    DateTimeFormatter.ISO_DATE);

            //        Add data to r12Sales series        
            r12Sales.set(chartDate, salesVolumeR12);


            /* *************** SUMMARY CALCULATIONS *************** */
//  Round R12 sales volume to 3 significant figures and assign to class field
            this.globalSales = Utility.roundDouble(salesVolumeR12, 3);
        }

//                Collect and sum sales volume from two years ago for growth calculation
        Double r12h12 = salesMap.values().stream().filter(
                m -> Utility.isWithinRange(dateH12, m.getDate())).
                collect(Collectors.summingDouble(
                        GlobalChartData::getQuantity));

//                Collect and sum sales volume from one year ago for growth calculation
        Double r12t0 = salesMap.values().stream().filter(
                m -> Utility.isWithinRange(dateT0, m.getDate())).
                collect(Collectors.summingDouble(
                        GlobalChartData::getQuantity));

        //            Calculate the growth
        double r12GrowthRate = Utility.calcGrowthRate(r12t0, r12h12);

//  Round R12 growth rate to 3 significant figures and assign to class field
        this.globalGrowth = Utility.roundDouble(r12GrowthRate, 3);

        /* *************** CHART PARAMETERS *************** */
        //        Populate r12SalesModel             
        r12SalesModel.addSeries(r12Sales);
        r12Sales.setLabel("Sales Volume");

//        Set chart parameters for the sales volume chart
        r12SalesModel.setLegendPosition("nw");
        r12SalesModel.getAxis(AxisType.Y).setLabel("kPcs");
        DateAxis axis = new DateAxis("Dates");
        axis.setTickAngle(-50);
        axis.setMax(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        axis.setTickFormat("%y-%b-%#d");
        r12SalesModel.getAxes().put(AxisType.X, axis);
        r12SalesModel.setAnimate(true);

//      Set chart parameters for the MeterGauge chart 
        double maxscaleValue = 15d;
        List<Number> intervals = new ArrayList<Number>() {
            {
                add(5.0);
                add(7.2) /* Target */;
                add(maxscaleValue);
            }
        };
        r12GrowthModel = new MeterGaugeChartModel(this.globalGrowth,
                intervals);
        r12GrowthModel.setMax(maxscaleValue);
        r12GrowthModel.setMin(0d);
        r12GrowthModel.setGaugeLabel("%");
        r12GrowthModel.setSeriesColors("cc6666,E7E658,66cc66");

    }

    /**
     * ============================ MARKET CONTROLS ===========================
     * Populate Market Map with data from database. The data is limited to the
     * Top-10 Markets based on Sales Volume in the last 12-Month period.
     */
    private void populateMarketSalesMap() {
        System.out.println(" I'm in the 'populateMarketSalesMap()' method.");
//        Accumulate sales volume from this date to determine the largest markets
        String startDate = Utility.makeStartDateLast12MonthSales();
        // code query here
        try {
//  Query the ten biggest markets in terms of sales volume over the last 12 months
            String whereStatement = makeCypherWhereStatementType2a();

            String tx = "MATCH (c:Customer)<-[r:FOR]-(t:Transaction)-[:BOOKED_AS]->(:ServiceCategory {name: {name}}),"
                    + " (cl:ClusterDB)<-[:MEMBER_OF]-(:MarketGroup)<-[:MEMBER_OF]-(m:MarketDB)-[:MADE]->(t),"
                    + " (a:Assortment)-[:IN]->(t)"
                    + whereStatement
                    + " WITH m.mktName AS Market, SUM(r.quantity) AS TVolSales"
                    + " ORDER BY TVolSales DESC LIMIT 10" /* Here, set the number of top markets */
                    /* Collect the markets in a list */
                    + " RETURN collect(Market) AS Markets";

            StatementResult result = this.session.run(tx, Values.parameters(
                    "name", this.SERVICE_CATEGORY, "date", startDate,
                    "Clusters", this.clusters,
                    "assortmentGrpsBU", this.ASSORTMENT_GRPS_BU,
                    "CustGrps", this.customerGroups));

            while (result.hasNext()) {
                Record r = result.next();
                this.top10Markets = r.get("Markets").asList();
            }
            String whereStatement1 = makeCypherWhereStatementType2b();

            String tx1 = "MATCH (c:Customer)<-[r:FOR]-(t:Transaction)-[:BOOKED_AS]->(:ServiceCategory {name: {name}}),"
                    + " (m:MarketDB)-[:MADE]->(t),"
                    + " (a:Assortment)-[:IN]->(t)"
                    + whereStatement1
                    + " RETURN t.year AS Year, t.month AS Month, m.mktName AS Market, SUM(r.netSales)/1E6 AS NetSales, SUM(r.directCost)/1E6 AS DirectCost, SUM(r.quantity)/1E3 AS Quantity"
                    + " ORDER BY Year, Month";

            StatementResult result1 = this.session.run(tx1, Values.parameters(
                    "name", this.SERVICE_CATEGORY, "Markets", this.top10Markets,
                    "assortmentGrpsBU", this.ASSORTMENT_GRPS_BU,
                    "CustGrps", this.customerGroups));

            while (result1.hasNext()) {
                Record r = result1.next();

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
                marketSalesMap.put(key, new CategoryChartData(d, market,
                        netSales, directCost, quantity));
            }

        } catch (ClientException e) {
            System.err.println("Exception in 'populateMarketSalesMap()':" + e);
        }
    }

    /**
     * Populate the Market Sales Volume Line Charts and Data Table with Rolling
     * 12 data.
     */
    private void populateR12MarketLineChartsAndTable() {
        System.out.println("I'm in the 'populateR12MarketLineCharts' method.");

//        Initiate totTop10MarketSales
        totTop10MarketSales = 0d;

//        Initiate r12MarketSalesModel
        r12MarketSalesModel = new LineChartModel();

        //        Calculate historical sales volume start dates to use in Growth calculation
        LocalDate dateT0 = Utility.makeDate(LocalDate.now().minusYears(1).
                getYear(), LocalDate.now().getMonthValue()
        );
        LocalDate dateH12 = Utility.makeDate(LocalDate.now().minusYears(2).
                getYear(), LocalDate.now().getMonthValue()
        );

//       R12 algorithm based on dates
//        Accumulate sales volume for each market over rolling 12 periods
        int rollingPeriod = 12;
        marketCounter = 0;
        double totR12SalesT0 = 0d;
        double totR12SalesH12 = 0d;
        double totR12Growth = 0d;

        try {
            for (Object mkt : top10Markets) {
//                Initiate chart series and variables
                ChartSeries r12Sales = new ChartSeries(mkt.toString());

                for (int i = 0; i <= (Utility.calcMonthsFromStart() - rollingPeriod + 1); i++) {
                    LocalDate date = Utility.calcStartDate().plusMonths(i).with(
                            TemporalAdjusters.lastDayOfMonth());

//                Collect and sum sales volume
                    Double salesVolumeR12 = marketSalesMap.values().stream().
                            filter(
                                    m -> m.getCategory().equals(mkt)
                                    && Utility.isWithinRange(date, m.getDate())).
                            collect(Collectors.summingDouble(
                                    CategoryChartData::getQuantity));

                    String chartDate = date.plusMonths(11).with(
                            TemporalAdjusters.
                                    lastDayOfMonth()).format(
                                    DateTimeFormatter.ISO_DATE);

                    //        Add data to r12Sales series        
                    r12Sales.set(chartDate, salesVolumeR12);

                }
                /* *************** TABLE CALCULATIONS *************** */
//                Collect and sum sales volume from two years ago for growth calculation
                Double r12SalesH12 = marketSalesMap.values().stream().filter(
                        m -> m.getCategory().equals(mkt) && Utility.
                        isWithinRange(
                                dateH12, m.getDate())).collect(Collectors.
                                summingDouble(
                                        CategoryChartData::getQuantity));

//                Collect and sum sales volume from one year ago for growth calculation
                Double r12SalesT0 = marketSalesMap.values().stream().filter(
                        m -> m.getCategory().equals(mkt) && Utility.
                        isWithinRange(
                                dateT0, m.getDate())).collect(Collectors.
                                summingDouble(
                                        CategoryChartData::getQuantity));

//            Calculate the growth
                double growthRate = Utility.calcGrowthRate(r12SalesT0,
                        r12SalesH12);

// Populate the Category Table List and round results to 3 significant figures
                double r12SalesT0Rounded = Utility.roundDouble(r12SalesT0, 3);
                double growthRateRounded = Utility.roundDouble(growthRate, 3);

                marketTableList.add(new CategoryTableData(mkt.toString(),
                        r12SalesT0Rounded, growthRateRounded, 0d,
                        0d)
                );

//            Sum total R12 sales volume
                totR12SalesT0 = totR12SalesT0 + r12SalesT0;
                totR12SalesH12 = totR12SalesH12 + r12SalesH12;
//            Calculate total R12 growth
                totR12Growth = Utility.calcGrowthRate(totR12SalesT0,
                        totR12SalesH12);

                //        Set number of markets in the charts
                if (marketCounter < 5) {
                    //        Populate r12MarketSalesModel             
                    r12MarketSalesModel.addSeries(r12Sales);
                    r12Sales.setLabel(mkt.toString());

                    marketCounter++;
                }
            }
            /* *************** TABLE SUMMARY CALCULATIONS *************** */
//  Sort category list in decending order based on sales volume
            Collections.sort(marketTableList,
                    (CategoryTableData a, CategoryTableData b) -> b.getSales().
                            compareTo(a.getSales()));

            /*  Round total R12 Sales and Growth to 3 significant figures and
            assign to class field. */
            this.totTop10MarketSales = Utility.roundDouble(totR12SalesT0, 3);
            this.totTop10MarketGrowth = Utility.roundDouble(totR12Growth, 3);

            /* *************** CHART PARAMETERS *************** */
//        Set chart parameters for the sales volume chart
            r12MarketSalesModel.setLegendPosition("nw");
            r12MarketSalesModel.getAxis(AxisType.Y).setLabel("kPcs");
            r12MarketSalesModel.setSeriesColors(this.CHART_COLORS);
            DateAxis axis = new DateAxis("Dates");
            axis.setTickAngle(-50);
            axis.setMax(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
            axis.setTickFormat("%y-%b-%#d");
            r12MarketSalesModel.getAxes().put(AxisType.X, axis);
            r12MarketSalesModel.setAnimate(true);

        } catch (ClientException e) {
            System.err.println(
                    "Exception in 'populateR12MarketLineCharts method':" + e);
        }
    }

    /**
     * ================= CUSTOMER GROUP CONTROLS =================
     *
     * Populate CustomerGrp Map with data from database. The data is limited to
     * the Top-10 Customer Groups based on Sales Volume in the last 12-Month
     * period, and also override to include all Global Accounts.
     */
    private void populateCustomerGrpSalesMap() {
        System.out.
                println(" I'm in the 'populateCustomerGrpSalesMap()' method.");
//        Accumulate sales volume from this date to find the largest customers grps
        String startDate = Utility.makeStartDateLast12MonthSales();
        // code query here
        try {
            /* Query the ten biggest customer groups in terms of sales volume 
            over the last 12 months */
            String whereStatement = makeCypherWhereStatementType2a();

            String tx = "MATCH (c:Customer)<-[r:FOR]-(t:Transaction)-[:BOOKED_AS]->(:ServiceCategory {name: {name}}),"
                    + " (cl:ClusterDB)<-[:MEMBER_OF]-(:MarketGroup)<-[:MEMBER_OF]-(m:MarketDB)-[:MADE]->(t),"
                    + " (a:Assortment)-[:IN]->(t)"
                    + whereStatement
                    + " WITH c.custGroup AS CustGroup, SUM(r.quantity) AS TVolSales"
                    + " ORDER BY TVolSales DESC LIMIT 10" /* Here, set the number of top customer groups */
                    /* Collect the customer groups in a list */
                    + " RETURN collect(CustGroup) AS CustGroups";

            StatementResult result = this.session.run(tx, Values.parameters(
                    "name", this.SERVICE_CATEGORY, "date", startDate,
                    "Clusters", this.clusters,
                    "assortmentGrpsBU", this.ASSORTMENT_GRPS_BU,
                    "CustGrps", this.customerGroups));

            while (result.hasNext()) {
                Record r = result.next();

                this.top10CustomerGrps = r.get("CustGroups").asList();
            }

            String whereStatement1 = makeCypherWhereStatementType2c();

            String tx1 = "MATCH (c:Customer)<-[r:FOR]-(t:Transaction)-[:BOOKED_AS]->(:ServiceCategory {name: {name}}),"
                    + " (cl:ClusterDB)<-[:MEMBER_OF]-(:MarketGroup)<-[:MEMBER_OF]-(m:MarketDB)-[:MADE]->(t),"
                    + " (a:Assortment)-[:IN]->(t)"
                    + whereStatement1
                    + " RETURN t.year AS Year, t.month AS Month, c.custGroup AS CustGroup, SUM(r.netSales)/1E6 AS NetSales, SUM(r.directCost)/1E6 AS DirectCost, SUM(r.quantity)/1E3 AS Quantity"
                    + " ORDER BY Year, Month";

            StatementResult result1 = this.session.run(tx1, Values.parameters(
                    "name", this.SERVICE_CATEGORY,
                    "CustGroups", this.top10CustomerGrps,
                    "Clusters", this.clusters,
                    "assortmentGrpsBU", this.ASSORTMENT_GRPS_BU));

            while (result1.hasNext()) {
                Record r = result1.next();

                int year = r.get("Year").asInt();
                int month = r.get("Month").asInt();
                String custGrp = r.get("CustGroup").asString();
                double netSales = r.get("NetSales").asDouble();
                double directCost = r.get("DirectCost").asDouble();
                double quantity = r.get("Quantity").asDouble();

//                Make date
                LocalDate d = Utility.makeDate(year, month);
//                Make composite key
                String key = d + custGrp;

//            Add results to Map
                custGrpSalesMap.put(key, new CategoryChartData(d, custGrp,
                        netSales, directCost, quantity));
            }

//            Print Map contents
//        custGrpSalesMap.entrySet().stream().map((entry) -> entry.getValue()).forEachOrdered((v) -> {System.out.printf("%s;%s;%s;%s;%s\n", v.getDate(), v.getCategory(), v.getNetSales(), v.getDirectCost(), v.getQuantity());});
//  Populate a Table Customer Grp List to be used in the sales volume table.
            ArrayList<CategoryChartData> tList = new ArrayList<>(
                    custGrpSalesMap.values());
//            Extract Customer groups to list
            this.setOfCustGrps = tList.stream().map(c -> c.getCategory()).
                    collect(Collectors.toSet());

        } catch (ClientException e) {
            System.err.println(
                    "Exception in 'populateCustomerGrpSalesMap()':" + e);
        }
    }

    /**
     * Populate the Customer Group Sales Line Charts and Data Table with Rolling
     * 12 data.
     */
    private void populateR12CustomerGrpLineChartsAndTable() {
        System.out.println(
                "I'm in the 'populateR12CustomerGrpLineChartsAndTable' method.");

//        Initiate totTop10CustGrpSales
        totTop10CustGrpSales = 0d;

//        Initiate r12CustGrpSalesModel
        r12CustGrpSalesModel = new LineChartModel();

        //        Calculate historical sales volume start dates to use in Growth calculation
        LocalDate dateT0 = Utility.makeDate(LocalDate.now().minusYears(1).
                getYear(), LocalDate.now().getMonthValue()
        );
        LocalDate dateH12 = Utility.makeDate(LocalDate.now().minusYears(2).
                getYear(), LocalDate.now().getMonthValue()
        );

//       R12 algorithm based on dates
//        Accumulate sales volume for each customer group over rolling 12 periods
        int rollingPeriod = 12;
        double totR12SalesT0 = 0d;
        double totR12SalesH12 = 0d;
        double totR12Growth = 0d;

        try {
//            Convert set of customer groups to list
            List<String> listOfCustGrps = new LinkedList<>(this.setOfCustGrps);

//            Loop through the customer groups
            for (String cgr : listOfCustGrps) {
//                Initiate chart series and variables
                ChartSeries r12Sales = new ChartSeries(cgr);

                for (int i = 0; i <= (Utility.calcMonthsFromStart() - rollingPeriod + 1); i++) {
                    LocalDate date = Utility.calcStartDate().plusMonths(i).with(
                            TemporalAdjusters.lastDayOfMonth());

//                Collect and sum sales volume
                    Double salesVolumeR12 = custGrpSalesMap.values().stream().
                            filter(m -> m.getCategory().equals(cgr)
                            && Utility.isWithinRange(date, m.getDate())).
                            collect(Collectors.summingDouble(
                                    CategoryChartData::getQuantity));

                    String chartDate = date.plusMonths(11).with(
                            TemporalAdjusters.lastDayOfMonth()).format(
                            DateTimeFormatter.ISO_DATE);

                    //        Add data to r12Sales series        
                    r12Sales.set(chartDate, salesVolumeR12);
                }
                /* *************** TABLE CALCULATIONS *************** */
//                Collect and sum sales volume from two years ago for growth calculation
                Double r12SalesH12 = custGrpSalesMap.values().stream().filter(
                        m -> m.getCategory().equals(cgr) && Utility.
                        isWithinRange(
                                dateH12, m.getDate())).collect(Collectors.
                                summingDouble(
                                        CategoryChartData::getQuantity));

//                Collect and sum sales volume from one year ago for growth calculation
                Double r12SalesT0 = custGrpSalesMap.values().stream().filter(
                        m -> m.getCategory().equals(cgr) && Utility.
                        isWithinRange(
                                dateT0, m.getDate())).collect(Collectors.
                                summingDouble(
                                        CategoryChartData::getQuantity));

//            Calculate the growth
                double growthRate = Utility.calcGrowthRate(r12SalesT0,
                        r12SalesH12);

// Populate the Category Table List and round results to 3 significant figures
                double r12SalesT0Rounded = Utility.roundDouble(r12SalesT0, 3);
                double growthRateRounded = Utility.roundDouble(growthRate, 3);

                custGrpTableList.add(new CategoryTableData(cgr,
                        r12SalesT0Rounded, growthRateRounded, 0d,
                        0d)
                );

//            Sum total R12 sales volume
                totR12SalesT0 = totR12SalesT0 + r12SalesT0;
                totR12SalesH12 = totR12SalesH12 + r12SalesH12;
//            Calculate total R12 growth
                totR12Growth = Utility.calcGrowthRate(totR12SalesT0,
                        totR12SalesH12);

                //        Populate r12CustGrpSalesModel             
                r12CustGrpSalesModel.addSeries(r12Sales);
                r12Sales.setLabel(cgr);

            }
            /* *************** TABLE SUMMARY CALCULATIONS *************** */
//  Sort category list in decending order based on sales volume
            Collections.sort(custGrpTableList,
                    (CategoryTableData a, CategoryTableData b) -> b.getSales().
                            compareTo(a.getSales()));

            /*  Round total R12 Sales and Growth to 3 significant figures and 
            assign to class field. */
            this.totTop10CustGrpSales = Utility.roundDouble(totR12SalesT0, 3);
            this.totTop10CustGrpGrowth = Utility.roundDouble(totR12Growth, 3);

            /* *************** CHART PARAMETERS *************** */
            int myCounter = 0;
//            Keep Top-5 series for the line charts
            for (CategoryTableData c : custGrpTableList) {
//                Remove series after serie 5 (in descending order)
                if (myCounter > 4) {
                    String target = c.getCategory();
                    r12CustGrpSalesModel.getSeries().removeIf(p -> p.getLabel().
                            equals(target));
                }

                myCounter++;
            }

//        Set chart parameters for the sales volume chart
            r12CustGrpSalesModel.setLegendPosition("nw");
            r12CustGrpSalesModel.getAxis(AxisType.Y).setLabel("kPcs");
            r12CustGrpSalesModel.setSeriesColors(this.CHART_COLORS);
            DateAxis axis = new DateAxis("Dates");
            axis.setTickAngle(-50);
            axis.setMax(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
            axis.setTickFormat("%y-%b-%#d");
            r12CustGrpSalesModel.getAxes().put(AxisType.X, axis);
            r12CustGrpSalesModel.setAnimate(true);

        } catch (ClientException e) {
            System.err.println(
                    "Exception in 'populateR12CustomerGrpLineChartsAndTable method':" + e);
        }
    }

    /**
     * ================= ASSORTMENT GROUP CONTROLS =================
     *
     * Populate AssortmentGrp Map with data from database. The data is limited
     * to the Top-10 Assortment Groups based on Sales Volume in the last
     * 12-Month period.
     */
    private void populateAssortmentGrpSalesMap() {
        System.out.
                println(" I'm in the 'populateAssortmentGrpSalesMap' method.");
//        Accumulate sales volume from this date to find the largest assortment grps
        String startDate = Utility.makeStartDateLast12MonthSales();
        // code query here
        try {
            /* Query the ten biggest assortment groups in terms of sales volume 
            over the last 12 months */
            String whereStatement = makeCypherWhereStatementType2a();

            String tx = "MATCH (c:Customer)<-[r:FOR]-(t:Transaction)-[:BOOKED_AS]->(:ServiceCategory {name: {name}}),"
                    + " (cl:ClusterDB)<-[:MEMBER_OF]-(:MarketGroup)<-[:MEMBER_OF]-(m:MarketDB)-[:MADE]->(t)<-[:IN]-(a:Assortment)"
                    + whereStatement
                    + " WITH a.name AS assortment, SUM(r.quantity) AS TVolSales"
                    + " ORDER BY TVolSales DESC LIMIT 10" /* Here, set the number of top assortment groups */
                    /* Collect the assortment groups in a list */
                    + " RETURN collect(assortment) AS Assortments";

            StatementResult result = this.session.run(tx, Values.parameters(
                    "name", this.SERVICE_CATEGORY, "date", startDate,
                    "Clusters", this.clusters,
                    "assortmentGrpsBU", this.ASSORTMENT_GRPS_BU,
                    "CustGrps", this.customerGroups));

            while (result.hasNext()) {
                Record r = result.next();

                this.top10AssortmentGrps = r.get("Assortments").asList();
            }

            String whereStatement1 = makeCypherWhereStatementType2d();

            String tx1 = "MATCH (c:Customer)<-[r:FOR]-(t:Transaction)-[:BOOKED_AS]->(:ServiceCategory {name: {name}}),"
                    + " (cl:ClusterDB)<-[:MEMBER_OF]-(:MarketGroup)<-[:MEMBER_OF]-(m:MarketDB)-[:MADE]->(t)<-[:IN]-(a:Assortment)" /* Model based on Special Ledger */
                    + whereStatement1
                    + " RETURN t.year AS Year, t.month AS Month, a.name AS Asg, SUM(r.netSales)/1E6 AS NetSales, SUM(r.directCost)/1E6 AS DirectCost, SUM(r.quantity)/1E3 AS Quantity"
                    + " ORDER BY Year, Month";

            StatementResult result1 = this.session.run(tx1, Values.parameters(
                    "name", this.SERVICE_CATEGORY,
                    "Assortments", this.top10AssortmentGrps,
                    "Clusters", this.clusters,
                    "CustGrps", this.customerGroups));

            while (result1.hasNext()) {
                Record r = result1.next();

                int year = r.get("Year").asInt();
                int month = r.get("Month").asInt();
                String assortmentGrp = r.get("Asg").asString();
                double netSales = r.get("NetSales").asDouble();
                double directCost = r.get("DirectCost").asDouble();
                double quantity = r.get("Quantity").asDouble();

//                Make date
                LocalDate d = Utility.makeDate(year, month);
//                Make composite key
                String key = d + assortmentGrp;

//            Add results to Map
                assortmentSalesMap.put(key, new CategoryChartData(d,
                        assortmentGrp, netSales, directCost, quantity));
            }

//            Print Map contents
//        assortmentSalesMap.entrySet().stream().map((entry) -> entry.getValue()).forEachOrdered((v) -> {System.out.printf("%s;%s;%s;%s;%s\n", v.getDate(), v.getCategory(), v.getNetSales(), v.getDirectCost(), v.getQuantity());});
        } catch (ClientException e) {
            System.err.println(
                    "Exception in 'populateAssortmentGrpSalesMap':" + e);
        }
    }

    /**
     * Populate the Assortment Group Sales Line Charts and Data Table with
     * Rolling 12 data.
     */
    private void populateR12AssortmentGrpLineChartsAndTable() {
        System.out.println(
                "I'm in the 'populateR12AssortmentGrpLineChartsAndTable' method.");

//        Initiate totTop10AssortmentSales
        totTop10AssortmentSales = 0d;

//        Initiate r12AssortmentSalesModel
        r12AssortmentSalesModel = new LineChartModel();

        //        Calculate historical sales volume start dates to use in Growth calculation
        LocalDate dateT0 = Utility.makeDate(LocalDate.now().minusYears(1).
                getYear(), LocalDate.now().getMonthValue()
        );
        LocalDate dateH12 = Utility.makeDate(LocalDate.now().minusYears(2).
                getYear(), LocalDate.now().getMonthValue()
        );

//       R12 algorithm based on dates
//        Accumulate sales volume for each assortment group over rolling 12 periods
        int rollingPeriod = 12;
        assortmentCounter = 0;
        double totR12SalesT0 = 0d;
        double totR12SalesH12 = 0d;
        double totR12Growth = 0d;

        try {
            for (Object asg : top10AssortmentGrps) {
//                Initiate chart series and variables
                ChartSeries r12Sales = new ChartSeries(asg.toString());

                for (int i = 0; i <= (Utility.calcMonthsFromStart() - rollingPeriod + 1); i++) {
                    LocalDate date = Utility.calcStartDate().plusMonths(i).with(
                            TemporalAdjusters.lastDayOfMonth());

//                Collect and sum sales volume
                    Double salesVolumeR12 = assortmentSalesMap.values().stream().
                            filter(m -> m.getCategory().equals(asg)
                            && Utility.isWithinRange(date, m.getDate())).
                            collect(Collectors.summingDouble(
                                    CategoryChartData::getQuantity));

                    String chartDate = date.plusMonths(11).with(
                            TemporalAdjusters.lastDayOfMonth()).format(
                            DateTimeFormatter.ISO_DATE);

                    //        Add data to r12Sales series        
                    r12Sales.set(chartDate, salesVolumeR12);

                }
                /* *************** TABLE CALCULATIONS *************** */
//                Collect and sum sales volume from two years ago for growth calculation
                Double r12SalesH12 = assortmentSalesMap.values().stream().
                        filter(
                                m -> m.getCategory().equals(asg) && Utility.
                                isWithinRange(
                                        dateH12, m.getDate())).collect(
                                Collectors.
                                        summingDouble(
                                                CategoryChartData::getQuantity));

//                Collect and sum sales volume from one year ago for growth calculation
                Double r12SalesT0 = assortmentSalesMap.values().stream().filter(
                        m -> m.getCategory().equals(asg) && Utility.
                        isWithinRange(
                                dateT0, m.getDate())).collect(Collectors.
                                summingDouble(
                                        CategoryChartData::getQuantity));

//            Calculate the growth
                double growthRate = Utility.calcGrowthRate(r12SalesT0,
                        r12SalesH12);

// Populate the Category Table List and round results to 3 significant figures
                double r12SalesT0Rounded = Utility.roundDouble(r12SalesT0, 3);
                double growthRateRounded = Utility.roundDouble(growthRate, 3);

                assortmentTableList.add(new CategoryTableData(asg.toString(),
                        r12SalesT0Rounded, growthRateRounded, 0d,
                        0d)
                );

//            Sum total R12 sales volume
                totR12SalesT0 = totR12SalesT0 + r12SalesT0;
                totR12SalesH12 = totR12SalesH12 + r12SalesH12;
//            Calculate total R12 growth
                totR12Growth = Utility.calcGrowthRate(totR12SalesT0,
                        totR12SalesH12);

                //        Set number of assortment groups in the charts
                if (assortmentCounter < 5) {
                    //        Populate r12AssortmentSalesModel             
                    r12AssortmentSalesModel.addSeries(r12Sales);
                    r12Sales.setLabel(asg.toString());

                    assortmentCounter++;
                }
            }
            /* *************** TABLE SUMMARY CALCULATIONS *************** */
//  Sort category list in decending order based on sales volume
            Collections.sort(assortmentTableList,
                    (CategoryTableData a, CategoryTableData b) -> b.getSales().
                            compareTo(a.getSales()));

            /*  Round total R12 Sales and Growth to 3 significant figures and
            assign to class field. */
            this.totTop10AssortmentSales = Utility.roundDouble(totR12SalesT0, 3);
            this.totTop10AssortmentGrowth = Utility.roundDouble(totR12Growth, 3);

            /* *************** CHART PARAMETERS *************** */
//        Set chart parameters for the sales volume chart
            r12AssortmentSalesModel.setLegendPosition("nw");
            r12AssortmentSalesModel.getAxis(AxisType.Y).setLabel("kPcs");
            r12AssortmentSalesModel.setSeriesColors(this.CHART_COLORS);
            DateAxis axis = new DateAxis("Dates");
            axis.setTickAngle(-50);
            axis.setMax(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
            axis.setTickFormat("%y-%b-%#d");
            r12AssortmentSalesModel.getAxes().put(AxisType.X, axis);
            r12AssortmentSalesModel.setAnimate(true);

        } catch (ClientException e) {
            System.err.println(
                    "Exception in 'populateR12AssortmentGrpLineChartsAndTable method':" + e);
        }
    }

//    GETTERS & SETTERS
    public String getSelectedClustersInfo() {
        return selectedClustersInfo;
    }

    public boolean isIsCustGrpSelected() {
        return isCustGrpSelected;
    }

    public String getSelectedCustomerGroupInfo() {
        return selectedCustomerGroupInfo;
    }

    public LineChartModel getR12SalesModel() {
        return r12SalesModel;
    }

    public MeterGaugeChartModel getR12GrowthModel() {
        return r12GrowthModel;
    }

    public Double getGlobalGrowth() {
        return globalGrowth;
    }

    public Double getGlobalSales() {
        return globalSales;
    }

    public LineChartModel getR12MarketSalesModel() {
        return r12MarketSalesModel;
    }

    public List<CategoryTableData> getMarketTableList() {
        return marketTableList;
    }

    public Double getTotTop10MarketSales() {
        return totTop10MarketSales;
    }

    public Double getTotTop10MarketGrowth() {
        return totTop10MarketGrowth;
    }

    public Double getTotTop10CustGrpSales() {
        return totTop10CustGrpSales;
    }

    public Double getTotTop10CustGrpGrowth() {
        return totTop10CustGrpGrowth;
    }

    public LineChartModel getR12CustGrpSalesModel() {
        return r12CustGrpSalesModel;
    }

    public List<CategoryTableData> getCustGrpTableList() {
        return custGrpTableList;
    }

    public LineChartModel getR12AssortmentSalesModel() {
        return r12AssortmentSalesModel;
    }

    public List<CategoryTableData> getAssortmentTableList() {
        return assortmentTableList;
    }

    public Double getTotTop10AssortmentSales() {
        return totTop10AssortmentSales;
    }

    public Double getTotTop10AssortmentGrowth() {
        return totTop10AssortmentGrowth;
    }

}
