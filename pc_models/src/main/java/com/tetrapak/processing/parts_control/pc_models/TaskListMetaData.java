/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control.pc_models;

import java.util.Objects;

/**
 * Models the meta data of a task list
 *
 * @author SEPALMM
 */
public class TaskListMetaData {

    private String id;
    private String description;
    private String version;
    private String dateOfAnalysis;
    private String userID;

    // Constructor
    public TaskListMetaData(String id, String description, String version, String dateOfAnalysis, String userID) {
        this.id = id;
        this.description = description;
        this.version = version;
        this.dateOfAnalysis = dateOfAnalysis;
        this.userID = userID;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.id);
        hash = 71 * hash + Objects.hashCode(this.description);
        hash = 71 * hash + Objects.hashCode(this.version);
        hash = 71 * hash + Objects.hashCode(this.dateOfAnalysis);
        hash = 71 * hash + Objects.hashCode(this.userID);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TaskListMetaData other = (TaskListMetaData) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!Objects.equals(this.version, other.version)) {
            return false;
        }
        if (!Objects.equals(this.dateOfAnalysis, other.dateOfAnalysis)) {
            return false;
        }
        if (!Objects.equals(this.userID, other.userID)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TaskListMetaData{" + "id=" + id + ", description=" + description + ", version=" + version + ", dateOfAnalysis=" + dateOfAnalysis + ", userID=" + userID + '}';
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDateOfAnalysis() {
        return dateOfAnalysis;
    }

    public void setDateOfAnalysis(String dateOfAnalysis) {
        this.dateOfAnalysis = dateOfAnalysis;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

}
