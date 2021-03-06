/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control.pc_models;

import java.util.Objects;

/**
 * Models task list events such as Check, Change, Turn, and etc.
 *
 * @author SEPALMM
 */
public class TaskListEvent {

    private final String family;
//    private final String label;
//    private final String classItem;
//    private final String articleNo;
//    private final String eqDenomination;
//    private final String type;
//    private final String docNo;
//    private final int interval;
    private String action;
    private final String description;
    private final String sparePartNo;
    private final String spDenomination;
    private final int qty;
    private final String functionalArea;

    // Constructor
    public TaskListEvent(String family, String action, String description, String sparePartNo, String spDenomination, int qty, String functionalArea) {
        this.family = family;
        this.action = action;
        this.description = description;
        this.sparePartNo = sparePartNo;
        this.spDenomination = spDenomination;
        this.qty = qty;
        this.functionalArea = functionalArea;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.family);
        hash = 23 * hash + Objects.hashCode(this.action);
        hash = 23 * hash + Objects.hashCode(this.description);
        hash = 23 * hash + Objects.hashCode(this.sparePartNo);
        hash = 23 * hash + Objects.hashCode(this.spDenomination);
        hash = 23 * hash + this.qty;
        hash = 23 * hash + Objects.hashCode(this.functionalArea);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TaskListEvent other = (TaskListEvent) obj;
        if (this.qty != other.qty) {
            return false;
        }
        if (!Objects.equals(this.family, other.family)) {
            return false;
        }
        if (!Objects.equals(this.action, other.action)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!Objects.equals(this.sparePartNo, other.sparePartNo)) {
            return false;
        }
        if (!Objects.equals(this.spDenomination, other.spDenomination)) {
            return false;
        }
        if (!Objects.equals(this.functionalArea, other.functionalArea)) {
            return false;
        }
        return true;
    }

    public String getFamily() {
        return family;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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
