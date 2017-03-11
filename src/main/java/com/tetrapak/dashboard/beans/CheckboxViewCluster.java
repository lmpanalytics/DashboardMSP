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
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

@Named(value = "checkboxViewCluster")
@SessionScoped
public class CheckboxViewCluster implements Serializable {

    private static final long serialVersionUID = 1L;

    //        Avoid NPE
    private String[] selectedClusters = {"0"};
    private List<String> clusters;

    @PostConstruct
    public void init() {
        clusters = new ArrayList<>();
        clusters.add("E&CA");
        clusters.add("GC");
        clusters.add("GME&A");
        clusters.add("NC&SA");
        clusters.add("SAEA&O");
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
