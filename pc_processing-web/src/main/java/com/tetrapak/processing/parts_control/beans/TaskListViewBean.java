/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control.beans;

import com.tetrapak.processing.parts_control.pc_models.TaskListMetaData;
import com.tetrapak.processing.parts_control.pc_neo4j_service_ejb.Neo4jService;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages Task list meta data selections by user from the jsf page. The bean is
 * request scoped to enable new Task list meta data from data base at browser
 * refresh.
 *
 * @author SEPALMM
 */
@Named(value = "taskListViewBean")
@RequestScoped
public class TaskListViewBean implements Serializable {

    @EJB
    Neo4jService neo4jServiceBean;

    private static final long serialVersionUID = 1L;
    private List<TaskListMetaData> taskLists;
    private TaskListMetaData selectedTaskList;

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskListViewBean.class);
    private static Session session;

    /**
     * Creates a new instance of TaskListViewBean
     */
    public TaskListViewBean() {
    }

    @PostConstruct
    public void init() {
        // INITIATE CLASS SPECIFIC MAPS AND FIELDS HERE - THE ORDER IS IMPORTANT

        // Initialize driver
        session = neo4jServiceBean.getDRIVER().session();
        // Initialize taskList list
        taskLists = new ArrayList<>();

        queryGlobalTaskLists();

    }

    private void queryGlobalTaskLists() {

        try {

            String tx = "MATCH (t:TaskList) "
                    + "RETURN t.id AS id, t.description AS description, t.version AS version, t.year AS year, t.month AS month, t.day AS day, t.userId AS userId;";

            StatementResult result = session.run(tx);

            while (result.hasNext()) {
                Record next = result.next();

                StringJoiner sj = new StringJoiner("-");

                String id = next.get("id").asString();
                String description = next.get("description").asString().toUpperCase();
                String version = next.get("version").asString().toUpperCase();
                String year = String.valueOf(next.get("year"));
                String month = String.valueOf(next.get("month"));
                String day = String.valueOf(next.get("day"));
                String dateOfAnalysis = sj.add(year).add(month).add(day).toString();
                String userId = next.get("userId").asString().toUpperCase();

                // Add to TaskList list
                taskLists.add(new TaskListMetaData(id, description, version, dateOfAnalysis, userId));

            }

        } catch (Exception e) {
            LOGGER.error("Could not query the TaskLists. Error message: {}", e.getMessage());
        }
    }

    public List<TaskListMetaData> getTaskLists() {
        return taskLists;
    }

    public void setTaskLists(List<TaskListMetaData> taskLists) {
        this.taskLists = taskLists;
    }

    public TaskListMetaData getSelectedTaskList() {
        return selectedTaskList;
    }

    public void setSelectedTaskList(TaskListMetaData selectedTaskList) {
        this.selectedTaskList = selectedTaskList;
    }

}
