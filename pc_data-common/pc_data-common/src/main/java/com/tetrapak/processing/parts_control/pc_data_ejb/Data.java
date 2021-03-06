/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control.pc_data_ejb;

import com.tetrapak.processing.parts_control.pc_models.Material;
import java.util.Map;
import javax.ejb.Remote;

/**
 * Remote business interface for the Data Bean.
 *
 * @author SEPALMM
 */
@Remote
public interface Data {

    public String sayHello();

    public void findTaskListGap(String[] customerNumber);

    public void findCustomerGap(String[] customerNumbers);

    public Map<String, Material> getTaskListGapMaterialMap();

    public void setTaskListGapMaterialMap(Map<String, Material> materialMap);

    public Map<String, Material> getCustomerGapMaterialMap();

    public void setCustomerGapMaterialMap(Map<String, Material> materialMap);

}
