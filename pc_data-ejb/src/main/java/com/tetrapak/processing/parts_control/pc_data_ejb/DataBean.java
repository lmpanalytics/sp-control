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
    private static Map<String, Material> materialMap;

    @PostConstruct
    public void init() {
        // INITIATE CLASS SPECIFIC MAPS AND FIELDS HERE - THE ORDER IS IMPORTANT

        // Initialize driver
        session = neo4jServiceBean.getDRIVER().session();
        materialMap = new HashMap();
    }

    @Override
    public String sayHello() {
        return message = "Hello from DataBean";
    }

    /**
     * Find materials in customer's Stock or Cart purchase history that do not
     * exist in the TaskList.
     *
     * @param customerNumber
     */
    @Override
    public void findTaskListGap(String customerNumber) {

        try {
            String id = customerNumber;

            String tx = "MATCH (m:PcMaterial)-[:STOCKED_IN|:PURCHASED_IN]->(n {id:$custNo}) "
                    + "OPTIONAL MATCH (m)-[r:LISTED_IN]->(t:TaskList {id:$custNo}) "
                    + "WITH m.materialNumber AS mtrlNo, m, r "
                    + "WHERE r IS NULL "
                    + "WITH DISTINCT mtrlNo, m "
                    + "RETURN m.assortmentGroup AS aGrp, m.mpg AS mpg, mtrlNo, m.description AS desc, m.pg AS pg";

            StatementResult result = session.run(tx, Values.parameters(
                    "custNo", id
            ));

            while (result.hasNext()) {
                Record next = result.next();

                String assortmentGroup = next.get("aGrp").asString();
                String mpg = next.get("mpg").asString();
                String materialNumber = next.get("mtrlNo").asString();
                String description = next.get("desc").asString();
                Double pg = Double.valueOf(next.get("pg").asString());

//                System.out.printf("Assortment Group: %s, MPG: %s, MaterialNo: %s, Description: %s, PG %f\n",assortmentGroup, mpg, materialNumber, description, pg);
                materialMap.put(materialNumber, new Material(assortmentGroup, description, materialNumber, mpg, pg));
            }

        } catch (Exception e) {
            LOGGER.error("Could not query the Task List gap. Error message: {}", e.getMessage());
        }
    }

    @Override
    public Map<String, Material> getMaterialMap() {
        return materialMap;
    }

    @Override
    public void setMaterialMap(Map<String, Material> materialMap) {
        DataBean.materialMap = materialMap;
    }

}
