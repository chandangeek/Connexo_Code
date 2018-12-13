/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

import com.elster.jupiter.util.HasName;

import javax.xml.bind.annotation.XmlTransient;
import java.util.Objects;

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
        this.section = section;
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

        return hasEqualValues(that);
    }

    private boolean hasEqualValues(TownDetail that) {
        return Objects.equals(code, that.code)
                && Objects.equals(country, that.country)
                && Objects.equals(name, that.name)
                && Objects.equals(section, that.section)
                && Objects.equals(stateOrProvince, that.stateOrProvince);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, country, name, section, stateOrProvince);
    }

    @Override
	public String toString() {
		return "" + getCode() + " " + getName() + " " + getStateOrProvince() + " " + getCountry();
	}
}
