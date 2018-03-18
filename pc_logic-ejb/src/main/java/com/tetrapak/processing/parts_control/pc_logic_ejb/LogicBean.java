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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;
import org.neo4j.driver.v1.Values;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(LogicBean.class);
    private Map<String, Inventory> recommendedMaterialMap;
    private String message;

    @PostConstruct
    public void init() {
        // INITIATE CLASS SPECIFIC MAPS AND FIELDS HERE - THE ORDER IS IMPORTANT

        // Initialize material maps
        recommendedMaterialMap = new HashMap<>();

        // Initialize message
        message = "";
    }

    @Override
    public String getMessage() {
        return message;
    }

    /**
     * WIP: NOT FULLY IMPLEMENTED YET
     *
     * Calculate recommended critical part to stock based on the average annual
     * running hours.
     *
     * @param taskListMetaData meta data from task lists e.g, Description,
     * UserID.
     * @param logicParameters logic parameters from jsf user inputs
     */
    @Override
    public void calculateInventory(TaskListMetaData taskListMetaData, LogicParameters logicParameters) {
        recommendedMaterialMap.clear();
        List<Inventory> list = getMaterials(taskListMetaData, logicParameters);
        list.forEach((inv) -> {
            recommendedMaterialMap.put(inv.getMaterial(), new Inventory(inv.getMaterial(), inv.getDescription(), inv.getQuantity()));
        });
        try {
            neo4jServiceBean.close();
        } catch (Exception e) {
            LOGGER.error("Error when closing Neo4j Driver: {}", e.getMessage());
        }
    }

    private List<Inventory> getMaterials(TaskListMetaData taskListMetaData, LogicParameters logicParameters) {
        try (Session session = neo4jServiceBean.getDRIVER().session();) {
            return session.readTransaction(new TransactionWork<List<Inventory>>() {
                @Override
                public List<Inventory> execute(Transaction tx) {
                    return matchMaterialNodes(tx, taskListMetaData, logicParameters);
                }
            });
        }
    }

    private List<Inventory> matchMaterialNodes(Transaction tx, TaskListMetaData taskListMetaData, LogicParameters logicParameters) {

        List<Inventory> materials = new ArrayList<>();
        boolean exceptionFlag = true;
        int rowCounter = 0;

        try {
            // Get Task list Meta data
            String id = taskListMetaData.getId();

            // Get Logic parameters
            int intervalLL = logicParameters.getactionIntervalLL();
            int intervalUL = logicParameters.getactionIntervalUL();
            StatementResult result = tx.run(
                    "MATCH (m:PcMaterial)-[r:LISTED_IN ]->(t:TaskList {id:$id}) "
                    + "WHERE r.actionInterval >= $intervalLL AND r.actionInterval <= $intervalUL "
                    + "RETURN m.materialNumber AS materialNumber, m.description AS description, r.quantity AS quantity;",
                    Values.parameters(
                            "id", id,
                            "intervalLL", intervalLL,
                            "intervalUL", intervalUL
                    ));
            while (result.hasNext()) {
                Record next = result.next();

                String materialNumber = next.get("materialNumber").asString();
                String description = next.get("description").asString();
                double t = next.get("quantity").asDouble();
                int quantity = (int) (Math.ceil(t / 20));
                materials.add(new Inventory(materialNumber, description, quantity));
                rowCounter++;
            }
            exceptionFlag = false;

        } catch (Exception e) {
            exceptionFlag = true;
            LOGGER.error("Error when calculating recommended parts to stock: {}", e.getMessage());
        }
        if (!exceptionFlag) {
            LOGGER.info("Recommended {} material(s) to stock.", rowCounter);
            LocalTime time = LocalDateTime.now().toLocalTime();
            message = message + time + ": Recommended " + rowCounter + " material(s) to stock.\n";
        }
        return materials;
    }

    @PreDestroy
    public void destroyMe() {
        try {
            neo4jServiceBean.close();
        } catch (Exception e) {
            LOGGER.error("Error when closing Neo4j Driver: {}", e.getMessage());
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
