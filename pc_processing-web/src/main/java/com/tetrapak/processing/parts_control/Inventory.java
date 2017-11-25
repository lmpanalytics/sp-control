/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control;

/**
 * Models inventory data
 *
 * @author SEPALMM
 */
public class Inventory {

    private String material;
    private String description;
    private int quantity;

    public Inventory(String material, String description, int quantity) {
        this.material = material;
        this.description = description;
        this.quantity = quantity;
    }

    public String getMaterial() {
        return material;
    }

    public String getDescription() {
        return description;
    }

    public int getQuantity() {
        return quantity;
    }

}
