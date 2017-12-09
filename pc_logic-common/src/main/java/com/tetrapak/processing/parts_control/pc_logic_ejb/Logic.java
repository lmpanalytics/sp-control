/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control.pc_logic_ejb;

import javax.ejb.Remote;

/**
 * Remote business interface for the Logic Bean.
 *
 * @author SEPALMM
 */
@Remote
public interface Logic {

    public String sayHello();

}
