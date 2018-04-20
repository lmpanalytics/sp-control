/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control.models;

import java.util.Objects;

/**
 *
 * @author SEPALMM
 */
public class ImportMaterial {

    private String partNumber;
    private String materialDescription;

    public ImportMaterial(String partNumber, String materialDescription) {
        this.partNumber = partNumber;
        this.materialDescription = materialDescription;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getMaterialDescription() {
        return materialDescription;
    }

    public void setMaterialDescription(String materialDescription) {
        this.materialDescription = materialDescription;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Objects.hashCode(this.partNumber);
        hash = 41 * hash + Objects.hashCode(this.materialDescription);
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
        final ImportMaterial other = (ImportMaterial) obj;
        if (!Objects.equals(this.partNumber, other.partNumber)) {
            return false;
        }
        if (!Objects.equals(this.materialDescription, other.materialDescription)) {
            return false;
        }
        return true;
    }

}
