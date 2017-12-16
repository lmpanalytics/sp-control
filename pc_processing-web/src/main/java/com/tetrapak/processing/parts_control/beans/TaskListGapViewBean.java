/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control.beans;

import com.tetrapak.processing.parts_control.pc_data_ejb.Data;
import com.tetrapak.processing.parts_control.pc_models.Material;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Inject;

/**
 * The bean calls the method 'findTaskListGap' via the remote interface 'Data',
 * passes a collection of customer ids, then calls the method
 * 'getTaskListGapMaterialMap' via the Data interface and receives a MaterialMap
 * of Assortment Groups, Material descriptions, Material numbers, MPGs, and PG
 * prices.
 *
 * @author SEPALMM
 */
@Named(value = "taskListGapViewBean")
@SessionScoped
public class TaskListGapViewBean implements Serializable {

    @EJB
    Data dataBean;

    @Inject
    CustomerViewBean customerViewBean;

    private static final long serialVersionUID = 1L;
    private List<Material> mtrlList;

    /**
     * Creates a new instance of TaskListGapViewBean
     */
    public TaskListGapViewBean() {
    }

    @PostConstruct
    public void init() {
        // INITIATE CLASS SPECIFIC MAPS AND FIELDS HERE - THE ORDER IS IMPORTANT

        // Initiate mtrlList
        mtrlList = new LinkedList<>();
    }

    public void processTaskListGap() {
        dataBean.findTaskListGap(customerViewBean.getSelectedIDs());
// Make a list of Materials
        mtrlList = new ArrayList<Material>(dataBean.getTaskListGapMaterialMap().values());
    }

    public List<Material> getMtrlList() {
        return mtrlList;
    }

    public void setMtrlList(List<Material> mtrlList) {
        this.mtrlList = mtrlList;
    }

}
