/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.dashboard.beans;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.PermitAll;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.faces.bean.SessionScoped;

import javax.inject.Named;

/**
 * This class models users and the html components to be rendered.
 *
 * @author SEPALMM
 */
@Named(value = "user")
@Stateful
@SessionScoped

@DeclareRoles(
        {"CENTRAL_TEAM", "BULF_DB", "BUICF_DB", "CPS_DB", "ALF_DB", "ECA_DB", "GC_DB", "GMEA_DB", "NCSA_DB", "SAEAO_DB"})
@PermitAll
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource
    SessionContext ctx;

    private boolean isCentralTeamUser;
    private boolean isBULF_DB_User;
    private boolean isBUICF_DB_User;
    private boolean isCPS_DB_User;
    private boolean isALF_DB_User;
    private boolean isECA_DB_User;
    private boolean isGC_DB_User;
    private boolean isGMEA_DB_User;
    private boolean isNCSA_DB_User;
    private boolean isSAEAO_DB_User;

    private boolean isRenderCluster_DB;
    private boolean isRenderCustGrp_DB;
    private boolean isRenderCategory_DB;

    private boolean isRenderBULF_DB;
    private boolean isRenderBUICF_DB;
    private boolean isRenderCPS_DB;
    private boolean isRenderALF_DB;

//    Constructor
    public User() {
    }

    @PostConstruct
    public void init() {
//        System.out.println("I'm in the 'User.init()' method.");

// Initiate user group classifiers
        isCentralTeamUser();
        isBULF_DB_User();
        isBUICF_DB_User();
        isCPS_DB_User();
        isALF_DB_User();
        isECA_DB_User();
        isGC_DB_User();
        isGMEA_DB_User();
        isNCSA_DB_User();
        isSAEAO_DB_User();

        // Initiate rendering of jsf components
        isRenderCluster_DB();
        isRenderCustGrp_DB();
        isRenderCategory_DB();
        isRenderBULF_DB();
        isRenderBUICF_DB();
        isRenderCPS_DB();
        isRenderALF_DB();

    }

    @PreDestroy
    public void destroyMe() {

    }

    //    DETERMINE USER
    public boolean isCentralTeamUser() {
        return isCentralTeamUser = ctx.isCallerInRole("CENTRAL_TEAM");
    }

    public boolean isBULF_DB_User() {
        return isBULF_DB_User = ctx.isCallerInRole("BULF_DB");
    }

    public boolean isBUICF_DB_User() {
        return isBUICF_DB_User = ctx.isCallerInRole("BUICF_DB");
    }

    public boolean isCPS_DB_User() {
        return isCPS_DB_User = ctx.isCallerInRole("CPS_DB");
    }

    public boolean isALF_DB_User() {
        return isALF_DB_User = ctx.isCallerInRole("ALF_DB");
    }

    public boolean isECA_DB_User() {
        return isECA_DB_User = ctx.isCallerInRole("ECA_DB");
    }

    public boolean isGC_DB_User() {
        return isGC_DB_User = ctx.isCallerInRole("GC_DB");
    }

    public boolean isGMEA_DB_User() {
        return isGMEA_DB_User = ctx.isCallerInRole("GMEA_DB");
    }

    public boolean isNCSA_DB_User() {
        return isNCSA_DB_User = ctx.isCallerInRole("NCSA_DB");
    }

    public boolean isSAEAO_DB_User() {
        return isSAEAO_DB_User = ctx.isCallerInRole("SAEAO_DB");
    }

//    Condition to render Cluster selections on index page
    public boolean isRenderCluster_DB() {
        if (isCentralTeamUser || isBULF_DB_User || isBUICF_DB_User || isCPS_DB_User || isALF_DB_User) {
            isRenderCluster_DB = true;
        }
        return isRenderCluster_DB;
    }

    //    Condition to render Customer Group selections on index page
    public boolean isRenderCustGrp_DB() {
        if (isCentralTeamUser) {
            isRenderCustGrp_DB = true;
        }
        return isRenderCustGrp_DB;
    }

//    Condition to render Service Category selections on index page
    public boolean isRenderCategory_DB() {
        if (isCentralTeamUser || isECA_DB_User || isGC_DB_User || isGMEA_DB_User || isNCSA_DB_User || isSAEAO_DB_User) {
            isRenderCategory_DB = true;
        }
        return isRenderCategory_DB;
    }

//    Condition to render BULF Report selections on index page    
    public boolean isRenderBULF_DB() {
        if (isCentralTeamUser || isBULF_DB_User || isECA_DB_User || isGC_DB_User || isGMEA_DB_User || isNCSA_DB_User || isSAEAO_DB_User) {
            isRenderBULF_DB = true;
        }
        return isRenderBULF_DB;
    }

    //    Condition to render BUICF Report selections on index page    
    public boolean isRenderBUICF_DB() {
        if (isCentralTeamUser || isBUICF_DB_User || isECA_DB_User || isGC_DB_User || isGMEA_DB_User || isNCSA_DB_User || isSAEAO_DB_User) {
            isRenderBUICF_DB = true;
        }
        return isRenderBUICF_DB;
    }

    //    Condition to render CPS Report selections on index page    
    public boolean isRenderCPS_DB() {
        if (isCentralTeamUser || isCPS_DB_User || isECA_DB_User || isGC_DB_User || isGMEA_DB_User || isNCSA_DB_User || isSAEAO_DB_User) {
            isRenderCPS_DB = true;
        }
        return isRenderCPS_DB;
    }

    //    Condition to render ALF Report selections on index page    
    public boolean isRenderALF_DB() {
        if (isCentralTeamUser || isALF_DB_User || isECA_DB_User || isGC_DB_User || isGMEA_DB_User || isNCSA_DB_User || isSAEAO_DB_User) {
            isRenderALF_DB = true;
        }
        return isRenderALF_DB;
    }
}
