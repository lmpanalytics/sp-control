/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control.pc_models;

/**
 * Models Materials
 *
 * @author SEPALMM
 */
public class Material {

    private String assortmentGroup;
    private String description;
    private String materialNumber;
    private String mpg;
    private double pg;

    // Constructor
    public Material(String assortmentGroup, String description, String materialNumber, String mpg, double pg) {
        this.assortmentGroup = assortmentGroup;
        this.description = description;
        this.materialNumber = materialNumber;
        this.mpg = mpg;
        this.pg = pg;
    }

    public String getAssortmentGroup() {
        return assortmentGroup;
    }

    public void setAssortmentGroup(String assortmentGroup) {
        this.assortmentGroup = assortmentGroup;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMaterialNumber() {
        return materialNumber;
    }

    public void setMaterialNumber(String materialNumber) {
        this.materialNumber = materialNumber;
    }

    public String getMpg() {
        return mpg;
    }

    public void setMpg(String mpg) {
        this.mpg = mpg;
    }

    public double getPg() {
        return pg;
    }

    public void setPg(double pg) {
        this.pg = pg;
    }

}
