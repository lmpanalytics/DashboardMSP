/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utility;

import java.time.LocalDate;
import java.time.Month;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author SEPALMM
 */
public class UtilityTest {

    public UtilityTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of calcStartDate method, of class Utility.
     */
    @Test
    public void testCalcStartDate() {
        System.out.println("calcStartDate");
        int currentYear = LocalDate.now().getYear();
        if (currentYear == 2017) {
            LocalDate expResult = LocalDate.of(2015, 1, 31);
            LocalDate result = Utility.calcStartDate();
            System.out.println("calcStartDate is: " + result);
            assertEquals(expResult, result);
        } else if (currentYear == 2018) {
            LocalDate expResult = LocalDate.of(2016, 1, 31);
            LocalDate result = Utility.calcStartDate();
            System.out.println("calcStartDate is: " + result);
            assertEquals(expResult, result);
        } else if (currentYear == 2019) {
            LocalDate expResult = LocalDate.of(2017, 1, 31);
            LocalDate result = Utility.calcStartDate();
            System.out.println("calcStartDate is: " + result);
            assertEquals(expResult, result);
        } else if (currentYear == 2020) {
            LocalDate expResult = LocalDate.of(2018, 1, 31);
            LocalDate result = Utility.calcStartDate();
            System.out.println("calcStartDate is: " + result);
            assertEquals(expResult, result);
        } else if (currentYear == 2021) {
            LocalDate expResult = LocalDate.of(2019, 1, 31);
            LocalDate result = Utility.calcStartDate();
            System.out.println("calcStartDate is: " + result);
            assertEquals(expResult, result);
        }

    }

    /**
     * Test of makeDate method, of class Utility.
     */
    @Test
    public void testMakeDate() {
        System.out.println("makeDate");
        assertEquals(LocalDate.of(2017, 1, 31), Utility.makeDate(2017, 1));
        assertEquals(LocalDate.of(1900, 1, 31), Utility.makeDate(1800, 3));
        assertEquals(LocalDate.of(1900, 1, 31), Utility.makeDate(2055, 1));
        assertEquals(LocalDate.of(2016, 2, 29), Utility.makeDate(2016, 2));
    }

}
