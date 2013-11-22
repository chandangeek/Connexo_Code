package com.elster.jupiter.util.units;

public class ScaledUnit {
	private final Unit unit;
	private final int multiplier;
	
	public ScaledUnit(Unit unit, int multiplier) {
		this.unit = unit;
		this.multiplier = multiplier;
	}

	public Unit getUnit() {
		return unit;
	}

	public int getMultiplier() {
		return multiplier;
	}
}
