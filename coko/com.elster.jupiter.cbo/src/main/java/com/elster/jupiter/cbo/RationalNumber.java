/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

import java.util.Objects;

public final class RationalNumber {

    public static final String NOT_APPLICABLE = "Not applicable";
    private final long numerator;
	private final long denominator;
	
	public static final RationalNumber NOTAPPLICABLE = new RationalNumber();
	
	private RationalNumber() {
		this.numerator = 0;
		this.denominator = 0;
	}
	
	public RationalNumber(long numerator,long denominator) {
		if (denominator == 0) {
			throw new IllegalArgumentException("Denominator cannot be 0");
		}
		this.numerator = numerator;
		this.denominator = denominator;
	}
	
	public long getNumerator() {
		return numerator;
	}
	
	public long getDenominator() {
		return denominator;
	}
	
	public double doubleValue() {
		return numerator / (double) denominator;
	}
	
	@Override
	public String toString() {
        if (denominator == 0L) {
            return NOT_APPLICABLE;
        }
		return "" + numerator + "/" + denominator;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RationalNumber that = (RationalNumber) o;

        return denominator == that.denominator && numerator == that.numerator;

    }

    @Override
    public int hashCode() {
        return Objects.hash(numerator, denominator);
    }
}
