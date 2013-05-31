package com.elster.jupiter.cbo;

public final class StreetDetail implements Cloneable {
	private String addressGeneral;
	private String buildingName;
	private String code;
	private String name;
	private String number;
	private String prefix;
	private String suffix;
	private String suiteNumber;
	private String type;
	private boolean withinTownLimits = true;
	
	public StreetDetail() {		
	}
	
	public StreetDetail(String name , String number) {
		this.name = name;
		this.number = number;
	}

	public String getAddressGeneral() {
		return addressGeneral;
	}

	public void setAddressGeneral(String addressGeneral) {
		this.addressGeneral = addressGeneral;
	}

	public String getBuildingName() {
		return buildingName;
	}

	public void setBuildingName(String buildingName) {
		this.buildingName = buildingName;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public String getSuiteNumber() {
		return suiteNumber;
	}

	public void setSuiteNumber(String suiteNumber) {
		this.suiteNumber = suiteNumber;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isWithinTownLimits() {
		return withinTownLimits;
	}

	public void setWithinTownLimits(boolean withinTownLimits) {
		this.withinTownLimits = withinTownLimits;
	}
	
	public StreetDetail copy() {
		try {
			return (StreetDetail) clone();
		} catch (CloneNotSupportedException e) {
			// should not happen
			throw new UnsupportedOperationException(e);
		}
	}
	
	@Override
	public String toString() {
		return "" + getName() + " " + getNumber(); 
	}
	
	boolean isEmpty() {
		return 
			addressGeneral == null && 
			buildingName == null &&
			code == null &&
			name == null &&
			number == null &&
			prefix == null &&
			suffix == null &&
			suiteNumber == null &&
			type == null;		
	}
}
