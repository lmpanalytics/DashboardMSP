/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utility;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.Month;
import static java.time.temporal.ChronoUnit.MONTHS;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedList;
import java.util.List;

/**
 * This class contains utility methods
 *
 * @author SEPALMM
 */
public class Utility {

    /**
     * Calculate the start of the Rolling 12 period, which is the end of January
     * in the year two years from the current year. Example: Current date is
     * 2017-01-29, which gives a starting date of 2015-01-31
     *
     * @return the start date
     */
    public static LocalDate calcStartDate() {
        int startYear = LocalDate.now().getYear() - 2;
        int startMonth = 1;
        int startDay = 31;
        return LocalDate.of(startYear, startMonth, startDay);
    }

    /**
     * Calculate the number of months from the start of the Rolling 12 period
     * until the current month minus 1, which is the end of the R12 plot range.
     *
     * @return the number of months between Start Date (Inclusive) and Today
     * (Exclusive)
     */
    public static long calcMonthsFromStart() {
        return MONTHS.between(calcStartDate(), LocalDate.now());
    }

    /**
     * Makes a date using the final day of the Month based on the supplied Year
     * and Month values.
     *
     * @param year the year of the date to be made
     * @param month the month of the date to be made
     * @return the date
     */
    public static LocalDate makeDate(int year, int month) {
        LocalDate d = LocalDate.of(1900, 1, 31);
        if ((year > 1900 && year < 2050) && (month >= 1 && month <= 12)) {
            d = LocalDate.of(year, month, 1);
        }
        return d.with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * Test if the test date is within a 12 month range from a start date
     *
     * @param startDate the start date
     * @param testDate the date to be tested
     * @return true if within rage, else false
     */
    public static boolean isWithinRange(LocalDate startDate, LocalDate testDate) {
        LocalDate endDate = startDate.plusMonths(12).with(TemporalAdjusters.
                lastDayOfMonth());

        boolean inRange = false;
        if (testDate.isBefore(endDate) && testDate.isAfter(startDate) || testDate.
                equals(startDate)) {
            inRange = true;
        } else {
            inRange = false;
        }
//        System.out.printf("Is %s within the range of %s and %s? Results %s\n",testDate, startDate, endDate, inRange);
        return inRange;
    }

    /**
     * Calculate the percentage margin
     *
     * @param sales the sales amount (positive)
     * @param cost the cost amount (negative)
     * @return the margin in percent
     */
    public static double calcMargin(double sales, double cost) {
        double margin = 0d;
//        Handle divison by zero exception
        if (sales != 0d) {
            margin = ((sales + cost) / sales) * 100.0;
        }
        return margin;
    }

    /**
     * Calculate the growth rate in percent
     *
     * @param currentValue
     * @param pastValue
     * @return growth rate
     */
    public static double calcGrowthRate(double currentValue, double pastValue) {
        //            Calculate the growth
        double rate = 0d;
        if (pastValue != 0d) {
            rate = ((currentValue / pastValue) - 1) * 100d;
        }
        return rate;
    }

    /**
     * Round input value to significant figures
     *
     * @param input the value to be rounded
     * @param significanFigures enter 3 for 3 significant figures and so forth
     * @return rounded value
     */
    public static double roundDouble(double input, int significanFigures) {
        BigDecimal bd = new BigDecimal(input);
        bd = bd.round(new MathContext(significanFigures));
        return bd.doubleValue();
    }

    /**
     * Calculate and return the starting date for the last 12 months of sales,
     * from today.
     *
     * @return start date
     */
    public static String makeStartDateLast12MonthSales() {
        return LocalDate.now().minusMonths(12).
                with(TemporalAdjusters.lastDayOfMonth()).toString().
                replaceAll("-", "");
    }
}
