/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control;

/**
 * Models purchase data
 *
 * @author SEPALMM
 */
public class Purchases {

    private String material;
    private String description;
    private int quantity;

    public Purchases(String material, String description, int quantity) {
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
