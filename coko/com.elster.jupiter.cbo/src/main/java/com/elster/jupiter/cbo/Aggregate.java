package com.elster.jupiter.cbo;

public enum Aggregate {
	NOTAPPLICABLE (0,"NotApplicable"),
	AVERAGE (2,"Average"),
	EXCESS (4,"Excess"),
	HIGHTHRESHOLD(5,"HighThreshold"),
	LOWTHRESHOLD(7,"LowThreshold"),
	MAXIMUM (8,"Maximum"),
	MINIMUM (9,"Minimum"),
	NOMINAL (11,"Nominal"),
	NORMAL (12,"Normal"),
	SECONDMAXIMUM (16,"SecondMaximum"),
	SECONDMINIMUM (17,"SecondMinimum"),
	THIRDMAXIMUM (23,"ThirdMaximum"),
	FOURTHMAXIMUM (24,"FourthMaximum"),
	FIFTHMAXIMIMUM (25,"FifthMaximum"),
	SUM (26,"Sum"),
	HIGH(27,"High"),
	LOW(28,"Low");
	
	private final int id;
	private final String description;
	
	Aggregate(int id , String description) {
		this.id = id;
		this.description = description;
	}	
	
	public static Aggregate get(int id) {
		for (Aggregate each : values()) {
			if (each.getId() == id) {
				return each;
			}
		}
		throw new IllegalEnumValueException(Aggregate.class, id);
	}
	
	@Override
	public String toString() {
		return "Data Qualifier " + id + " : " + description;
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean isApplicable() {
		return id != 0;
	}
	
	public int getId() {
		return id;
	}
}

