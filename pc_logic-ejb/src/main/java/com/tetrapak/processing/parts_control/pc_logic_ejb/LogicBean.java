/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control.pc_logic_ejb;

import com.tetrapak.processing.parts_control.pc_models.Inventory;
import com.tetrapak.processing.parts_control.pc_models.LogicParameters;
import com.tetrapak.processing.parts_control.pc_models.TaskListMetaData;
import com.tetrapak.processing.parts_control.pc_neo4j_service_ejb.Neo4jService;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.neo4j.driver.v1.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs business logic
 *
 * @author Magnus Palm
 */
@Stateless
public class LogicBean implements Logic, Serializable {

    @EJB
    private Neo4jService neo4jServiceBean;

    private static final long serialVersionUID = 1L;

    private String message;
    private static final Logger LOGGER = LoggerFactory.getLogger(LogicBean.class);
    private static Session session;
    private Map<String, Inventory> recommendedMaterialMap;

    @PostConstruct
    public void init() {
        // INITIATE CLASS SPECIFIC MAPS AND FIELDS HERE - THE ORDER IS IMPORTANT

        // Initialize driver
        session = neo4jServiceBean.getDRIVER().session();
        // Initialize material maps
        recommendedMaterialMap = new HashMap<>();
    }

    @Override
    public String sayHello() {
        return message = "Hello from LogicBean";
    }

    @Override
    public void calculateInventory(TaskListMetaData taskListMetaData, LogicParameters logicParameters) {

        try {
            // Implement row counter.
            int rowCounter = 0;

            // Add bean's logic calculation methods to execute
            recommendedMaterialMap.put("my_key", new Inventory("my_mtrlNumber", "my_description", 1));

            LOGGER.info("Calculated {} row(s) of recommended materials to stock.", rowCounter);

        } catch (Exception e) {
            LOGGER.error("Could not Calculate Inventory. Error message: {}", e.getMessage());
        }
    }

    @Override
    public Map<String, Inventory> getRecommendedMaterialMap() {
        return recommendedMaterialMap;
    }

    @Override
    public void setRecommendedMaterialMap(Map<String, Inventory> materialMap) {
        this.recommendedMaterialMap = recommendedMaterialMap;
    }

}
