/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.dashboard.beans;

/**
 * This bean controls the Customer Group selections
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

@Named(value = "checkboxViewCustGroup")
@Stateful
@SessionScoped

@DeclareRoles(
        {"CENTRAL_TEAM", "BULF_DB", "BUICF_DB", "CPS_DB", "ALF_DB", "ECA_DB", "GC_DB", "GMEA_DB", "NCSA_DB", "SAEAO_DB"})
@PermitAll
public class CheckboxViewCustGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource
    SessionContext ctx;

    //        Avoid NPE
    private String[] selectedCustGroups = {"ALL CUSTOMER GROUPS"};
    private List<String> customerGroups;

    @PostConstruct
    public void init() {
//        Initiate cluster list
        customerGroups = new ArrayList<>();

//        Populate customerGroups
        customerGroups.add("COCA_COLA");
        customerGroups.add("DANONE");
        customerGroups.add("LACTALIS");
        customerGroups.add("NESTLE");
        customerGroups.add("PEPSICO");

    }

    public String[] getSelectedCustGroups() {
        return selectedCustGroups;
    }

    public void setSelectedCustGroups(String[] selectedCustGroups) {
        this.selectedCustGroups = selectedCustGroups;
    }

    public List<String> getCustomerGroups() {
        return customerGroups;
    }
}
