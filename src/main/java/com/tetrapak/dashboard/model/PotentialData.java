/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.dashboard.model;

/**
 * This class models data for potential sales
 *
 * @author SEPALMM
 */
public class PotentialData {

    private double potSpareParts;
    private double potMaintenanceHrs;
    private double potMaintenance;

    public PotentialData(double potSpareParts, double potMaintenanceHrs,
            double potMaintenance) {
        this.potSpareParts = potSpareParts;
        this.potMaintenanceHrs = potMaintenanceHrs;
        this.potMaintenance = potMaintenance;
    }

    public double getPotSpareParts() {
        return potSpareParts;
    }

    public void setPotSpareParts(double potSpareParts) {
        this.potSpareParts = potSpareParts;
    }

    public double getPotMaintenanceHrs() {
        return potMaintenanceHrs;
    }

    public void setPotMaintenanceHrs(double potMaintenanceHrs) {
        this.potMaintenanceHrs = potMaintenanceHrs;
    }

    public double getPotMaintenance() {
        return potMaintenance;
    }

    public void setPotMaintenance(double potMaintenance) {
        this.potMaintenance = potMaintenance;
    }

}
