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

    private int changeIntervalUL;
    private int changeIntervalLL;

    /**
     * Constructor
     *
     * @param changeIntervalUL upper limit
     * @param changeIntervalLL lower limit
     */
    public LogicParameters(int changeIntervalUL, int changeIntervalLL) {
        this.changeIntervalUL = changeIntervalUL;
        this.changeIntervalLL = changeIntervalLL;
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
