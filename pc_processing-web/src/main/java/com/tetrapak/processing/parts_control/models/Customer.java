/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control.models;

/**
 * Models Customers
 *
 * @author SEPALMM
 */
public class Customer {

    private String id;
    private String name;
    private String deliveryZone;

    // Constructor 1
    public Customer(String id, String name, String deliveryZone) {
        this.id = id;
        this.name = name;
        this.deliveryZone = deliveryZone;
    }

    // Constructor 2
    public Customer(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeliveryZone() {
        return deliveryZone;
    }

    public void setDeliveryZone(String deliveryZone) {
        this.deliveryZone = deliveryZone;
    }

}
