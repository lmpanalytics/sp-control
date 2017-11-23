/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control;

import com.tetrapak.processing.parts_control.pc_logic_ejb.Logic;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Named;

/**
 * Returns logic from the ejb 'LogicBean' doing the business logic processing
 *
 * @author SEPALMM
 */
@Named(value = "logicClient")
@Stateless
public class LogicClient {

    @EJB
    private Logic logicBean;

    // Properties to be returned to the jsf page
    private String myLogic;

    // Constructor
    public LogicClient() {
    }

    // Initialize properties and methods (sequencial order is important)
    @PostConstruct
    public void init() {
        System.out.println("I'm in the LogicClient init()...");
        myLogic = "";
        callLogic();
    }

    public void callLogic() {
        System.out.println("I'm in callLogic() method ...");
        myLogic = logicBean.sayHello();
    }

    public String getMyLogic() {
        return myLogic;
    }

    public void setMyLogic(String myLogic) {
        this.myLogic = myLogic;
    }

}
