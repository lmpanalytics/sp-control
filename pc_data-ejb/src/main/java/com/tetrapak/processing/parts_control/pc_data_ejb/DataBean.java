/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control.pc_data_ejb;

import java.io.Serializable;
import javax.ejb.Stateless;

/**
 * Performs data calculations
 *
 * @author Magnus Palm
 */
@Stateless
public class DataBean implements Data, Serializable {

    private static final long serialVersionUID = 1L;

    private String message;

    @Override
    public String sayHello() {
        return message = "Hello from DataBean";
    }
}
