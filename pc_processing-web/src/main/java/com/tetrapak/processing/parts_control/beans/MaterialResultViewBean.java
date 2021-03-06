/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control.beans;

import com.tetrapak.processing.parts_control.pc_logic_ejb.Logic;
import com.tetrapak.processing.parts_control.pc_models.Inventory;
import com.tetrapak.processing.parts_control.pc_models.LogicParameters;
import com.tetrapak.processing.parts_control.pc_models.TaskListMetaData;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

/**
 * Manages jsf view of recommended parts calculation results. The bean is
 * session scoped to keep calculation results in session.
 *
 * @author SEPALMM
 */
@Named(value = "materialResultViewBean")
@SessionScoped
public class MaterialResultViewBean implements Serializable {

    @EJB
    Logic logicBean;

    @Inject
    TaskListViewBean taskListViewBean;

    @Inject
    LogicParametersViewBean logicParametersViewBean;

    private static final long serialVersionUID = 1L;
    private List<Inventory> recommendedPartsList;
    private List<Inventory> excludedPartsList;

    /**
     * Creates a new instance of MaterialResultViewBean
     */
    public MaterialResultViewBean() {
    }

    @PostConstruct
    public void init() {
        // INITIATE CLASS SPECIFIC MAPS AND FIELDS HERE - THE ORDER IS IMPORTANT

        // Initiate Recommended parts list
        recommendedPartsList = new ArrayList<>();
        excludedPartsList = new ArrayList<>();
    }

    /**
     * Make lists of Recommended Materials as SKUs, and Excluded Materials, i.e.
     * materials neither in GPL nor sold through PSC in last 36 months.
     */
    public void processRecommendedParts() {
        recommendedPartsList.clear();
        excludedPartsList.clear();

        List<TaskListMetaData> metaData = taskListViewBean.getSelectedTaskLists();
        LogicParameters parameters = logicParametersViewBean.getLogicParameters();

        logicBean.calculateInventory(metaData, parameters);

        recommendedPartsList = new ArrayList<>(logicBean.getRecommendedMaterialMap().values());
        excludedPartsList = new ArrayList<>(logicBean.getNonSKUmap().values());
    }

    public List<Inventory> getRecommendedPartsList() {
        return recommendedPartsList;
    }

    public void setRecommendedPartsList(List<Inventory> recommendedPartsList) {
        this.recommendedPartsList = recommendedPartsList;
    }

    public List<Inventory> getExcludedPartsList() {
        return excludedPartsList;
    }

    public void setExcludedPartsList(List<Inventory> excludedPartsList) {
        this.excludedPartsList = excludedPartsList;
    }

}
