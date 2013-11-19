package com.elster.jupiter.cbo;

public final class RationalNumber {
	private final long numerator;
	private final long denominator;
	
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
		return "" + numerator + "/" + denominator;
	}
	
}
