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
    private int actionIntervalUL;
    private int actionIntervalLL;

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
        actionIntervalUL = 0;
        actionIntervalLL = 0;
    }

    public void compileParameters() {
        this.logicParameters = new LogicParameters(annualRunningHours, actionIntervalUL, actionIntervalLL);
    }

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

    public LogicParameters getLogicParameters() {
        return logicParameters;
    }

    public void setLogicParameters(LogicParameters logicParameters) {
        this.logicParameters = logicParameters;
    }

}
