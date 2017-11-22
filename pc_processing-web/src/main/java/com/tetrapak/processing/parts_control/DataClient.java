/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control;

import com.tetrapak.processing.parts_control.pc_data_ejb.Data;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Named;

/**
 * Returns data from the ejb 'dataBean' doing the data processing
 *
 * @author SEPALMM
 */
@Named(value = "dataClient")
@Stateless
public class DataClient {

    @EJB
    private Data dataBean;

    // Properties to be returned to the jsf page
    private String myData;

    // Constructor
    public DataClient() {
    }

    // Initialize properties and methods (sequencial order is important)
    @PostConstruct
    public void init() {
        System.out.println("I'm in the DataClient init()...");
        myData = "";
        callData();
    }

    public void callData() {
        System.out.println("I'm in callData() method ...");
        myData = dataBean.sayHello();
    }

    public String getMyData() {
        return myData;
    }

    public void setMyData(String myData) {
        this.myData = myData;
    }

}
