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
import org.junit.Ignore;

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

    /**
     * Test of calcMargin method, of class Utility.
     */
    @Test
    public void testCalcMargin() {
        System.out.println("calcMargin");
//        double result = Utility.calcMargin(sales, cost);
        assertEquals(20d, Utility.calcMargin(100d, -80d), 0.0);
        assertEquals(23.954372623574148, Utility.calcMargin(105.2, -80), 0.0);
        assertEquals(0, Utility.calcMargin(0d, -80d), 0.0);
        assertEquals(100, Utility.calcMargin(200d, 0d), 0.0);
        assertEquals(180, Utility.calcMargin(100d, 80d), 0.0);
        assertEquals(20, Utility.calcMargin(-100d, 80d), 0.0);
        assertEquals(1000100, Utility.calcMargin(0.01, 100d), 0.0);
        assertEquals(99.999, Utility.calcMargin(100d, -0.001), 0.0);

    }

}
