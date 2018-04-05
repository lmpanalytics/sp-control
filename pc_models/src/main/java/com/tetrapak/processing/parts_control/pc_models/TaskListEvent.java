/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control.pc_models;

/**
 * Models task list events such as Check, Change, Turn, and etc.
 *
 * @author SEPALMM
 */
public class TaskListEvent {

//    private final int machineNumber;
//    private final String label;
//    private final String classItem;
//    private final String articleNo;
//    private final String eqDenomination;
//    private final String type;
//    private final String docNo;
//    private final int interval;
    private final String action;
    private final String description;
    private final String sparePartNo;
    private final String spDenomination;
    private final int qty;
    private final String functionalArea;

    public TaskListEvent(String action, String description, String sparePartNo, String spDenomination, int qty, String functionalArea) {
        this.action = action;
        this.description = description;
        this.sparePartNo = sparePartNo;
        this.spDenomination = spDenomination;
        this.qty = qty;
        this.functionalArea = functionalArea;
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
