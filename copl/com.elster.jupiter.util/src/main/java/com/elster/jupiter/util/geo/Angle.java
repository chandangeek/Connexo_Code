package com.elster.jupiter.util.geo;

import java.math.BigDecimal;

public class Angle implements Comparable<Angle> {
	final private BigDecimal value;
	
	Angle (BigDecimal value)  {
		if (value == null) {
			throw new NullPointerException();
		}
		this.value = value;
	}

	final public boolean equals(Object other) {
		if (other == null)
			return false;
		if (this.getClass() != other.getClass())
			return false;
		Angle o = (Angle) other;
		return this.value == o.value;		
	}

	final public int hashCode() {
		return value.hashCode();
	}

	@Override
	final public int compareTo(Angle other) {
		return value.compareTo(other.value);		
	}
	
	final public BigDecimal getValue() {
		return value;
	}
	
	final public int getDegrees() {
		return Math.abs(value.intValue());
	}
	
	final public int getMinutes() {
		return (int) Math.floor(Math.abs((value.doubleValue() - getDegrees() * signum()) * 60.0));
	}
	
	final public int getSeconds() {
		return (int) Math.floor(Math.abs((value.doubleValue() - (getDegrees() * signum() + getMinutes() / 60.0 * signum())) * 3600.0));  
	}
	
	final public Angle subtract(Angle other) {
		return new Angle(this.value.subtract(other.value));
	}
	
	final public double toRadians() {
		return Math.toRadians(value.doubleValue());
	}
	
	final public double cos() {
		return Math.cos(toRadians());
	}
	
	
	final public int signum() {
		return value.signum();
	}
	
	String baseString() {
		return getDegrees() + "\u00B0" + getMinutes() + "'" + getSeconds() + "\"";
	}
	
	@Override 
	public String toString() {
		return (signum() < 0 ?  "-" : "") + baseString();
	}

	
}

