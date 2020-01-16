/*
 * DbFunctions.java
 *
 * Created on 12 september 2003, 14:04
 */

package com.energyict.mdc.engine.offline.gui.util;

/**
 * @author pasquien
 */
public class DbFunctions {

    static int FUNCTION_COUNT = 23;
    static int GROUPFUNCTION_COUNT = 7;

    public static String[] getNumberFunctions() {
        String[] function = new String[FUNCTION_COUNT];
        function[0] = "ABS( x )";
        function[1] = "CEIL( x )";
        function[2] = "COS( x )";
        function[3] = "COSH( x )";
        function[4] = "DECODE( x, if1, then1, [ if2, then2 ] ... ,else )";
        function[5] = "EXP( x )";
        function[6] = "FLOOR( x )";
        function[7] = "GREATEST( x1, x2, ... )";
        function[8] = "LEAST( x1, x2, ... )";
        function[9] = "LN( x )";
        function[10] = "LOG( x )";
        function[11] = "MOD( x, divisor )";
        function[12] = "NVL( x, substitute )";
        function[13] = "POWER( x , exponent )";
        function[14] = "ROUND( x, precision )";
        function[15] = "SIGN( x )";
        function[16] = "SIN( x )";
        function[17] = "SINH( x )";
        function[18] = "SQRT( x )";
        function[19] = "TAN( x )";
        function[20] = "TANH( x )";
        function[21] = "TRUNC( x, precision )";
        function[22] = "VSIZE(value)";

        return function;
    }

    public static String[] getGroupFunctions() {
        String[] function = new String[GROUPFUNCTION_COUNT];
        function[0] = "AVG( x )";
        function[1] = "COUNT( x )";
        function[2] = "MAX( x )";
        function[3] = "MIN( x )";
        function[4] = "STDDEV( x )";
        function[5] = "SUM( x )";
        function[6] = "VARIANCE( x )";
        return function;
    }
}
