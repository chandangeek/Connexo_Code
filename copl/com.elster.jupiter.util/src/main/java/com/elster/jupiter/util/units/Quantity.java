package com.elster.jupiter.util.units;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;

@XmlRootElement
@XmlJavaTypeAdapter(QuantityAdapter.class)

/**
 * Immutable class representing a quantity in one unit.
 */
public final class Quantity {
	private final Unit unit;
	private final BigDecimal value;
	private final int multiplier;
	
	@SuppressWarnings("unused")
	private Quantity() {		
		unit = null;
		value = null;
		multiplier = 0;
	}
	
	Quantity(Unit unit, BigDecimal value , int multiplier) {
		this.unit = unit;
		this.value = value;
		this.multiplier = multiplier;
	}

	Quantity(Unit unit, BigDecimal value) {
		this(unit,value,0);
	}
	
	public Unit getUnit() {
		return unit;
	}

	public BigDecimal getValue() {
		return value;
	}

	public int getMultiplier() {
		return multiplier;
	}
	
	private String getCode(int exponent, boolean asciiOnly) {
		switch(exponent) {
			case -12:
				return "p";
			case -9:
				return "n";
			case -6:
				return asciiOnly ? "micro" : "\u00b5";
			case -3:
				return "m";
			case -2:
				return "c";
			case -1:
				return "d";
			case 0:
				return "";
			case 1:
				return "Da";
			case 2:
				return "h";
			case 3:
				return "k";
			case 6:
				return "M";
			case 9:
				return "G";
			case 12:
				return "T";
			case 15:
				return "P";
			case 18:
				return "E";
			default:
				return "*10^" + exponent;					
		}
	}
	
	public Quantity asSi() {
		if (unit.isDimensionLess() || unit.isCoherentSiUnit()) {
			return this;
		}
		BigDecimal newValue = unit.siValue(value.scaleByPowerOfTen(multiplier));		
		return new Quantity(Unit.getSIUnit(unit.getDimension()),newValue);
	}
	
	@Override
	public String toString() {		
		return "" + value + " " + getCode(multiplier, false) + unit.getSymbol();
	}
	
	public String toString(boolean asciiOnly) {
		return "" + value + " " + getCode(multiplier, asciiOnly) + unit.getSymbol(asciiOnly);
	}
	
	public static Quantity create(BigDecimal value , int multiplier , String unitSymbol) {
		Unit unit = Unit.get(unitSymbol);
		if (unit == null) {
			throw new IllegalArgumentException(unitSymbol);
		}
		return unit.amount(value,multiplier);
	}
	
	public static Quantity create(BigDecimal value , String unitSymbol) {
		return create(value, 0, unitSymbol);
	}
	
	public static void main(String[] args) {
		for (Unit each : Unit.values()) {
			if (!each.isDimensionLess() && !each.isCoherentSiUnit()) {
				Quantity q = each.amount(BigDecimal.ONE,3);
				try {
					System.out.println("" + q + " : " + q.asSi());
				} catch (IllegalArgumentException ex) {
					System.out.println("No SI unit for " + q.getUnit());
				}
			}
		}
	}
}
