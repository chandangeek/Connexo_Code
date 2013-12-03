package com.elster.jupiter.cbo;

public enum Commodity {
	NOTAPPLICABLE (0,"NotApplicable"),
	ELECTRICITY_SECONDARY_METERED (1,"Electricity Secondary Metered"),
	ELECTRICITY_PRIMARY_METERED (2,"Electricity Primary Metered"),
	COMMUNICATION (3,"Communication"),
	AIR (4,"Air"),
	INSULATIVEGAS (5,"Insulative Gas"),
	INSULATIVEOIL (6,"Insulative Oil"),
	NATURALGAS (7,"Natural Gas"),
	PROPANE (8,"Propane"),
	POTABLEWATER (9,"Potable Water"),
	STEAM (10,"Steam"),
	WASTEWATER (11,"Waste Water"),
	HEATINGFLUID (12,"Heating Fluid"),
	COOLINGFLUID(13,"Cooling Fluild"),
	NONPOTABLEWATER(14,"Non Potable Water"),
	NOX(15,"NOx"),
	SO2(16,"SO2"),
	CH4(17,"CH4"),
	CO2(18,"CO2"),
	CARBON(19,"Carbon"),
	HCH(20,"HCH"),
	PFC(21,"PFC"),
	SF6(22,"SF6"),
    TVLICENSE(23,"TV License"),
    INTERNET(24,"Internet"),
    REFUSE(25,"Trash");
	
	private final int id;
	private final String description;
	
	Commodity(int id, String description) {
		this.id = id;
		this.description = description;
	}
	
	public static Commodity get(int id) {
		for (Commodity each : values()) {
			if (each.id == id) {
                return each;
            }
		}
		throw new IllegalArgumentException("" + id);
	}
	
	@Override
	public String toString() {
		return "Commodity " + id + " : " + description;
	}
	
	public int getId() {
		return id;
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean isApplicable() {
		return id != 0;
	}
	
}
