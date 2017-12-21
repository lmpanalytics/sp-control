/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control.models;

/**
 * Models consolidated task list exported from SSPt
 *
 * @author SEPALMM
 */
public class TaskList {

    private int machineNumber;
    private String label;
    private String classItem;
    private String articleNo;
    private String eqDenomination;
    private String type;
    private String docNo;
    private int interval;
    private String action;
    private String description;
    private String sparePartNo;
    private String spDenomination;
    private int qty;
    private String functionalArea;

    public TaskList(int machineNumber, String label, String classItem, String articleNo, String eqDenomination, String type, String docNo, int interval, String action, String description, String sparePartNo, String spDenomination, int qty, String functionalArea) {
        this.machineNumber = machineNumber;
        this.label = label;
        this.classItem = classItem;
        this.articleNo = articleNo;
        this.eqDenomination = eqDenomination;
        this.type = type;
        this.docNo = docNo;
        this.interval = interval;
        this.action = action;
        this.description = description;
        this.sparePartNo = sparePartNo;
        this.spDenomination = spDenomination;
        this.qty = qty;
        this.functionalArea = functionalArea;
    }

    public int getMachineNumber() {
        return machineNumber;
    }

    public String getLabel() {
        return label;
    }

    public String getClassItem() {
        return classItem;
    }

    public String getArticleNo() {
        return articleNo;
    }

    public String getEqDenomination() {
        return eqDenomination;
    }

    public String getType() {
        return type;
    }

    public String getDocNo() {
        return docNo;
    }

    public int getInterval() {
        return interval;
    }

    public String getAction() {
        return action;
    }

    public String getDescription() {
        return description;
    }

    public String getSparePartNo() {
        return sparePartNo;
    }

    public String getSpDenomination() {
        return spDenomination;
    }

    public int getQty() {
        return qty;
    }

    public String getFunctionalArea() {
        return functionalArea;
    }

}
