/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control.pc_data_ejb;

import javax.ejb.Stateless;

/**
 *
 * @author Magnus Palm
 */
@Stateless
public class DataBean implements Data {

    @Override
    public String sayHello() {
        return ("Hello from DataBean");
    }
}
