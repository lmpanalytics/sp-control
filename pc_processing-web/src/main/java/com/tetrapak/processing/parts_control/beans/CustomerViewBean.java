/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control.beans;

import com.tetrapak.processing.parts_control.models.Customer;
import com.tetrapak.processing.parts_control.pc_neo4j_service_ejb.Neo4jService;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Inject;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages Customer selection check-box. The bean is session scoped to keep in
 * session the selected Customer.
 *
 * The bean receives a String array of selected composite customer id + name
 * keys from user input on jsf 'taskListGap'.
 *
 * @author SEPALMM
 */
@Named(value = "customerViewBean")
@SessionScoped
public class CustomerViewBean implements Serializable {

    @EJB
    Neo4jService neo4jServiceBean;

    @Inject
    TaskListGapViewBean taskListGapViewBean;

    private static final long serialVersionUID = 1L;
    private List<String> customers;
    private Map<String, Customer> globalCustomersMap;
    private String[] selectedCustomers;
    private String[] selectedIDs;
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerViewBean.class);
    private static Session session;

    /**
     * Creates a new instance of CustomerViewBean.
     */
    public CustomerViewBean() {
    }

    @PostConstruct
    public void init() {
        // INITIATE CLASS SPECIFIC MAPS AND FIELDS HERE - THE ORDER IS IMPORTANT

        // Initialize driver
        session = neo4jServiceBean.getDRIVER().session();
        // Initialize customers list
        customers = new LinkedList<>();
        // Initialize globalCustomersMap
        globalCustomersMap = new HashMap<>();

        queryGlobalCustomers();
    }

    private void queryGlobalCustomers() {

        try {

            String tx = "MATCH (c:PcCustomer) RETURN c.id AS id, c.name AS name ORDER BY id;";

            StatementResult result = session.run(tx);

            while (result.hasNext()) {
                Record next = result.next();

                String id = next.get("id").asString();
                String name = next.get("name").asString().toUpperCase();
                String compositeName = id + " (" + name + ")";

                // Add to customers list
                customers.add(compositeName);

                // Add to globalCustomersMap
                globalCustomersMap.put(compositeName, new Customer(id, name));

            }

        } catch (Exception e) {
            LOGGER.error("Could not query the Customers. Error message: {}", e.getMessage());
        }
    }

    /**
     * Collect IDs of selected customers to string array to be used in a Cypher
     * statement. Called by user from jsf.
     *
     * @param globalCustomersMap
     */
    public void collectSelectedCustomerIds() {
        try {
            int numberOfSelectedCustomers = selectedCustomers.length;
            selectedIDs = new String[numberOfSelectedCustomers];
            if (numberOfSelectedCustomers > 0) {

                for (int i = 0; i < selectedCustomers.length; i++) {
                    String id = globalCustomersMap.get(selectedCustomers[i]).getId();
                    selectedIDs[i] = id;
                }

            }
        } catch (Exception e) {
            LOGGER.error("Could not collect CustomerIDs to text array. Error message: {}", e.getMessage());
        }

        // Call taskListGapViewBean and process the Task list gaps
        taskListGapViewBean.processTaskListGap();
    }

    public List<String> getCustomers() {
        return customers;
    }

    public void setCustomers(List<String> customers) {
        this.customers = customers;
    }

    public String[] getSelectedCustomers() {
        return selectedCustomers;
    }

    public void setSelectedCustomers(String[] selectedCustomers) {
        this.selectedCustomers = selectedCustomers;
    }

    public String[] getSelectedIDs() {
        return selectedIDs;
    }

    public void setSelectedIDs(String[] selectedIDs) {
        this.selectedIDs = selectedIDs;
    }

}
