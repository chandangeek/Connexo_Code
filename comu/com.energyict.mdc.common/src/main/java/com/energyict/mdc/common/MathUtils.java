package com.energyict.mdc.common;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains a collection of static utility methods.
 */
public class MathUtils {

    /**
     * Creates a new instance of MathUtils
     */
    private MathUtils() {
    }

    // calculates the greatest common divider of n1 and n2
    // recursive calculation

    /**
     * compute the greatest common denominator
     *
     * @param n1 first integer
     * @param n2 second integer
     * @return the greatest common denominator of n1 and n2
     */
    public static int gcd(int n1, int n2) {
        if (n2 == 0) {
            return n1;
        } else {
            return gcd(n2, n1 % n2);
        }
    }


    // calculated the least common multiple of n1 and n2

    /**
     * compute the least common multitude
     *
     * @param n1 first integer
     * @param n2 second integer
     * @return least common multitude of n1 and n2
     */
    public static int lcm(int n1, int n2) {
        return (n1 * n2) / gcd(n1, n2);
    }

    /**
     * compute least common multitude of all the integers in the array argument
     *
     * @param numbers array of integers
     * @return least common multitude of all integers in numbers
     */
    public static int lcm(int[] numbers) {
        if (numbers.length == 2) {   // end recursion
            return lcm(numbers[0], numbers[1]);
        }
        int[] tail = new int[numbers.length - 1];
        for (int i = 1; i < numbers.length; i++) {
            tail[i - 1] = numbers[i];
        }
        return lcm(numbers[0], lcm(tail));
    }

    /**
     * rounds the val argument to places precision,
     * more precisely the return value is the nearest value
     * near val that is a multiple of 10^precision.
     *
     * @param val    double to round
     * @param places precision
     * @return val rounded to the nearest multiple of 10^precision
     */
    public static double round(double val, int places) {
        long factor = (long) Math.pow(10, places);
        val = val * factor;
        long tmp = Math.round(val);
        return (double) tmp / factor;
    }

    /**
     * compute the standard deviation of the given values
     *
     * @param values a List of BigDecimal values
     * @return the standard deviation of the given values
     */
    public static double stddev(List values) {
        BigDecimal avg = avg(values);
        int size = values.size();
        BigDecimal value;
        List deviations = new ArrayList();
        for (int i = 0; i < size; i++) {
            value = (BigDecimal) values.get(i);
            deviations.add(new BigDecimal(Math.pow(value.add(avg.negate()).doubleValue(), 2d)));
        }
        return Math.sqrt(avg(deviations).doubleValue());
    }

    /**
     * compute the average of the given values
     *
     * @param values a List of BigDecimal values
     * @return the average of the given values
     */
    public static BigDecimal avg(List values) {
        int size = values.size();
        BigDecimal value;
        BigDecimal sum = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            value = (BigDecimal) values.get(i);
            sum = sum.add(value);
        }
        return sum.divide(new BigDecimal(size), BigDecimal.ROUND_HALF_UP);
    }

    /**
     * compute the sum of the given values
     *
     * @param values a List of BigDecimal values
     * @return the sum of the given values
     */
    public static BigDecimal sum(List values) {
        int size = values.size();
        BigDecimal value;
        BigDecimal sum = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            value = (BigDecimal) values.get(i);
            sum = sum.add(value);
        }
        return sum;
    }

    /**
     * compute the maximum of the given values
     *
     * @param values a List of BigDecimal values
     * @return the maximum of the given values
     */
    public static BigDecimal max(List values) {
        int size = values.size();
        BigDecimal value;
        BigDecimal max = null;
        for (int i = 0; i < size; i++) {
            value = (BigDecimal) values.get(i);
            if (max == null) {
                max = value;
            } else if (max.compareTo(value) == -1) {
                max = value;
            }
        }
        return max;
    }

    /**
     * compute the minimum of the given values
     *
     * @param values a List of BigDecimal values
     * @return the minimum of the given values
     */
    public static BigDecimal min(List values) {
        int size = values.size();
        BigDecimal value;
        BigDecimal min = null;
        for (int i = 0; i < size; i++) {
            value = (BigDecimal) values.get(i);
            if (min == null) {
                min = value;
            } else if (min.compareTo(value) == 1) {
                min = value;
            }
        }
        return min;
    }

    /**
     * Multiplies two integers throwing an exception if an overflow occurs.
     *
     * @param a an integer
     * @param b an integer
     * @return the result of a * b
     * @throws ArithmeticException if an overflow occurs
     */
    public static int safeMultiply(int a, int b) throws ArithmeticException {
        long result = (long) a * (long) b;
        int desiredhibits = -((int) (result >>> 31) & 1);
        int actualhibits = (int) (result >>> 32);
        if (desiredhibits == actualhibits) {
            return (int) result;
        } else {
            throw new ArithmeticException(a + " * " + b + " = " + result);
        }
    }

    /**
     * Multiplies a set of integers throwing an exception if an overflow occurs.
     *
     * @param ints an array of integers
     * @return the result of integers multiplied
     * @throws ArithmeticException if an overflow occurs
     */
    public static int safeMultiply(int[] ints) throws ArithmeticException {
        if (ints.length == 0) {
            return 0;
        }
        if (ints.length == 2) {
            return safeMultiply(ints[0], ints[1]);
        }

        int arg0 = ints[0];
        int arg1 = ints[1];
        int result = safeMultiply(arg0, arg1);
        for (int i = 2; i < ints.length; i++) {
            result = safeMultiply(result, ints[i]);
        }
        return result;
    }
}
