/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control.pc_logic_ejb;

import com.tetrapak.processing.parts_control.pc_models.Inventory;
import com.tetrapak.processing.parts_control.pc_models.LogicParameters;
import com.tetrapak.processing.parts_control.pc_models.TaskListEvent;
import com.tetrapak.processing.parts_control.pc_models.TaskListMetaData;
import com.tetrapak.processing.parts_control.pc_neo4j_service_ejb.Neo4jService;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
    private boolean is_SKU;

    private enum Action {
        A_CHECK, B_TURN, C_CHANGE
    }

    @PostConstruct
    public void init() {
        // INITIATE CLASS SPECIFIC MAPS AND FIELDS HERE - THE ORDER IS IMPORTANT

        // Initialize material maps
        recommendedMaterialMap = new HashMap<>();

        // Initialize message
        message = "";
        is_SKU = false;
    }

    @Override
    public String getMessage() {
        return message;
    }

    /**
     * WIP: NOT FULLY IMPLEMENTED YET
     *
     * Calculate recommended critical part to stock based on the range of
     * service interval hours. Material numbers are given in BW format (if
     * originating) from the GPL.
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
                    return queryDB(tx, taskListMetaData, logicParameters);
                }
            });
        }
    }

    private List<Inventory> queryDB(Transaction tx, TaskListMetaData taskListMetaData, LogicParameters logicParameters) {

        List<TaskListEvent> events = new ArrayList<>();
        List<Inventory> processedMaterials = new ArrayList<>();
        boolean exceptionFlag = true;

        try {
            // Get Task list Meta data
            String id = taskListMetaData.getId();

            // Get Logic parameters
            int intervalLL = logicParameters.getactionIntervalLL();
            int intervalUL = logicParameters.getactionIntervalUL();
//            Change id matching to be a collection of IDs
//            Possibly group materials by machine model from task list?
            StatementResult result = tx.run(
                    "MATCH (m:PcMaterial)-[r:LISTED_IN ]->(t:TaskList {id:$id}) "
                    + "WHERE r.actionInterval >= $intervalLL AND r.actionInterval <= $intervalUL "
                    + "RETURN t.equipment AS equipment, t.action AS action, t.description AS description, m.materialNumber AS materialNumber, m.denomination AS denomination, r.quantity AS quantity, t.functionalArea AS functionalArea;",
                    Values.parameters(
                            "id", id,
                            "intervalLL", intervalLL,
                            "intervalUL", intervalUL
                    ));
            while (result.hasNext()) {
                Record next = result.next();

                String equipment = next.get("equipment").asString();
                String tAction = next.get("action").asString();
                String tDescription = next.get("description").asString();
                String mMaterialNumber = next.get("materialNumber").asString();
                String mDenomination = next.get("denomination").asString();
                int rQuantity = next.get("quantity").asInt();
                String tFunctionalArea = next.get("functionalArea").asString();

                events.add(new TaskListEvent(equipment, tAction, tDescription, mMaterialNumber, mDenomination, rQuantity, tFunctionalArea));
            }
            exceptionFlag = false;

        } catch (Exception e) {
            exceptionFlag = true;
            LOGGER.error("Error when calculating recommended parts to stock: {}", e.getMessage());
        }
        if (!exceptionFlag) {
            // Processing of materials according to logic rules
            processedMaterials = processEvents(events);
            LOGGER.info("Recommended {} material(s) to stock.", processedMaterials.size());
            LocalTime time = LocalDateTime.now().toLocalTime();
            message = message + time + ": Recommended " + processedMaterials.size() + " material(s) to stock.\n";
        }
        return processedMaterials;
    }

    /**
     * Process Task list events and Materials according to logic rules.
     *
     * @param events
     * @return inventory list of materials to stock
     */
    @Override
    public List<Inventory> processEvents(List<TaskListEvent> events) {
        Map<Integer, Inventory> skuMap = new HashMap<>();

// **************************** PRE-PROCESSING ********************************
//       Filter action types and fix natural language ordering of actions
        events.stream().filter(p -> p.getAction().equalsIgnoreCase("check")
                || p.getAction().equalsIgnoreCase("change")
                || p.getAction().equalsIgnoreCase("turn")).forEach((e) -> {
            if (e.getAction().equalsIgnoreCase("check")) {
                e.setAction(Action.A_CHECK.toString());
            } else if (e.getAction().equalsIgnoreCase("turn")) {
                e.setAction(Action.B_TURN.toString());
            } else if (e.getAction().equalsIgnoreCase("change")) {
                e.setAction(Action.C_CHANGE.toString());
            }
        });

        Comparator<TaskListEvent> taskListEventComparator
                = Comparator.comparing(TaskListEvent::getEquipment)
                        .thenComparing(TaskListEvent::getSparePartNo)
                        .thenComparing(TaskListEvent::getAction);

        events.sort(taskListEventComparator);

        Map<String, Map<String, List<TaskListEvent>>> groupedMap = events.stream().
                collect(Collectors.groupingBy(TaskListEvent::getEquipment,
                        Collectors.groupingBy(TaskListEvent::getSparePartNo)));

// ****************************** APPLY LOGIC **********************************
        groupedMap.values().stream().forEachOrdered(eq -> {
            eq.values().stream().forEachOrdered(sp -> {
                is_SKU = false;

                sp.stream().forEachOrdered(tle -> {

// Singleton CHECK action, or followed by TURN and/or CHANGE is SKU
                    if (is_SKU || tle.getAction().equals(Action.A_CHECK.toString())) {
                        is_SKU = true;

//                        Calculate quantity to stock
                        int q = (int) Math.ceil((double) tle.getQty() / 20);

                        int key = tle.getSparePartNo().hashCode();
                        skuMap.put(key, new Inventory(
                                tle.getSparePartNo(),
                                tle.getSpDenomination(),
                                q)
                        );
//                        System.out.printf("Stock: %s, %s, %s, %s%n", tle.getEquipment(), tle.getSparePartNo(), tle.getQty(), tle.getAction());

                    } else {
//                        System.out.printf("No stock: %s, %s, %s, %s%n", tle.getEquipment(), tle.getSparePartNo(), tle.getQty(), tle.getAction());
                    }

                });
            });
        });

        List<Inventory> inventory = skuMap.values().stream().collect(Collectors.toList());
//        System.out.println("RECOMMENDED INVENTORY LIST:");
//        inventory.forEach(c -> System.out.printf("%s, %s, %s%n", c.getMaterial(), c.getDescription(), c.getQuantity()));
        return inventory;
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
