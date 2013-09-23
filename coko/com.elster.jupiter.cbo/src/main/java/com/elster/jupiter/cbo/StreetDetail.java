package com.elster.jupiter.cbo;

import javax.xml.bind.annotation.XmlTransient;

import com.elster.jupiter.util.HasName;

public final class StreetDetail implements Cloneable, HasName {
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StreetDetail that = (StreetDetail) o;

        if (withinTownLimits != that.withinTownLimits) {
            return false;
        }
        if (addressGeneral != null ? !addressGeneral.equals(that.addressGeneral) : that.addressGeneral != null) {
            return false;
        }
        if (buildingName != null ? !buildingName.equals(that.buildingName) : that.buildingName != null) {
            return false;
        }
        if (code != null ? !code.equals(that.code) : that.code != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (number != null ? !number.equals(that.number) : that.number != null) {
            return false;
        }
        if (prefix != null ? !prefix.equals(that.prefix) : that.prefix != null) {
            return false;
        }
        if (suffix != null ? !suffix.equals(that.suffix) : that.suffix != null) {
            return false;
        }
        if (suiteNumber != null ? !suiteNumber.equals(that.suiteNumber) : that.suiteNumber != null) {
            return false;
        }
        return !(type != null ? !type.equals(that.type) : that.type != null);

    }

    @Override
    public int hashCode() {
        int result = addressGeneral != null ? addressGeneral.hashCode() : 0;
        result = 31 * result + (buildingName != null ? buildingName.hashCode() : 0);
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (number != null ? number.hashCode() : 0);
        result = 31 * result + (prefix != null ? prefix.hashCode() : 0);
        result = 31 * result + (suffix != null ? suffix.hashCode() : 0);
        result = 31 * result + (suiteNumber != null ? suiteNumber.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (withinTownLimits ? 1 : 0);
        return result;
    }

    @Override
	public String toString() {
		return "" + getName() + " " + getNumber(); 
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
