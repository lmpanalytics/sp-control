/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control.beans;

import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

/**
 * Manages jsf view of recommended parts calculation results. The bean is
 * request scoped as calculation results are not kept is session.
 *
 * @author SEPALMM
 */
@Named(value = "materialResultViewBean")
@RequestScoped
public class MaterialResultViewBean {

    @Inject
    TaskListViewBean taskListViewBean;

    /**
     * Creates a new instance of MaterialResultViewBean
     */
    public MaterialResultViewBean() {
    }

    public void showSelectedTaskList() {
        System.out.println("In selected TaskList(), TaskList: " + taskListViewBean.getSelectedTaskList().toString());
    }

}
