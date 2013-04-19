package com.elster.jupiter.cbo;

public enum DataQualifier {
	NOTAPPLICABLE (0,"NotApplicable"),
	ABSOLUTE (1,"Absolute"),
	AVERAGE (2,"Average"),
	COMPENSATED (3,"Compensated"),
	EXCESS (4,"Excess"),
	HIGHTRESHOLD (5,"HighThreshold"),
	INCREMENTAL (6,"Incremental"),
	LOWTTHRESHOLD (7,"LowThreshold"),
	MAXIMUM (8,"Maximum"),
	MINIMUM (9,"Minumum"),
	MISSING (10,"Missing"),
	NOMINAL (11,"Nominal"),
	NORMAL (12,"Normal"),
	RELATIVE (13,"Relative"),
	REQUIRED (14,"Required"),
	RMS (15,"Root Mean Squared"),
	SECONDMAXIMUM (16,"SecondMaximum"),
	SECONDMINIMUM (17,"SecondMinimum"),
	SURVEYDATA (18,"Survey Data"),
	TESTDATA (19,"Test Data"),
	UNCOMPENSTATED (20,"Uncompensated"),
	UNSTABLE (21,"Unstable"),
	ACTION (22,"Action");
	
	private final int id;
	private final String description;
	
	private DataQualifier(int id , String description) {
		this.id = id;
		this.description = description;
	}	
	
	public static DataQualifier get(int id) {
		return values()[id];
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

}
