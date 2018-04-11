/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control.models;

import java.util.Objects;

/**
 * Models a task list formatted acc. to TPPS20057
 *
 * @author SEPALMM
 */
public class TaskList {

    private final String label;
    private final String classItem;
    private final String articleNo;
    private final String eqDenomination;
    private final String type;
    private final String docNo;
    private final int interval;
    private final String action;
    private final String description;
    private final String sparePartNo;
    private final String spDenomination;
    private final int qty;

    public TaskList(String label, String classItem, String articleNo, String eqDenomination, String type, String docNo, int interval, String action, String description, String sparePartNo, String spDenomination, int qty) {
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
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.label);
        hash = 71 * hash + Objects.hashCode(this.classItem);
        hash = 71 * hash + Objects.hashCode(this.articleNo);
        hash = 71 * hash + Objects.hashCode(this.eqDenomination);
        hash = 71 * hash + Objects.hashCode(this.type);
        hash = 71 * hash + Objects.hashCode(this.docNo);
        hash = 71 * hash + this.interval;
        hash = 71 * hash + Objects.hashCode(this.action);
        hash = 71 * hash + Objects.hashCode(this.description);
        hash = 71 * hash + Objects.hashCode(this.sparePartNo);
        hash = 71 * hash + Objects.hashCode(this.spDenomination);
        hash = 71 * hash + this.qty;
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
        final TaskList other = (TaskList) obj;
        if (this.interval != other.interval) {
            return false;
        }
        if (this.qty != other.qty) {
            return false;
        }
        if (!Objects.equals(this.label, other.label)) {
            return false;
        }
        if (!Objects.equals(this.classItem, other.classItem)) {
            return false;
        }
        if (!Objects.equals(this.articleNo, other.articleNo)) {
            return false;
        }
        if (!Objects.equals(this.eqDenomination, other.eqDenomination)) {
            return false;
        }
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.docNo, other.docNo)) {
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
        return true;
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

}
