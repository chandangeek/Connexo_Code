/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.geo;

import java.math.BigDecimal;

import static com.elster.jupiter.util.Checks.is;

public class Angle implements Comparable<Angle> {

    private static final BigDecimal MINUTES_PER_HOUR = BigDecimal.valueOf(60);
    private static final BigDecimal BD3600 = BigDecimal.valueOf(3600);
    private static final int BITS_PER_INT = 32;
    private static final int SECONDS_PER_MINUTE = 60;
    private final BigDecimal value;
	
	Angle (BigDecimal value)  {
		if (value == null) {
			throw new IllegalArgumentException();
		}
		this.value = value;
	}

	public final int hashCode() {
		long bits = Double.doubleToLongBits(this.value.doubleValue());
		return (int) (bits ^ (bits >>> BITS_PER_INT));
	}

	public final boolean equals(Object other) {
		if (other == null) {
            return false;
        }
		if (!(other instanceof Angle)) {
            return false;
        }
		Angle o = (Angle) other;
		return is(this.value).equalValue(o.value);
	}

	@Override
	public String toString() {
		return (signum() < 0 ? "-" : "") + baseString();
	}
	
	@Override
    public final int compareTo(Angle other) {
		return value.compareTo(other.value);
	}
	
	public final BigDecimal getValue() {
		return value;
	}
	
	public final int getDegrees() {
		return Math.abs(value.intValue());
	}
	
	public final int getMinutes() {
        return value.remainder(BigDecimal.ONE).abs().multiply(MINUTES_PER_HOUR).intValue();
	}
	
	public final int getSeconds() {
        return value.remainder(BigDecimal.ONE).abs().multiply(BD3600).intValue() % SECONDS_PER_MINUTE;
	}
	
	public final Angle subtract(Angle other) {
		return new Angle(this.value.subtract(other.value));
	}
	
	public final double toRadians() {
		return Math.toRadians(value.doubleValue());
	}

	public final double cos() {
		return Math.cos(toRadians());
	}

    public final double sin() {
        return Math.sin(toRadians());
    }

	public final int signum() {
		return value.signum();
	}

	String baseString() {
		return getDegrees() + "\u00B0" + getMinutes() + "'" + getSeconds() + "''";
	}

	
}

