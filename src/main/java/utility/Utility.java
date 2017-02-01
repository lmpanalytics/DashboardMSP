/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utility;

import java.time.LocalDate;
import static java.time.temporal.ChronoUnit.MONTHS;
import java.time.temporal.TemporalAdjusters;

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
     * @return number of months
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
}
