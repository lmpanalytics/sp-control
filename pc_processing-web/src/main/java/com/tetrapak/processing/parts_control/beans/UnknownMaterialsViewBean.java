/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control.beans;

import com.tetrapak.processing.parts_control.models.ImportMaterial;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Bean manages unknownMaterialsTable in jsf upload.
 *
 * @author SEPALMM
 */
@Named(value = "unknownMaterialsViewBean")
@SessionScoped
public class UnknownMaterialsViewBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private List<ImportMaterial> unknownMtrlsList;

    /**
     * Creates a new instance of UnknownMaterialsViewBean
     */
    public UnknownMaterialsViewBean() {
    }

    @PostConstruct
    public void init() {
        // INITIATE CLASS SPECIFIC MAPS AND FIELDS HERE - THE ORDER IS IMPORTANT

        // Initiate unknownMtrlsList
        unknownMtrlsList = new ArrayList<>();
    }

    @Inject
    private FileLoadBean fileLoadBean;

    public void processUnknownMaterials() {
// Make a list of Materials
        unknownMtrlsList = new ArrayList<ImportMaterial>(fileLoadBean.getUnknownMaterialsList());
    }

    public List<ImportMaterial> getUnknownMtrlsList() {
        return unknownMtrlsList;
    }

    public void setUnknownMtrlsList(List<ImportMaterial> unknownMtrlsList) {
        this.unknownMtrlsList = unknownMtrlsList;
    }

}
