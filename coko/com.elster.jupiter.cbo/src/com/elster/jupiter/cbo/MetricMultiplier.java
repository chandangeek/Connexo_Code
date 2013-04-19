package com.elster.jupiter.cbo;

public enum MetricMultiplier {
	NANO (-9,"n"),
	MICRO (-6,"\u00b5"),
	MILLI (-3,"m"),
	CENTI (-2,"c"),
	DECI (-1,"d"),
	ZERO (0,""),
	DECA (1,"Da"),
	HECTO (2,"h"),
	KILO (3,"k"),
	MEGA (6,"M"),
	GIGA (9,"G");
	
	private final int multiplier;
	private final String symbol;
	
	private MetricMultiplier(int multiplier,String symbol) {
		this.multiplier = multiplier;
		this.symbol = symbol;
	}
	
	public static MetricMultiplier get(int id) {
		return with(id > 128  ?  (id - 256) : id);
	}
	
	public static MetricMultiplier with(int multiplier) {
		for (MetricMultiplier each : values()) {
			if (each.multiplier == multiplier)
				return each;
		}
		throw new IllegalArgumentException("" + multiplier);
	}
	
	public int getId() {
		return multiplier < 0 ? 256 + multiplier : multiplier;
	}
	
	public int getMultiplier() {		
		return multiplier;
	}

	public String getSymbol() {
		return symbol;	
	}
	
	@Override 
	public String toString() {
		return "*10^" + multiplier;
	}
}
