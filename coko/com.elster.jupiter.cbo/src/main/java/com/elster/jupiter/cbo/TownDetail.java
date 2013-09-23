package com.elster.jupiter.cbo;

import javax.xml.bind.annotation.XmlTransient;

import com.elster.jupiter.util.HasName;

public final class TownDetail implements Cloneable, HasName {
	
	private String code;
	private String country;
	private String name;
	private String section;
	private String stateOrProvince;
	
	public TownDetail() {
	}

	public TownDetail(String code , String name , String country) {
		this.code = code;
		this.name = name;
		this.country = country;
	}
	
	public TownDetail(String code , String name , String section , String stateOrProvince , String country) {
		this(code,name,country);
		this.stateOrProvince = stateOrProvince;
		this.country = country;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public String getStateOrProvince() {
		return stateOrProvince;
	}

	public void setStateOrProvince(String stateOrProvince) {
		this.stateOrProvince = stateOrProvince;
	}
	
	public TownDetail copy() {
		try {
			return (TownDetail) clone();
		} catch (CloneNotSupportedException e) {
			// should not happen
			throw new UnsupportedOperationException(e);
		}
	}
	
	@XmlTransient
	boolean isEmpty() {
		return code == null && country == null && name == null && section == null && stateOrProvince == null;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TownDetail that = (TownDetail) o;

        if (code != null ? !code.equals(that.code) : that.code != null) {
            return false;
        }
        if (country != null ? !country.equals(that.country) : that.country != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (section != null ? !section.equals(that.section) : that.section != null) {
            return false;
        }
        if (stateOrProvince != null ? !stateOrProvince.equals(that.stateOrProvince) : that.stateOrProvince != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (country != null ? country.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (section != null ? section.hashCode() : 0);
        result = 31 * result + (stateOrProvince != null ? stateOrProvince.hashCode() : 0);
        return result;
    }

    @Override
	public String toString() {
		return "" + getCode() + " " + getName() + " " + getStateOrProvince() + " " + getCountry();
	}
}
