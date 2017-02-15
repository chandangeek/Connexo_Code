/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 *
 */
package com.energyict.protocolimpl.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jme
 *
 */
public class NumberTools {

	private static final int	INTEGER		= 0;
	private static final int	LONG		= 1;
	private static final int	FLOAT		= 2;
	private static final int	DOUBLE		= 3;
	private static final int	BIGINTEGER	= 4;
	private static final int	BIGDECIMAL	= 5;

	private static final Map<Class, Integer> CLASSCODES = new HashMap<Class, Integer>();

	static {
        Integer intcode = new Integer(INTEGER);
        CLASSCODES.put(Byte.class, intcode);
        CLASSCODES.put(Short.class, intcode);
        CLASSCODES.put(Integer.class, intcode);
        CLASSCODES.put(Long.class, new Integer(LONG));
        CLASSCODES.put(Float.class, new Integer(FLOAT));
        CLASSCODES.put(Double.class, new Integer(DOUBLE));
        CLASSCODES.put(BigInteger.class, new Integer(BIGINTEGER));
        CLASSCODES.put(BigDecimal.class, new Integer(BIGDECIMAL));
	}


    public static Number add(Number first, Number second) {
        switch(getCommonClassCode(first, second)) {
            case INTEGER: {
                int n1 = first.intValue();
                int n2 = second.intValue();
                int n = n1 + n2;
                return
                    (((n ^ n1) < 0) && ((n ^ n2) < 0)) // overflow check
                    ? (Number)new Long(((long)n1) + n2)
                    : (Number)new Integer(n);
            }
            case LONG: {
                long n1 = first.longValue();
                long n2 = second.longValue();
                long n = n1 + n2;
                return
                    (((n ^ n1) < 0) && ((n ^ n2) < 0)) // overflow check
                    ? (Number)toBigInteger(first).add(toBigInteger(second))
                    : (Number)new Long(n);
            }
            case FLOAT: {
                return new Float(first.floatValue() + second.floatValue());
            }
            case DOUBLE: {
                return new Double(first.doubleValue() + second.doubleValue());
            }
            case BIGINTEGER: {
                BigInteger n1 = toBigInteger(first);
                BigInteger n2 = toBigInteger(second);
                return n1.add(n2);
            }
            case BIGDECIMAL: {
                BigDecimal n1 = toBigDecimal(first);
                BigDecimal n2 = toBigDecimal(second);
                return n1.add(n2);
            }
        }

        return null;

    }

    private static int getClassCode(Number num) {
        try {
            return ((Integer)CLASSCODES.get(num.getClass())).intValue();
        }
        catch(NullPointerException e) {
            if(num == null) {
                throw new IllegalArgumentException("Unknown number type null");
            }
            throw new IllegalArgumentException("Unknown number type " + num.getClass().getName());
        }
    }

    private static int getCommonClassCode(Number num1, Number num2) {
        int c1 = getClassCode(num1);
        int c2 = getClassCode(num2);
        int c = c1 > c2 ? c1 : c2;
        // If BigInteger is combined with a Float or Double, the result is a
        // BigDecimal instead of BigInteger in order not to lose the
        // fractional parts. If Float is combined with Long, the result is a
        // Double instead of Float to preserve the bigger bit width.
        switch(c) {
            case FLOAT: {
                if((c1 < c2 ? c1 : c2) == LONG) {
                    return DOUBLE;
                }
                break;
            }
            case BIGINTEGER: {
                int min = c1 < c2 ? c1 : c2;
                if((min == DOUBLE) || (min == FLOAT)) {
                    return BIGDECIMAL;
                }
                break;
            }
        }
        return c;
    }

    private static BigInteger toBigInteger(Number num) {
        return num instanceof BigInteger ? (BigInteger) num : new BigInteger(num.toString());
    }

    private static BigDecimal toBigDecimal(Number num) {
        return num instanceof BigDecimal ? (BigDecimal) num : new BigDecimal(num.toString());
    }

}
