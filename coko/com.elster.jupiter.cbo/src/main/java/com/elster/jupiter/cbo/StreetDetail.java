/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

import com.elster.jupiter.util.HasName;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;

import java.util.Objects;

public final class StreetDetail implements Cloneable, HasName {

    private String addressGeneral;
	private String buildingName;
	private String code;
	@NotNull
	@Size(min=1)
	private String name;
	@NotNull
	@Pattern(regexp=".*[0-9].*")  // at least one digit
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StreetDetail that = (StreetDetail) o;

        return hasEqualValues(that);
    }

    private boolean hasEqualValues(StreetDetail that) {
        return Objects.equals(addressGeneral, that.addressGeneral)
                && Objects.equals(buildingName, that.buildingName)
                && Objects.equals(code, that.code)
                && Objects.equals(name, that.name)
                && Objects.equals(number, that.number)
                && Objects.equals(prefix, that.prefix)
                && Objects.equals(suffix, that.suffix)
                && Objects.equals(suiteNumber, that.suiteNumber)
                && Objects.equals(type, that.type)
                && Objects.equals(withinTownLimits, that.withinTownLimits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(addressGeneral, buildingName, code, name, number, prefix, suffix, suiteNumber, type, withinTownLimits);
    }

    @Override
	public String toString() {
		return getName() + ' ' + getNumber();
	}
	
    @XmlTransient
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
