/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control.pc_logic_ejb;

import com.tetrapak.processing.parts_control.pc_models.Inventory;
import com.tetrapak.processing.parts_control.pc_models.LogicParameters;
import com.tetrapak.processing.parts_control.pc_models.TaskListEvent;
import com.tetrapak.processing.parts_control.pc_models.TaskListMetaData;
import java.util.List;
import java.util.Map;
import javax.ejb.Remote;

/**
 * Remote business interface for the Logic Bean.
 *
 * @author SEPALMM
 */
@Remote
public interface Logic {

    public String getMessage();

    public void calculateInventory(TaskListMetaData taskListMetaData, LogicParameters logicParameters);
    
    public List<Inventory> processEvents(List<TaskListEvent> events);

    public Map<String, Inventory> getRecommendedMaterialMap();

    public void setRecommendedMaterialMap(Map<String, Inventory> materialMap);

}
