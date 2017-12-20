/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control.pc_data_ejb;

import com.tetrapak.processing.parts_control.pc_models.Material;
import com.tetrapak.processing.parts_control.pc_neo4j_service_ejb.Neo4jService;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs data calculations
 *
 * @author Magnus Palm
 */
@Stateless
public class DataBean implements Data, Serializable {

    @EJB
    private Neo4jService neo4jServiceBean;

    private static final long serialVersionUID = 1L;

    private String message;
    private static final Logger LOGGER = LoggerFactory.getLogger(DataBean.class);
    private static Session session;
    private Map<String, Material> taskListGapMaterialMap;
    private Map<String, Material> customerGapMaterialMap;

    @PostConstruct
    public void init() {
        // INITIATE CLASS SPECIFIC MAPS AND FIELDS HERE - THE ORDER IS IMPORTANT

        // Initialize driver
        session = neo4jServiceBean.getDRIVER().session();
        // Initialize material maps
        taskListGapMaterialMap = new HashMap<>();
        customerGapMaterialMap = new HashMap<>();
    }

    @Override
    public String sayHello() {
        return message = "Hello from DataBean";
    }

    /**
     * Find materials in customer's Stock or Cart purchase history that do not
     * exist in the TaskList.
     *
     * @param customerNumbers
     */
    @Override
    public void findTaskListGap(String[] customerNumbers) {
        try {
//            Initiate gap counter
            int gapCounter = 0;
            taskListGapMaterialMap.clear();
            String tx = "MATCH (m:PcMaterial)-[:STOCKED_IN|:PURCHASED_IN]->(n) "
                    + "WHERE n.id IN $custNumbers "
                    + "OPTIONAL MATCH (m)-[r:LISTED_IN]->(t:TaskList) "
                    + "WHERE t.id IN $custNumbers "
                    + "WITH m.materialNumber AS mtrlNo, m, r "
                    + "WHERE r IS NULL "
                    + "WITH DISTINCT mtrlNo, m "
                    + "RETURN m.assortmentGroup AS aGrp, m.mpg AS mpg, mtrlNo, m.description AS desc, m.pg AS pg";

            StatementResult result = session.run(tx, Values.parameters(
                    "custNumbers", customerNumbers
            ));

            while (result.hasNext()) {
                gapCounter++;
                Record next = result.next();

                String assortmentGroup = next.get("aGrp").asString();
                String mpg = next.get("mpg").asString();
                String materialNumber = next.get("mtrlNo").asString();
                String description = next.get("desc").asString();
                Double pg = Double.valueOf(next.get("pg").asString());

//                System.out.printf("Assortment Group: %s, MPG: %s, MaterialNo: %s, Description: %s, PG %f\n",assortmentGroup, mpg, materialNumber, description, pg);
                taskListGapMaterialMap.put(materialNumber, new Material(assortmentGroup, description, materialNumber, mpg, pg));
            }

            LOGGER.info("Queried and found {} Task list gap(s).", gapCounter);

        } catch (Exception e) {
            LOGGER.error("Could not query the Task List gap. Error message: {}", e.getMessage());
        }
    }

    /**
     * "Find materials in the TaskList that do not exist in the customer's Stock
     * or Cart purchase history"
     *
     * @param customerNumbers
     */
    @Override
    public void findCustomerGap(String[] customerNumbers) {
        try {
//            Initiate gap counter
            int gapCounter = 0;
            customerGapMaterialMap.clear();
            String tx = "MATCH (m:PcMaterial)-[:LISTED_IN]->(t:TaskList) "
                    + "WHERE t.id IN $custNumbers "
                    + "OPTIONAL MATCH (m)-[r:STOCKED_IN|:PURCHASED_IN]->(n) "
                    + "WHERE n.id IN $custNumbers "
                    + "WITH m.materialNumber AS mtrlNo, m, r "
                    + "WHERE r IS NULL "
                    + "WITH DISTINCT mtrlNo, m "
                    + "RETURN m.assortmentGroup AS aGrp, m.mpg AS mpg, mtrlNo, m.description AS desc, m.pg AS pg";

            StatementResult result = session.run(tx, Values.parameters(
                    "custNumbers", customerNumbers
            ));

            while (result.hasNext()) {
                gapCounter++;
                Record next = result.next();

                String assortmentGroup = next.get("aGrp").asString();
                String mpg = next.get("mpg").asString();
                String materialNumber = next.get("mtrlNo").asString();
                String description = next.get("desc").asString();
                Double pg = Double.valueOf(next.get("pg").asString());

//                System.out.printf("Assortment Group: %s, MPG: %s, MaterialNo: %s, Description: %s, PG %f\n",assortmentGroup, mpg, materialNumber, description, pg);
                customerGapMaterialMap.put(materialNumber, new Material(assortmentGroup, description, materialNumber, mpg, pg));
            }

            LOGGER.info("Queried and found {} Customer material gap(s).", gapCounter);

        } catch (Exception e) {
            LOGGER.error("Could not query the Customer material gap. Error message: {}", e.getMessage());
        }
    }

    @Override
    public Map<String, Material> getTaskListGapMaterialMap() {
        return taskListGapMaterialMap;
    }

    @Override
    public void setTaskListGapMaterialMap(Map<String, Material> taskListGapMaterialMap) {
        this.taskListGapMaterialMap = taskListGapMaterialMap;
    }

    @Override
    public Map<String, Material> getCustomerGapMaterialMap() {
        return customerGapMaterialMap;
    }

    @Override
    public void setCustomerGapMaterialMap(Map<String, Material> materialMap) {
        this.customerGapMaterialMap = customerGapMaterialMap;
    }

}
