/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control.beans;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author SEPALMM
 */
public class UtilitiesTest {

    public UtilitiesTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of removeNonDigits method, of class Utilities.
     */
    @org.junit.Test
    public void testRemoveNonDigits() {
        System.out.println("removeNonDigits");
        assertEquals("904580072", Utilities.removeNonDigits("90458-0072"));
        assertEquals("904580072", Utilities.removeNonDigits("  90458-0072"));
        assertEquals("69611922190", Utilities.removeNonDigits("6-9611-92-2190"));
        assertEquals("69611922190", Utilities.removeNonDigits("6-9611 92-2190"));
        assertEquals("69611922190", Utilities.removeNonDigits("6-9611 92 2190"));
        assertEquals("69611922190", Utilities.removeNonDigits("6-9611 92 2190 "));
    }

}
