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
    private int changeIntervalUL;
    private int changeIntervalLL;

    /**
     * Constructor
     *
     * @param annualRunningHours
     * @param changeIntervalUL upper limit
     * @param changeIntervalLL lower limit
     */
    public LogicParameters(int annualRunningHours, int changeIntervalUL, int changeIntervalLL) {
        this.annualRunningHours = annualRunningHours;
        this.changeIntervalUL = changeIntervalUL;
        this.changeIntervalLL = changeIntervalLL;
    }

    // Getters and Setters
    public int getAnnualRunningHours() {
        return annualRunningHours;
    }

    public void setAnnualRunningHours(int annualRunningHours) {
        this.annualRunningHours = annualRunningHours;
    }

    public int getChangeIntervalUL() {
        return changeIntervalUL;
    }

    public void setChangeIntervalUL(int changeIntervalUL) {
        this.changeIntervalUL = changeIntervalUL;
    }

    public int getChangeIntervalLL() {
        return changeIntervalLL;
    }

    public void setChangeIntervalLL(int changeIntervalLL) {
        this.changeIntervalLL = changeIntervalLL;
    }

}
