/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.bulf.beans;

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
@Named(value = "rp_SeparatorBean_eur")
@Stateful
@RequestScoped

@DeclareRoles(
        {"CENTRAL_TEAM", "BULF_DB", "BUICF_DB", "CPS_DB", "ALF_DB", "ECA_DB", "GC_DB", "GMEA_DB", "NCSA_DB", "SAEAO_DB"})
@RolesAllowed(
        {"CENTRAL_TEAM", "BULF_DB", "ECA_DB", "GC_DB", "GMEA_DB", "NCSA_DB", "SAEAO_DB"})
public class RP_SeparatorBean_eur implements Serializable {

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
    private List<Object> top10ReferenceParts;
    private Map<LocalDate, GlobalChartData> salesMap;
    private Map<String, CategoryChartData> marketSalesMap;
    private Map<String, CategoryChartData> custGrpSalesMap;
    private Map<String, CategoryChartData> referencePartSalesMap;

    private LineChartModel r12SalesModel;
    private LineChartModel r12MarginModel;
    private LineChartModel r12MarketSalesModel;
    private LineChartModel r12MarketMarginModel;
    private LineChartModel r12CustGrpSalesModel;
    private LineChartModel r12CustGrpMarginModel;
    private LineChartModel r12ReferencePartSalesModel;
    private LineChartModel r12ReferencePartMarginModel;
    private MeterGaugeChartModel r12GrowthModel;
    private List<CategoryTableData> marketTableList;
    private List<CategoryTableData> custGrpTableList;
    private List<CategoryTableData> referencePartTableList;
    private int marketCounter;
    private int referencePartCounter;
    private Double globalGrowth;
    private Double globalSales;
    private Double globalMargin;
    private Double totTop10MarketSales;
    private Double totTop10MarketGrowth;
    private Double totTop10MarketMargin;
    private Double totTop10CustGrpSales;
    private Double totTop10CustGrpGrowth;
    private Double totTop10CustGrpMargin;
    private Double totTop10ReferencePartSales;
    private Double totTop10ReferencePartGrowth;
    private Double totTop10ReferencePartMargin;
    private Session session;
    private Set<String> setOfCustGrps;
    private final String CHART_COLORS;
    private String[] clusters;
    private String[] customerGroups;
    private final String SERVICE_CATEGORY;
    private final String[] ASSORTMENT_GRPS_BU = {"Separator parts"};
    private String selectedClustersInfo;
    private String selectedCustomerGroupInfo;
    private boolean isCustGrpSelected;

    public RP_SeparatorBean_eur() {
        this.CHART_COLORS = "d7191c,fdae61,ffffbf,abd9e9,2c7bb6";
        this.SERVICE_CATEGORY = "Parts";

    }

    @PostConstruct
    public void init() {
        System.out.println("I'm in the 'RP_SeparatorBean_eur.init()' method.");

// INITIALIZE CLASS SPECIFIC MAPS AND FIELDS HERE
//      Initialize driver
        this.session = neo4jBean.getDriver().session();

//        Initialize the top-10 Markets list
        this.top10Markets = new LinkedList<>();

//        Initialize the top-10 Customer group list
        this.top10CustomerGrps = new LinkedList<>();

//        Initialize the set of Customer group list   
        this.setOfCustGrps = new LinkedHashSet<>();

//        Initialize the top-10 Reference Part group list
        this.top10ReferenceParts = new LinkedList<>();

//        Initialize the Sales map
        this.salesMap = new LinkedHashMap<>();

//        Initialize the marketSalesMap
        this.marketSalesMap = new LinkedHashMap<>();

//        Initialize the custGrpSalesMap
        this.custGrpSalesMap = new LinkedHashMap<>();

//        Initialize the referencePartSalesMap
        this.referencePartSalesMap = new LinkedHashMap<>();

//        Initialize the Market Table List
        this.marketTableList = new LinkedList<>();

//        Initialize the Customer Group Table List
        this.custGrpTableList = new LinkedList<>();

//        Initialize the Reference Part Table List
        this.referencePartTableList = new LinkedList<>();

//        Initialize and get cluster selections from the index page
        initiateClusterSelection();

//        Initialize and get customer group selections from the index page
        initiateCustomerGroupSelection();

//        Populate sales map with data from database
        populateSalesMap();

//        Populate the Global Sales & Margin Line Charts with Rolling 12 data
        populateR12LineCharts();

//        Populate Market Map
        populateMarketSalesMap();

//        Populate Customer Group Map
        populateCustomerGrpSalesMap();

//        Populate Reference Part Group Map
        populateReferencePartSalesMap();

//        Populate the Market Sales & Margin Line Charts with Rolling 12 data
        populateR12MarketLineChartsAndTable();

//        Populate the Customer Group Sales & Margin Line Charts with Rolling 12 data
        populateR12CustomerGrpLineChartsAndTable();

//        Populate the Reference Part Group Sales & Margin Line Charts with Rolling 12 data
        populateR12ReferencePartLineChartsAndTable();

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
     * Makes cypher 'WHERE statement' used in method 'populateSalesMap', to
     * select among combinations of Clusters and Customer groups.
     *
     * @return statement
     */
    private String makeCypherWhereStatementType1() {
        String whereStatement = "";
        if (this.clusters.length == 5 && (this.customerGroups[0].equals("")
                || this.customerGroups[0].equals("ALL CUSTOMER GROUPS"))) {
//                Use all clusters and customer groups
            whereStatement = " WHERE a.name IN {assortmentGrpsBU} AND ref.refMtrlName <> 'Other'";
        } else if (this.customerGroups[0].equals("")
                || this.customerGroups[0].equals("ALL CUSTOMER GROUPS")) {
            whereStatement = " WHERE a.name IN {assortmentGrpsBU} AND ref.refMtrlName <> 'Other' AND cl.name IN {Clusters}";
        } else {
            whereStatement = " WHERE a.name IN {assortmentGrpsBU} AND ref.refMtrlName <> 'Other' AND cl.name IN {Clusters} AND c.custGroup IN {CustGrps}";
        }
        return whereStatement;
    }

    /**
     * Makes cypher 'WHERE statement' used in methods 'populateMarketSalesMap',
     * 'populateCustomerGrpSalesMap', and populateReferencePartSalesMap to
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
            whereStatement = " WHERE (t.year + \"\" + t.month + \"\" + 01) >= {date} AND m.mktName = m.countryName AND a.name IN {assortmentGrpsBU} AND ref.refMtrlName <> 'Other'";
        } else if (this.customerGroups[0].equals("")
                || this.customerGroups[0].equals("ALL CUSTOMER GROUPS")) {
//            Use specific clusters but all customer groups
            whereStatement = " WHERE (t.year + \"\" + t.month + \"\" + 01) >= {date} AND m.mktName = m.countryName AND a.name IN {assortmentGrpsBU} AND ref.refMtrlName <> 'Other' AND cl.name IN {Clusters}";
        } else {
//            Use specific clusters and specific customer groups
            whereStatement = " WHERE (t.year + \"\" + t.month + \"\" + 01) >= {date} AND m.mktName = m.countryName AND a.name IN {assortmentGrpsBU} AND ref.refMtrlName <> 'Other' AND cl.name IN {Clusters} AND c.custGroup IN {CustGrps}";
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
            whereStatement = " WHERE c.custGroup IN {CustGrps} AND m.mktName IN {Markets} AND m.mktName = m.countryName AND a.name IN {assortmentGrpsBU} AND ref.refMtrlName <> 'Other'";
        } else {
//        Use Top10 markets and model based on Special Ledger
            whereStatement = " WHERE m.mktName IN {Markets} AND m.mktName = m.countryName AND a.name IN {assortmentGrpsBU} AND ref.refMtrlName <> 'Other'";
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
            whereStatement = " WHERE c.custGroup IN {CustGroups} AND m.mktName = m.countryName AND cl.name IN {Clusters} AND a.name IN {assortmentGrpsBU} AND ref.refMtrlName <> 'Other'";
        } else {
//       Include all Global Accounts as well, and Model based on Special Ledger
            whereStatement = " WHERE (c.custGroup IN {CustGroups} OR c.custType = 'Global Account') AND m.mktName = m.countryName AND cl.name IN {Clusters} AND a.name IN {assortmentGrpsBU} AND ref.refMtrlName <> 'Other'";
        }
        return whereStatement;
    }

    /**
     * Makes cypher 'WHERE statement' used in method
     * 'populateReferencePartSalesMap' to select among combinations of Clusters
     * and Customer groups. An active selection of Customer groups turns off the
     * compulsory viewing of customer types of type 'Global Accounts'.
     *
     * @return statement
     */
    private String makeCypherWhereStatementType2d() {
        String whereStatement = "";
        if (this.isCustGrpSelected) {
//           Use Top10 clusters, Include all selected customer groups, and model based on Special Ledger
            whereStatement = " WHERE c.custGroup IN {CustGrps} AND m.mktName = m.countryName AND cl.name IN {Clusters} AND a.name IN {assortmentGrpsBU} AND ref.refMtrlName <> 'Other'";
        } else {
//        Use Top10 markets and model based on Special Ledger
            whereStatement = " WHERE m.mktName = m.countryName AND cl.name IN {Clusters} AND a.name IN {assortmentGrpsBU} AND ref.refMtrlName <> 'Other'";
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
                    + " (ref:RefMaterial)-[:IN]->(t),"
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
     * Populate the Global Sales & Margin Line Charts with Rolling 12 data.
     */
    private void populateR12LineCharts() {
        System.out.println("I'm in the 'populateR12LineCharts()' method.");

//        Initiate r12SalesModel
        r12SalesModel = new LineChartModel();

//        Initiate r12MarginModel
        r12MarginModel = new LineChartModel();

//        Initiate r12GrowthModel
        r12GrowthModel = new MeterGaugeChartModel();

//        Calculate historical sales start dates to use in Growth calculation
        LocalDate dateT0 = Utility.makeDate(LocalDate.now().minusYears(1).
                getYear(), LocalDate.now().getMonthValue()
        );
        LocalDate dateH12 = Utility.makeDate(LocalDate.now().minusYears(2).
                getYear(), LocalDate.now().getMonthValue()
        );

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
                            GlobalChartData::getNetSales));

//                Collect and sum cost
            Double costR12 = salesMap.values().stream().filter(
                    m -> Utility.isWithinRange(date, m.getDate())).
                    collect(Collectors.summingDouble(
                            GlobalChartData::getDirectCost));

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

            /* *************** SUMMARY CALCULATIONS *************** */
//  Round R12 net sales to 3 significant figures and assign to class field
            this.globalSales = Utility.roundDouble(netSalesR12, 3);

//  Round R12 net margin to 3 significant figures and assign to class field
            this.globalMargin = Utility.roundDouble(margin, 3);
        }

//                Collect and sum sales from two years ago for growth calculation
        Double r12h12 = salesMap.values().stream().filter(
                m -> Utility.isWithinRange(dateH12, m.getDate())).
                collect(Collectors.summingDouble(GlobalChartData::getNetSales));

//                Collect and sum sales from one year ago for growth calculation
        Double r12t0 = salesMap.values().stream().filter(
                m -> Utility.isWithinRange(dateT0, m.getDate())).
                collect(Collectors.summingDouble(GlobalChartData::getNetSales));

        //            Calculate the growth
        double r12GrowthRate = Utility.calcGrowthRate(r12t0, r12h12);

//  Round R12 growth rate to 3 significant figures and assign to class field
        this.globalGrowth = Utility.roundDouble(r12GrowthRate, 3);

        /* *************** CHART PARAMETERS *************** */
        //        Populate r12SalesModel             
        r12SalesModel.addSeries(r12Sales);
        r12Sales.setLabel("Net Sales");

        //        Populate r12MarginModel             
        r12MarginModel.addSeries(r12Margin);
        r12Margin.setLabel("Net Margin");

//        Set chart parameters for the sales chart
        r12SalesModel.setLegendPosition("nw");
        r12SalesModel.getAxis(AxisType.Y).setLabel("MEur");
        r12SalesModel.getAxis(AxisType.Y).setTickFormat("%.1f");
        DateAxis axis = new DateAxis("Dates");
        axis.setTickAngle(-50);
        axis.setMax(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        axis.setTickFormat("%y-%b-%#d");
        r12SalesModel.getAxes().put(AxisType.X, axis);
        r12SalesModel.setAnimate(true);

//        Set chart parameters for the margin chart
        r12MarginModel.setLegendPosition("nw");
        r12MarginModel.getAxis(AxisType.Y).setLabel("Margin (%)");
        r12MarginModel.getAxis(AxisType.Y).setTickFormat("%.1f");
        DateAxis axis1 = new DateAxis("Dates");
        axis1.setTickAngle(-50);
        axis1.setMax(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        axis1.setTickFormat("%y-%b-%#d");
        r12MarginModel.getAxes().put(AxisType.X, axis1);
        r12MarginModel.setAnimate(true);

//      Set chart parameters for the MeterGauge chart 
        double maxscaleValue = 15d;
        List<Number> intervals = new ArrayList<Number>() {
            {
                add(5.0);
                add(7.2) /* Target */;
                add(maxscaleValue);
            }
        };
        r12GrowthModel = new MeterGaugeChartModel(this.globalGrowth, intervals);
        r12GrowthModel.setMax(maxscaleValue);
        r12GrowthModel.setMin(0d);
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
        String startDate = Utility.makeStartDateLast12MonthSales();
        // code query here
        try {
//  Query the ten biggest markets in terms of net sales over the last 12 months
            String whereStatement = makeCypherWhereStatementType2a();

            String tx = "MATCH (c:Customer)<-[r:FOR]-(t:Transaction)-[:BOOKED_AS]->(:ServiceCategory {name: {name}}),"
                    + " (cl:ClusterDB)<-[:MEMBER_OF]-(:MarketGroup)<-[:MEMBER_OF]-(m:MarketDB)-[:MADE]->(t),"
                    + " (a:Assortment)-[:IN]->(t),"
                    + " (ref:RefMaterial)-[:IN]->(t)"
                    + whereStatement
                    + " WITH m.mktName AS Market, SUM(r.netSales) AS TNetSales"
                    + " ORDER BY TNetSales DESC LIMIT 10" /* Here, set the number of top markets */
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
                    + " (a:Assortment)-[:IN]->(t),"
                    + " (ref:RefMaterial)-[:IN]->(t)"
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
     * Populate the Market Sales & Margin Line Charts and Data Table with
     * Rolling 12 data.
     */
    private void populateR12MarketLineChartsAndTable() {
        System.out.println("I'm in the 'populateR12MarketLineCharts' method.");

//        Initiate totTop10MarketSales
        totTop10MarketSales = 0d;

//        Initiate r12MarketSalesModel
        r12MarketSalesModel = new LineChartModel();

//        Initiate r12MarketMarginModel
        r12MarketMarginModel = new LineChartModel();

        //        Calculate historical sales start dates to use in Growth calculation
        LocalDate dateT0 = Utility.makeDate(LocalDate.now().minusYears(1).
                getYear(), LocalDate.now().getMonthValue()
        );
        LocalDate dateH12 = Utility.makeDate(LocalDate.now().minusYears(2).
                getYear(), LocalDate.now().getMonthValue()
        );

//       R12 algorithm based on dates
//        Accumulate sales and cost for each market over rolling 12 periods
        int rollingPeriod = 12;
        marketCounter = 0;
        double totR12SalesT0 = 0d;
        double totR12SalesH12 = 0d;
        double totR12Growth = 0d;
        double totR12CostT0 = 0d;
        double totR12Margin = 0d;

        try {
            for (Object mkt : top10Markets) {
//                Initiate chart series and variables
                ChartSeries r12Sales = new ChartSeries(mkt.toString());
                ChartSeries r12Margin = new ChartSeries(mkt.toString());

                for (int i = 0; i <= (Utility.calcMonthsFromStart() - rollingPeriod + 1); i++) {
                    LocalDate date = Utility.calcStartDate().plusMonths(i).with(
                            TemporalAdjusters.lastDayOfMonth());

//                Collect and sum sales
                    Double netSalesR12 = marketSalesMap.values().stream().
                            filter(
                                    m -> m.getCategory().equals(mkt)
                                    && Utility.isWithinRange(date, m.getDate())).
                            collect(Collectors.summingDouble(
                                    CategoryChartData::getNetSales));

//                Collect and sum cost
                    Double costR12 = marketSalesMap.values().stream().filter(
                            m -> m.getCategory().equals(mkt)
                            && Utility.isWithinRange(date, m.getDate())).
                            collect(Collectors.summingDouble(
                                    CategoryChartData::getDirectCost));

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
                /* *************** TABLE CALCULATIONS *************** */
//                Collect and sum sales from two years ago for growth calculation
                Double r12SalesH12 = marketSalesMap.values().stream().filter(
                        m -> m.getCategory().equals(mkt) && Utility.
                        isWithinRange(
                                dateH12, m.getDate())).collect(Collectors.
                                summingDouble(
                                        CategoryChartData::getNetSales));

//                Collect and sum sales from one year ago for growth calculation
                Double r12SalesT0 = marketSalesMap.values().stream().filter(
                        m -> m.getCategory().equals(mkt) && Utility.
                        isWithinRange(
                                dateT0, m.getDate())).collect(Collectors.
                                summingDouble(
                                        CategoryChartData::getNetSales));

//            Calculate the growth
                double growthRate = Utility.calcGrowthRate(r12SalesT0,
                        r12SalesH12);

//                Collect and sum cost from one year ago for margin calculation
                Double r12CostT0 = marketSalesMap.values().stream().filter(
                        m -> m.getCategory().equals(mkt) && Utility.
                        isWithinRange(
                                dateT0, m.getDate())).collect(Collectors.
                                summingDouble(
                                        CategoryChartData::getDirectCost));

//            Calculate the margin
                double margin = Utility.calcMargin(r12SalesT0,
                        r12CostT0);

// Populate the Category Table List and round results to 3 significant figures
                double r12SalesT0Rounded = Utility.roundDouble(r12SalesT0, 3);
                double growthRateRounded = Utility.roundDouble(growthRate, 3);
                double marginRounded = Utility.roundDouble(margin, 3);

                marketTableList.add(new CategoryTableData(mkt.toString(),
                        r12SalesT0Rounded, growthRateRounded, marginRounded, 0d)
                );

//            Sum total R12 sales
                totR12SalesT0 = totR12SalesT0 + r12SalesT0;
                totR12SalesH12 = totR12SalesH12 + r12SalesH12;
//            Calculate total R12 growth
                totR12Growth = Utility.calcGrowthRate(totR12SalesT0,
                        totR12SalesH12);
//            Sum total R12 cost
                totR12CostT0 = totR12CostT0 + r12CostT0;
//            Calculate R12 Margin
                totR12Margin = Utility.calcMargin(totR12SalesT0, totR12CostT0);

                //        Set number of markets in the charts
                if (marketCounter < 5) {
                    //        Populate r12MarketSalesModel             
                    r12MarketSalesModel.addSeries(r12Sales);
                    r12Sales.setLabel(mkt.toString());

                    //        Populate r12MarketMarginModel             
                    r12MarketMarginModel.addSeries(r12Margin);
                    r12Margin.setLabel(mkt.toString());
                    marketCounter++;
                }
            }
            /* *************** TABLE SUMMARY CALCULATIONS *************** */
//  Sort category list in decending order based on sales
            Collections.sort(marketTableList,
                    (CategoryTableData a, CategoryTableData b) -> b.getSales().
                            compareTo(a.getSales()));

            /*  Round total R12 Sales, Growth, and Margin to 3 
            significant figures and assign to class field. */
            this.totTop10MarketSales = Utility.roundDouble(totR12SalesT0, 3);
            this.totTop10MarketGrowth = Utility.roundDouble(totR12Growth, 3);
            this.totTop10MarketMargin = Utility.roundDouble(totR12Margin, 3);

            /* *************** CHART PARAMETERS *************** */
//        Set chart parameters for the sales chart
            r12MarketSalesModel.setLegendPosition("nw");
            r12MarketSalesModel.getAxis(AxisType.Y).setLabel("MEur");
            r12MarketSalesModel.getAxis(AxisType.Y).setTickFormat("%.1f");
            r12MarketSalesModel.setSeriesColors(this.CHART_COLORS);
            DateAxis axis = new DateAxis("Dates");
            axis.setTickAngle(-50);
            axis.setMax(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
            axis.setTickFormat("%y-%b-%#d");
            r12MarketSalesModel.getAxes().put(AxisType.X, axis);
            r12MarketSalesModel.setAnimate(true);

//        Set chart parameters for the margin chart
            r12MarketMarginModel.setLegendPosition("nw");
            r12MarketMarginModel.getAxis(AxisType.Y).setLabel("Margin (%)");
            r12MarketMarginModel.getAxis(AxisType.Y).setTickFormat("%.1f");
            r12MarketMarginModel.setSeriesColors(this.CHART_COLORS);
            DateAxis axis1 = new DateAxis("Dates");
            axis1.setTickAngle(-50);
            axis1.setMax(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
            axis1.setTickFormat("%y-%b-%#d");
            r12MarketMarginModel.getAxes().put(AxisType.X, axis1);
            r12MarketMarginModel.setAnimate(true);
        } catch (ClientException e) {
            System.err.println(
                    "Exception in 'populateR12MarketLineCharts method':" + e);
        }
    }

    /**
     * ================= CUSTOMER GROUP CONTROLS =================
     *
     * Populate CustomerGrp Map with data from database. The data is limited to
     * the Top-10 Customer Groups based on NetSales in the last 12-Month period,
     * and also override to include all Global Accounts.
     */
    private void populateCustomerGrpSalesMap() {
        System.out.
                println(" I'm in the 'populateCustomerGrpSalesMap()' method.");
//        Accumulate sales from this date to find the largest customers grps
        String startDate = Utility.makeStartDateLast12MonthSales();
        // code query here
        try {
            /* Query the ten biggest customer groups in terms of net sales 
            over the last 12 months */
            String whereStatement = makeCypherWhereStatementType2a();

            String tx = "MATCH (c:Customer)<-[r:FOR]-(t:Transaction)-[:BOOKED_AS]->(:ServiceCategory {name: {name}}),"
                    + " (cl:ClusterDB)<-[:MEMBER_OF]-(:MarketGroup)<-[:MEMBER_OF]-(m:MarketDB)-[:MADE]->(t),"
                    + " (a:Assortment)-[:IN]->(t),"
                    + " (ref:RefMaterial)-[:IN]->(t)"
                    + whereStatement
                    + " WITH c.custGroup AS CustGroup, SUM(r.netSales) AS TNetSales"
                    + " ORDER BY TNetSales DESC LIMIT 10" /* Here, set the number of top customer groups */
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
                    + " (a:Assortment)-[:IN]->(t),"
                    + " (ref:RefMaterial)-[:IN]->(t)"
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
//  Populate a Table Customer Grp List to be used in the sales table.
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
     * Populate the Customer Group Sales & Margin Line Charts and Data Table
     * with Rolling 12 data.
     */
    private void populateR12CustomerGrpLineChartsAndTable() {
        System.out.println(
                "I'm in the 'populateR12CustomerGrpLineChartsAndTable' method.");

//        Initiate totTop10CustGrpSales
        totTop10CustGrpSales = 0d;

//        Initiate r12CustGrpSalesModel
        r12CustGrpSalesModel = new LineChartModel();

//        Initiate r12CustGrpMarginModel
        r12CustGrpMarginModel = new LineChartModel();

        //        Calculate historical sales start dates to use in Growth calculation
        LocalDate dateT0 = Utility.makeDate(LocalDate.now().minusYears(1).
                getYear(), LocalDate.now().getMonthValue()
        );
        LocalDate dateH12 = Utility.makeDate(LocalDate.now().minusYears(2).
                getYear(), LocalDate.now().getMonthValue()
        );

//       R12 algorithm based on dates
//        Accumulate sales and cost for each customer group over rolling 12 periods
        int rollingPeriod = 12;
        double totR12SalesT0 = 0d;
        double totR12SalesH12 = 0d;
        double totR12Growth = 0d;
        double totR12CostT0 = 0d;
        double totR12Margin = 0d;

        try {
//            Convert set of customer groups to list
            List<String> listOfCustGrps = new LinkedList<>(this.setOfCustGrps);

//            Loop through the customer groups
            for (String cgr : listOfCustGrps) {
//                Initiate chart series and variables
                ChartSeries r12Sales = new ChartSeries(cgr);
                ChartSeries r12Margin = new ChartSeries(cgr);

                for (int i = 0; i <= (Utility.calcMonthsFromStart() - rollingPeriod + 1); i++) {
                    LocalDate date = Utility.calcStartDate().plusMonths(i).with(
                            TemporalAdjusters.lastDayOfMonth());

//                Collect and sum sales
                    Double netSalesR12 = custGrpSalesMap.values().stream().
                            filter(m -> m.getCategory().equals(cgr)
                            && Utility.isWithinRange(date, m.getDate())).
                            collect(Collectors.summingDouble(
                                    CategoryChartData::getNetSales));

//                Collect and sum cost
                    Double costR12 = custGrpSalesMap.values().stream().filter(
                            m -> m.getCategory().equals(cgr)
                            && Utility.isWithinRange(date, m.getDate())).
                            collect(Collectors.summingDouble(
                                    CategoryChartData::getDirectCost));

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
                /* *************** TABLE CALCULATIONS *************** */
//                Collect and sum sales from two years ago for growth calculation
                Double r12SalesH12 = custGrpSalesMap.values().stream().filter(
                        m -> m.getCategory().equals(cgr) && Utility.
                        isWithinRange(
                                dateH12, m.getDate())).collect(Collectors.
                                summingDouble(
                                        CategoryChartData::getNetSales));

//                Collect and sum sales from one year ago for growth calculation
                Double r12SalesT0 = custGrpSalesMap.values().stream().filter(
                        m -> m.getCategory().equals(cgr) && Utility.
                        isWithinRange(
                                dateT0, m.getDate())).collect(Collectors.
                                summingDouble(
                                        CategoryChartData::getNetSales));

//            Calculate the growth
                double growthRate = Utility.calcGrowthRate(r12SalesT0,
                        r12SalesH12);

//                Collect and sum cost from one year ago for margin calculation
                Double r12CostT0 = custGrpSalesMap.values().stream().filter(
                        m -> m.getCategory().equals(cgr) && Utility.
                        isWithinRange(
                                dateT0, m.getDate())).collect(Collectors.
                                summingDouble(
                                        CategoryChartData::getDirectCost));

//            Calculate the margin
                double margin = Utility.calcMargin(r12SalesT0,
                        r12CostT0);

// Populate the Category Table List and round results to 3 significant figures
                double r12SalesT0Rounded = Utility.roundDouble(r12SalesT0, 3);
                double growthRateRounded = Utility.roundDouble(growthRate, 3);
                double marginRounded = Utility.roundDouble(margin, 3);

                custGrpTableList.add(new CategoryTableData(cgr,
                        r12SalesT0Rounded, growthRateRounded, marginRounded,
                        0d)
                );

//            Sum total R12 sales
                totR12SalesT0 = totR12SalesT0 + r12SalesT0;
                totR12SalesH12 = totR12SalesH12 + r12SalesH12;
//            Calculate total R12 growth
                totR12Growth = Utility.calcGrowthRate(totR12SalesT0,
                        totR12SalesH12);
//            Sum total R12 cost
                totR12CostT0 = totR12CostT0 + r12CostT0;
//            Calculate R12 Margin
                totR12Margin = Utility.calcMargin(totR12SalesT0, totR12CostT0);

                //        Populate r12CustGrpSalesModel             
                r12CustGrpSalesModel.addSeries(r12Sales);
                r12Sales.setLabel(cgr);

                //        Populate r12CustGrpMarginModel             
                r12CustGrpMarginModel.addSeries(r12Margin);
                r12Margin.setLabel(cgr);

            }
            /* *************** TABLE SUMMARY CALCULATIONS *************** */
//  Sort category list in decending order based on sales
            Collections.sort(custGrpTableList,
                    (CategoryTableData a, CategoryTableData b) -> b.getSales().
                            compareTo(a.getSales()));

            /*  Round total R12 Sales, Growth, and Margin to 3 
            significant figures and assign to class field. */
            this.totTop10CustGrpSales = Utility.roundDouble(totR12SalesT0, 3);
            this.totTop10CustGrpGrowth = Utility.roundDouble(totR12Growth, 3);
            this.totTop10CustGrpMargin = Utility.roundDouble(totR12Margin, 3);

            /* *************** CHART PARAMETERS *************** */
            int myCounter = 0;
//            Keep Top-5 series for the line charts
            for (CategoryTableData c : custGrpTableList) {
//                Remove series after serie 5 (in descending order)
                if (myCounter > 4) {
                    String target = c.getCategory();
                    r12CustGrpSalesModel.getSeries().removeIf(p -> p.getLabel().
                            equals(target));
                    r12CustGrpMarginModel.getSeries().removeIf(
                            p -> p.getLabel().equals(target));
                }

                myCounter++;
            }

//        Set chart parameters for the sales chart
            r12CustGrpSalesModel.setLegendPosition("nw");
            r12CustGrpSalesModel.getAxis(AxisType.Y).setLabel("MEur");
            r12CustGrpSalesModel.getAxis(AxisType.Y).setTickFormat("%.1f");
            r12CustGrpSalesModel.setSeriesColors(this.CHART_COLORS);
            DateAxis axis = new DateAxis("Dates");
            axis.setTickAngle(-50);
            axis.setMax(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
            axis.setTickFormat("%y-%b-%#d");
            r12CustGrpSalesModel.getAxes().put(AxisType.X, axis);
            r12CustGrpSalesModel.setAnimate(true);

//        Set chart parameters for the margin chart
            r12CustGrpMarginModel.setLegendPosition("nw");
            r12CustGrpMarginModel.getAxis(AxisType.Y).setLabel("Margin (%)");
            r12CustGrpMarginModel.getAxis(AxisType.Y).setTickFormat("%.1f");
            r12CustGrpMarginModel.setSeriesColors(this.CHART_COLORS);
            DateAxis axis1 = new DateAxis("Dates");
            axis1.setTickAngle(-50);
            axis1.setMax(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
            axis1.setTickFormat("%y-%b-%#d");
            r12CustGrpMarginModel.getAxes().put(AxisType.X, axis1);
            r12CustGrpMarginModel.setAnimate(true);
        } catch (ClientException e) {
            System.err.println(
                    "Exception in 'populateR12CustomerGrpLineChartsAndTable method':" + e);
        }
    }

    /**
     * ================= REFERENCE PART CONTROLS =================
     *
     * Populate Reference Part Map with data from database. The data is limited
     * to the Top-10 Reference Parts based on NetSales in the last 12-Month
     * period.
     */
    private void populateReferencePartSalesMap() {
        System.out.
                println(" I'm in the 'populateReferencePartSalesMap' method.");
//        Accumulate sales from this date to find the largest Reference part grps
        String startDate = Utility.makeStartDateLast12MonthSales();
        // code query here
        try {
            /* Query the ten biggest Reference Parts in terms of net sales 
            over the last 12 months */
            String whereStatement = makeCypherWhereStatementType2a();

            String tx = "MATCH (c:Customer)<-[r:FOR]-(t:Transaction)-[:BOOKED_AS]->(:ServiceCategory {name: {name}}),"
                    + " (cl:ClusterDB)<-[:MEMBER_OF]-(:MarketGroup)<-[:MEMBER_OF]-(m:MarketDB)-[:MADE]->(t)<-[:IN]-(a:Assortment),"
                    + " (ref:RefMaterial)-[:IN]->(t)"
                    + whereStatement
                    + " WITH ref.refMtrlName AS refPart, SUM(r.netSales) AS TNetSales"
                    + " ORDER BY TNetSales DESC LIMIT 10" /* Here, set the number of top Reference Parts */
                    /* Collect the Reference Parts in a list */
                    + " RETURN collect(refPart) AS ReferenceParts";

            StatementResult result = this.session.run(tx, Values.parameters(
                    "name", this.SERVICE_CATEGORY, "date", startDate,
                    "Clusters", this.clusters,
                    "assortmentGrpsBU", this.ASSORTMENT_GRPS_BU,
                    "CustGrps", this.customerGroups));

            while (result.hasNext()) {
                Record r = result.next();

                this.top10ReferenceParts = r.get("ReferenceParts").asList();
            }

            String whereStatement1 = makeCypherWhereStatementType2d();

            String tx1 = "MATCH (c:Customer)<-[r:FOR]-(t:Transaction)-[:BOOKED_AS]->(:ServiceCategory {name: {name}}),"
                    + " (cl:ClusterDB)<-[:MEMBER_OF]-(:MarketGroup)<-[:MEMBER_OF]-(m:MarketDB)-[:MADE]->(t)<-[:IN]-(a:Assortment)," /* Model based on Special Ledger */
                    + " (t)<-[:IN]-(ref:RefMaterial)"
                    + whereStatement1
                    + " RETURN t.year AS Year, t.month AS Month, ref.refMtrlName AS RefPrts, SUM(r.netSales)/1E6 AS NetSales, SUM(r.directCost)/1E6 AS DirectCost, SUM(r.quantity)/1E3 AS Quantity"
                    + " ORDER BY Year, Month";

            StatementResult result1 = this.session.run(tx1, Values.parameters(
                    "name", this.SERVICE_CATEGORY,
                    "ReferenceParts", this.top10ReferenceParts,
                    "Clusters", this.clusters,
                    "assortmentGrpsBU", this.ASSORTMENT_GRPS_BU,
                    "CustGrps", this.customerGroups));

            while (result1.hasNext()) {
                Record r = result1.next();

                int year = r.get("Year").asInt();
                int month = r.get("Month").asInt();
                String referenceParts = r.get("RefPrts").asString();
                double netSales = r.get("NetSales").asDouble();
                double directCost = r.get("DirectCost").asDouble();
                double quantity = r.get("Quantity").asDouble();

//                Make date
                LocalDate d = Utility.makeDate(year, month);
//                Make composite key
                String key = d + referenceParts;

//            Add results to Map
                referencePartSalesMap.put(key, new CategoryChartData(d,
                        referenceParts, netSales, directCost, quantity));
            }

//            Print Map contents
//        referencePartSalesMap.entrySet().stream().map((entry) -> entry.getValue()).forEachOrdered((v) -> {System.out.printf("%s;%s;%s;%s;%s\n", v.getDate(), v.getCategory(), v.getNetSales(), v.getDirectCost(), v.getQuantity());});
        } catch (ClientException e) {
            System.err.println(
                    "Exception in 'populateReferencePartSalesMap':" + e);
        }
    }

    /**
     * Populate the Reference Part Sales & Margin Line Charts and Data Table
     * with Rolling 12 data.
     */
    private void populateR12ReferencePartLineChartsAndTable() {
        System.out.println(
                "I'm in the 'populateR12ReferencePartLineChartsAndTable' method.");

//        Initiate totTop10ReferencePartSales
        totTop10ReferencePartSales = 0d;

//        Initiate r12ReferencePartSalesModel
        r12ReferencePartSalesModel = new LineChartModel();

//        Initiate r12ReferencePartMarginModel
        r12ReferencePartMarginModel = new LineChartModel();

        //        Calculate historical sales start dates to use in Growth calculation
        LocalDate dateT0 = Utility.makeDate(LocalDate.now().minusYears(1).
                getYear(), LocalDate.now().getMonthValue()
        );
        LocalDate dateH12 = Utility.makeDate(LocalDate.now().minusYears(2).
                getYear(), LocalDate.now().getMonthValue()
        );

//       R12 algorithm based on dates
//        Accumulate sales and cost for each Reference Part over rolling 12 periods
        int rollingPeriod = 12;
        referencePartCounter = 0;
        double totR12SalesT0 = 0d;
        double totR12SalesH12 = 0d;
        double totR12Growth = 0d;
        double totR12CostT0 = 0d;
        double totR12Margin = 0d;

        try {
            for (Object ref : top10ReferenceParts) {
//                Initiate chart series and variables
                ChartSeries r12Sales = new ChartSeries(ref.toString());
                ChartSeries r12Margin = new ChartSeries(ref.toString());

                for (int i = 0; i <= (Utility.calcMonthsFromStart() - rollingPeriod + 1); i++) {
                    LocalDate date = Utility.calcStartDate().plusMonths(i).with(
                            TemporalAdjusters.lastDayOfMonth());

//                Collect and sum sales
                    Double netSalesR12 = referencePartSalesMap.values().stream().
                            filter(m -> m.getCategory().equals(ref)
                            && Utility.isWithinRange(date, m.getDate())).
                            collect(Collectors.summingDouble(
                                    CategoryChartData::getNetSales));

//                Collect and sum cost
                    Double costR12 = referencePartSalesMap.values().stream().
                            filter(
                                    m -> m.getCategory().equals(ref)
                                    && Utility.isWithinRange(date, m.getDate())).
                            collect(Collectors.summingDouble(
                                    CategoryChartData::getDirectCost));

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
                /* *************** TABLE CALCULATIONS *************** */
//                Collect and sum sales from two years ago for growth calculation
                Double r12SalesH12 = referencePartSalesMap.values().stream().
                        filter(
                                m -> m.getCategory().equals(ref) && Utility.
                                isWithinRange(
                                        dateH12, m.getDate())).collect(
                                Collectors.
                                        summingDouble(
                                                CategoryChartData::getNetSales));

//                Collect and sum sales from one year ago for growth calculation
                Double r12SalesT0 = referencePartSalesMap.values().stream().
                        filter(
                                m -> m.getCategory().equals(ref) && Utility.
                                isWithinRange(
                                        dateT0, m.getDate())).collect(
                                Collectors.
                                        summingDouble(
                                                CategoryChartData::getNetSales));

//            Calculate the growth
                double growthRate = Utility.calcGrowthRate(r12SalesT0,
                        r12SalesH12);

//                Collect and sum cost from one year ago for margin calculation
                Double r12CostT0 = referencePartSalesMap.values().stream().
                        filter(
                                m -> m.getCategory().equals(ref) && Utility.
                                isWithinRange(
                                        dateT0, m.getDate())).collect(
                                Collectors.
                                        summingDouble(
                                                CategoryChartData::getDirectCost));

//            Calculate the margin
                double margin = Utility.calcMargin(r12SalesT0,
                        r12CostT0);

// Populate the Category Table List and round results to 3 significant figures
                double r12SalesT0Rounded = Utility.roundDouble(r12SalesT0, 3);
                double growthRateRounded = Utility.roundDouble(growthRate, 3);
                double marginRounded = Utility.roundDouble(margin, 3);

                referencePartTableList.add(new CategoryTableData(ref.toString(),
                        r12SalesT0Rounded, growthRateRounded, marginRounded,
                        0d)
                );

//            Sum total R12 sales
                totR12SalesT0 = totR12SalesT0 + r12SalesT0;
                totR12SalesH12 = totR12SalesH12 + r12SalesH12;
//            Calculate total R12 growth
                totR12Growth = Utility.calcGrowthRate(totR12SalesT0,
                        totR12SalesH12);
//            Sum total R12 cost
                totR12CostT0 = totR12CostT0 + r12CostT0;
//            Calculate R12 Margin
                totR12Margin = Utility.calcMargin(totR12SalesT0, totR12CostT0);

                //        Set number of Reference Parts in the charts
                if (referencePartCounter < 5) {
                    //        Populate r12ReferencePartSalesModel             
                    r12ReferencePartSalesModel.addSeries(r12Sales);
                    r12Sales.setLabel(ref.toString());

                    //        Populate r12ReferencePartMarginModel             
                    r12ReferencePartMarginModel.addSeries(r12Margin);
                    r12Margin.setLabel(ref.toString());
                    referencePartCounter++;
                }
            }
            /* *************** TABLE SUMMARY CALCULATIONS *************** */
//  Sort category list in decending order based on sales
            Collections.sort(referencePartTableList,
                    (CategoryTableData a, CategoryTableData b) -> b.getSales().
                            compareTo(a.getSales()));

            /*  Round total R12 Sales, Growth, and Margin to 3 
            significant figures and assign to class field. */
            this.totTop10ReferencePartSales = Utility.roundDouble(totR12SalesT0,
                    3);
            this.totTop10ReferencePartGrowth = Utility.roundDouble(totR12Growth,
                    3);
            this.totTop10ReferencePartMargin = Utility.roundDouble(totR12Margin,
                    3);

            /* *************** CHART PARAMETERS *************** */
//        Set chart parameters for the sales chart
            r12ReferencePartSalesModel.setLegendPosition("nw");
            r12ReferencePartSalesModel.getAxis(AxisType.Y).setLabel("MEur");
            r12ReferencePartSalesModel.getAxis(AxisType.Y).setTickFormat("%.1f");
            r12ReferencePartSalesModel.setSeriesColors(this.CHART_COLORS);
            DateAxis axis = new DateAxis("Dates");
            axis.setTickAngle(-50);
            axis.setMax(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
            axis.setTickFormat("%y-%b-%#d");
            r12ReferencePartSalesModel.getAxes().put(AxisType.X, axis);
            r12ReferencePartSalesModel.setAnimate(true);

//        Set chart parameters for the margin chart
            r12ReferencePartMarginModel.setLegendPosition("nw");
            r12ReferencePartMarginModel.getAxis(AxisType.Y).setLabel(
                    "Margin (%)");
            r12ReferencePartMarginModel.getAxis(AxisType.Y).
                    setTickFormat("%.1f");
            r12ReferencePartMarginModel.setSeriesColors(this.CHART_COLORS);
            DateAxis axis1 = new DateAxis("Dates");
            axis1.setTickAngle(-50);
            axis1.setMax(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
            axis1.setTickFormat("%y-%b-%#d");
            r12ReferencePartMarginModel.getAxes().put(AxisType.X, axis1);
            r12ReferencePartMarginModel.setAnimate(true);
        } catch (ClientException e) {
            System.err.println(
                    "Exception in 'populateR12ReferencePartLineChartsAndTable method':" + e);
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

    public LineChartModel getR12MarginModel() {
        return r12MarginModel;
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

    public Double getGlobalMargin() {
        return globalMargin;
    }

    public LineChartModel getR12MarketSalesModel() {
        return r12MarketSalesModel;
    }

    public LineChartModel getR12MarketMarginModel() {
        return r12MarketMarginModel;
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

    public Double getTotTop10MarketMargin() {
        return totTop10MarketMargin;
    }

    public Double getTotTop10CustGrpSales() {
        return totTop10CustGrpSales;
    }

    public Double getTotTop10CustGrpGrowth() {
        return totTop10CustGrpGrowth;
    }

    public Double getTotTop10CustGrpMargin() {
        return totTop10CustGrpMargin;
    }

    public LineChartModel getR12CustGrpSalesModel() {
        return r12CustGrpSalesModel;
    }

    public LineChartModel getR12CustGrpMarginModel() {
        return r12CustGrpMarginModel;
    }

    public List<CategoryTableData> getCustGrpTableList() {
        return custGrpTableList;
    }

    public LineChartModel getR12ReferencePartSalesModel() {
        return r12ReferencePartSalesModel;
    }

    public LineChartModel getR12ReferencePartMarginModel() {
        return r12ReferencePartMarginModel;
    }

    public List<CategoryTableData> getReferencePartTableList() {
        return referencePartTableList;
    }

    public Double getTotTop10ReferencePartSales() {
        return totTop10ReferencePartSales;
    }

    public Double getTotTop10ReferencePartGrowth() {
        return totTop10ReferencePartGrowth;
    }

    public Double getTotTop10ReferencePartMargin() {
        return totTop10ReferencePartMargin;
    }

}
