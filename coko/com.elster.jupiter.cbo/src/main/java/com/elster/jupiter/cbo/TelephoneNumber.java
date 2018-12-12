/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

import javax.xml.bind.annotation.XmlTransient;

public final class TelephoneNumber implements Cloneable {
	private String areaCode;
	private String cityCode;
	private String countryCode;
	private String extension;
	private String localNumber;
	
	public TelephoneNumber(String countryCode , String areaCode , String cityCode , String localNumber , String extension) {
		this.countryCode = countryCode;
		this.areaCode = areaCode;
		this.cityCode = cityCode;
		this.localNumber = localNumber;
		this.extension = extension;
	}
	
	public TelephoneNumber(String countryCode , String areaCode , String localNumber) {
		this(countryCode, areaCode , "" , localNumber , "");
	}
	
	public TelephoneNumber(String countryCode , String localNumber) {
		this(countryCode,"" ,"" , localNumber , "");
	}
	
	public TelephoneNumber() {
	}

	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	public String getCityCode() {
		return cityCode;
	}

	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getLocalNumber() {
		return localNumber == null ? "" : localNumber;
	}

	public void setLocalNumber(String localNumber) {
		this.localNumber = localNumber;
	}
	
	public TelephoneNumber copy() {
		try {
			return (TelephoneNumber) this.clone();
		} catch (CloneNotSupportedException ex) {
			throw new UnsupportedOperationException(ex);
		}
	}
	
	@XmlTransient
	public String getNumber() {
		return "" + getCountryCode() + getAreaCode() + getCityCode() + getLocalNumber() + getExtension();
	}
	
	@XmlTransient
	public boolean isEmpty() {
		return 
			countryCode == null &&
			areaCode == null &&
			cityCode == null &&
			localNumber == null &&
			extension == null;		
	}
}
