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
    private Map<Integer, Inventory> skuMap;
    private String message;
    private boolean is_SKU;
    private int sumMtrlQty;

    private enum Action {
        A_CHECK, B_TURN, C_CHANGE
    }

    @PostConstruct
    public void init() {
        // INITIATE CLASS SPECIFIC MAPS AND FIELDS HERE - THE ORDER IS IMPORTANT

        // Initialize maps
        recommendedMaterialMap = new HashMap<>();
        skuMap = new HashMap<>();

        // Initialize message
        message = "";
        is_SKU = false;
        sumMtrlQty = 0;
    }

    @Override
    public String getMessage() {
        return message;
    }

    /**
     * WIP: NOT FULLY IMPLEMENTED YET
     *
     * Calculate recommended critical part to stock based on: (1) Range of
     * service interval hours, (2) Combinations of service actions, and (3)
     * Spare part families.
     *
     * Material numbers are given in BW format (if originating) from the GPL.
     *
     * @param taskListMetaData meta data from task lists e.g, Description,
     * UserID.
     * @param logicParameters logic parameters from jsf user inputs
     */
    @Override
    public void calculateInventory(List<TaskListMetaData> taskListMetaData, LogicParameters logicParameters) {
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

    private List<Inventory> getMaterials(List<TaskListMetaData> taskListMetaData, LogicParameters logicParameters) {
        try (Session session = neo4jServiceBean.getDRIVER().session();) {
            return session.readTransaction(new TransactionWork<List<Inventory>>() {
                @Override
                public List<Inventory> execute(Transaction tx) {
                    return queryDB(tx, taskListMetaData, logicParameters);
                }
            });
        }
    }

    private List<Inventory> queryDB(Transaction tx, List<TaskListMetaData> taskListMetaData, LogicParameters logicParameters) {

        List<TaskListEvent> events = new ArrayList<>();
        List<Inventory> processedMaterials = new ArrayList<>();
        boolean exceptionFlag = true;

        try {
            // Collect Task list IDs from Task list Meta data
            List<String> listOfTaskListIDs
                    = taskListMetaData.stream().map(t -> t.getId()).
                            collect(Collectors.toList());
//           Convert list of IDs to array
            String[] tasklistIDs = new String[listOfTaskListIDs.size()];
            listOfTaskListIDs.toArray(tasklistIDs);

            // Get Logic parameters
            int intervalLL = logicParameters.getactionIntervalLL();
            int intervalUL = logicParameters.getactionIntervalUL();

            StatementResult result = tx.run(
                    "MATCH (m:PcMaterial)-[r:LISTED_IN ]->(t:TaskList) "
                    + "WHERE (r.actionInterval >= $intervalLL AND r.actionInterval <= $intervalUL) AND t.id IN $ids "
                    + "RETURN m.family AS family, r.action AS action, t.description AS tDescription, m.materialNumber AS materialNumber, m.description AS mDescription, r.quantity AS quantity, t.functionalArea AS functionalArea;",
                    Values.parameters(
                            "ids", tasklistIDs,
                            "intervalLL", intervalLL,
                            "intervalUL", intervalUL
                    ));
            while (result.hasNext()) {
                Record next = result.next();

                String family = next.get("family").asString();
                String rAction = next.get("action").asString();
                String tDescription = next.get("tDescription").asString();
                String mMaterialNumber = next.get("materialNumber").asString();
                String mDenomination = next.get("mDescription").asString();
                int rQuantity = next.get("quantity").asInt();
                String tFunctionalArea = next.get("functionalArea").asString();

                events.add(new TaskListEvent(family, rAction, tDescription, mMaterialNumber, mDenomination, rQuantity, tFunctionalArea));
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
     * Process Task list events and Materials according to logic rules based on:
     * (1) Range of service interval hours, (2) Combinations of service actions,
     * and (3) Spare part families.
     *
     * @param events
     * @return inventory list of materials to stock
     */
    @Override
    public List<Inventory> processEvents(List<TaskListEvent> events) {
        skuMap.clear();

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
                = Comparator.comparing(TaskListEvent::getFamily)
                        .thenComparing(TaskListEvent::getSparePartNo)
                        .thenComparing(TaskListEvent::getAction);

        events.sort(taskListEventComparator);

        Map<String, Map<String, List<TaskListEvent>>> groupedMap = events.stream().
                collect(Collectors.groupingBy(TaskListEvent::getFamily,
                        Collectors.groupingBy(TaskListEvent::getSparePartNo)));

// ****************************** QUALIFICATION **********************************
        groupedMap.values().stream().forEachOrdered(fam -> {
            fam.values().stream().forEachOrdered(sp -> {
                is_SKU = false;

                sp.stream().forEachOrdered(tle -> {

// Singleton CHECK action (or followed by TURN and/or CHANGE) equals an SKU
                    if (is_SKU || tle.getAction().equals(Action.A_CHECK.toString())) {
                        is_SKU = true;

//                        Reset sum of material quantity for each new material
                        int key = tle.getSparePartNo().hashCode();
                        boolean existingSKU = skuMap.containsKey(key);

                        if (!existingSKU) {
                            sumMtrlQty = tle.getQty();

                        } else if (tle.getAction().equals(Action.A_CHECK.toString())) {
//                            Quantity assigned to action 'CHECK' is quantity basis
                            sumMtrlQty = sumMtrlQty + tle.getQty();
                        }

// ************************** APPLY LOGIC BEGINS *******************************
//                        Calculate quantity to stock
                        if (tle.getFamily().equals("Piston")
                                || tle.getFamily().equals("Piston seal")) {
                            if (sumMtrlQty % 3 == 0) {
//                                3-Piston Homogenizer
                                putToSKUmap(key, tle, 3);
                            } else {
//                                5-Piston Homogenizer
                                putToSKUmap(key, tle, 5);
                            }
                        } else {
//                            Calculate stock quantity by dafault formula
                            int q = (int) Math.ceil((double) sumMtrlQty / 20);
                            putToSKUmap(key, tle, q);
                        }

//                        System.out.printf("Stock: %s, %s, %s, %s%n", tle.getFamily(), tle.getSparePartNo(), tle.getQty(), tle.getAction());
                    } else {
//                        System.out.printf("No stock: %s, %s, %s, %s%n", tle.getFamily(), tle.getSparePartNo(), tle.getQty(), tle.getAction());
                    }
// ******************************* LOGIC ENDS ********************************** 
                });
            });
        });

        List<Inventory> inventory = skuMap.values().stream().collect(Collectors.toList());
//        System.out.println("RECOMMENDED INVENTORY LIST:");
//        inventory.forEach(c -> System.out.printf("%s, %s, %s%n", c.getMaterial(), c.getDescription(), c.getQuantity()));
        return inventory;
    }

    private void putToSKUmap(int key, TaskListEvent tle, int q) {
        skuMap.put(key, new Inventory(
                tle.getSparePartNo(),
                tle.getSpDenomination(),
                q)
        );
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
