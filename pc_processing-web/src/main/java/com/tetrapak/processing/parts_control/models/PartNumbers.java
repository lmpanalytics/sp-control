/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control.models;

import java.util.Objects;

/**
 * Models partNumbers from DB
 *
 * @author SEPALMM
 */
public class PartNumbers {

    private String materialNumberNUM;
    private String materialNumberBW;
    private String materialNumberTP;

    /**
     * Constructor 1
     */
    public PartNumbers() {
    }

    /**
     * Constructor 2
     *
     * @param materialNumberNUM
     * @param materialNumberBW
     * @param materialNumberTP
     */
    public PartNumbers(String materialNumberNUM, String materialNumberBW, String materialNumberTP) {
        this.materialNumberNUM = materialNumberNUM;
        this.materialNumberBW = materialNumberBW;
        this.materialNumberTP = materialNumberTP;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.materialNumberNUM);
        hash = 53 * hash + Objects.hashCode(this.materialNumberBW);
        hash = 53 * hash + Objects.hashCode(this.materialNumberTP);
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
        final PartNumbers other = (PartNumbers) obj;
        if (!Objects.equals(this.materialNumberNUM, other.materialNumberNUM)) {
            return false;
        }
        if (!Objects.equals(this.materialNumberBW, other.materialNumberBW)) {
            return false;
        }
        if (!Objects.equals(this.materialNumberTP, other.materialNumberTP)) {
            return false;
        }
        return true;
    }

    public String getMaterialNumberNUM() {
        return materialNumberNUM;
    }

    public void setMaterialNumberNUM(String materialNumberNUM) {
        this.materialNumberNUM = materialNumberNUM;
    }

    public String getMaterialNumberBW() {
        return materialNumberBW;
    }

    public void setMaterialNumberBW(String materialNumberBW) {
        this.materialNumberBW = materialNumberBW;
    }

    public String getMaterialNumberTP() {
        return materialNumberTP;
    }

    public void setMaterialNumberTP(String materialNumberTP) {
        this.materialNumberTP = materialNumberTP;
    }

}
