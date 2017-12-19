/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control.beans;

import com.tetrapak.processing.parts_control.pc_models.LogicParameters;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import javax.annotation.PostConstruct;

/**
 *
 * @author SEPALMM
 */
@Named(value = "logicParametersViewBean")
@SessionScoped
public class LogicParametersViewBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private int annualRunningHours;
    private int changeIntervalUL;
    private int changeIntervalLL;

    private LogicParameters logicParameters;

    /**
     * Creates a new instance of LogicParametersViewBean
     */
    public LogicParametersViewBean() {
    }

    @PostConstruct
    public void init() {
        // INITIATE CLASS SPECIFIC MAPS AND FIELDS HERE - THE ORDER IS IMPORTANT
        annualRunningHours = 0;
        changeIntervalUL = 0;
        changeIntervalLL = 0;

    }

    public void compileParameters(int annualRunningHours, int changeIntervalUL, int changeIntervalLL) {
        this.logicParameters = new LogicParameters(annualRunningHours, changeIntervalUL, changeIntervalLL);
    }

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

    public LogicParameters getLogicParameters() {
        return logicParameters;
    }

    public void setLogicParameters(LogicParameters logicParameters) {
        this.logicParameters = logicParameters;
    }

}
