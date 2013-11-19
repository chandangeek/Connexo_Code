package com.elster.jupiter.cbo;

public enum MetricMultiplier {
	PICO(-12,"p"),
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
	GIGA (9,"G"),
	TERA(12,"T");
	
	private final int multiplier;
	private final String symbol;
	
	MetricMultiplier(int multiplier,String symbol) {
		this.multiplier = multiplier;
		this.symbol = symbol;
	}
	
	public static MetricMultiplier get(int id) {
        return with((byte) id); // interpret id as signed byte
	}
	
	public static MetricMultiplier with(int multiplier) {
		for (MetricMultiplier each : values()) {
			if (each.multiplier == multiplier) {
                return each;
            }
		}
		throw new IllegalArgumentException("" + multiplier);
	}
	
	public int getId() {
        return ((byte) multiplier) & 0xff; // interpret multiplier as unsigned byte
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
