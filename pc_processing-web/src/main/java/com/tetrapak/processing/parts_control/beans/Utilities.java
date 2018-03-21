/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control.beans;

/**
 * Utility class
 *
 * @author SEPALMM
 */
public class Utilities {

    /**
     * Removes all non-digits from part numbers
     *
     * @param sparePartNo to be cleaned
     * @return cleaned spare part number
     */
    public static String removeNonDigits(String sparePartNo) {
        return sparePartNo.replaceAll("[\\D.]", "");
    }
}
