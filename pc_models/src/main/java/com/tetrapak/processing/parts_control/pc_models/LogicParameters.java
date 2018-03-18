/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control.pc_models;

/**
 * Models the logic parameters that the user sets as input.
 *
 * @author SEPALMM
 */
public class LogicParameters {

    private int annualRunningHours;
    private int actionIntervalUL;
    private int actionIntervalLL;

    /**
     * Constructor
     *
     * @param annualRunningHours
     * @param actionIntervalUL upper limit
     * @param actionIntervalLL lower limit
     */
    public LogicParameters(int annualRunningHours, int actionIntervalUL, int actionIntervalLL) {
        this.annualRunningHours = annualRunningHours;
        this.actionIntervalUL = actionIntervalUL;
        this.actionIntervalLL = actionIntervalLL;
    }

    // Getters and Setters
    public int getAnnualRunningHours() {
        return annualRunningHours;
    }

    public void setAnnualRunningHours(int annualRunningHours) {
        this.annualRunningHours = annualRunningHours;
    }

    public int getactionIntervalUL() {
        return actionIntervalUL;
    }

    public void setactionIntervalUL(int actionIntervalUL) {
        this.actionIntervalUL = actionIntervalUL;
    }

    public int getactionIntervalLL() {
        return actionIntervalLL;
    }

    public void setactionIntervalLL(int actionIntervalLL) {
        this.actionIntervalLL = actionIntervalLL;
    }

}
