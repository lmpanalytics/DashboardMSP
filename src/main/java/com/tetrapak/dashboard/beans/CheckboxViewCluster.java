/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.dashboard.beans;

/**
 * This bean controls the cluster selections
 *
 * @author SEPALMM
 */
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.PermitAll;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

@Named(value = "checkboxViewCluster")
@Stateful
@SessionScoped

@DeclareRoles(
        {"CENTRAL_TEAM", "BULF_DB", "BUICF_DB", "CPS_DB", "ALF_DB", "ECA_DB", "GC_DB", "GMEA_DB", "NCSA_DB", "SAEAO_DB"})
@PermitAll
public class CheckboxViewCluster implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource
    SessionContext ctx;

    //        Avoid NPE
    private String[] selectedClusters = {"0"};
    private List<String> clusters;

    @PostConstruct
    public void init() {
//        Initiate cluster list
        clusters = new ArrayList<>();

//        Populate clusters
        if (ctx.isCallerInRole("ECA_DB")) {
            clusters.add("E&CA");
        } else if (ctx.isCallerInRole("GC_DB")) {
            clusters.add("GC");
        } else if (ctx.isCallerInRole("GMEA_DB")) {
            clusters.add("GME&A");
        } else if (ctx.isCallerInRole("NCSA_DB")) {
            clusters.add("NC&SA");
        } else if (ctx.isCallerInRole("SAEAO_DB")) {
            clusters.add("SAEA&O");
        } else {
//        Fully populate clusters to non-cluster users
            clusters.add("E&CA");
            clusters.add("GC");
            clusters.add("GME&A");
            clusters.add("NC&SA");
            clusters.add("SAEA&O");
        }
    }

    public String[] getSelectedClusters() {
        return selectedClusters;
    }

    public void setSelectedClusters(String[] selectedClusters) {
        this.selectedClusters = selectedClusters;
    }

    public List<String> getClusters() {
        return clusters;
    }
}
