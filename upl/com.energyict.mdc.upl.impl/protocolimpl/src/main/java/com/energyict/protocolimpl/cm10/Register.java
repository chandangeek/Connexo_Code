package com.energyict.protocolimpl.cm10;

import com.energyict.obis.ObisCode;

public class Register {
	
	private ObisCode obisCode;
	private String description;
	
	public Register(ObisCode obisCode) {
		this.obisCode = obisCode;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	protected String getDescription() {
		return description;
	}
	
	public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append(getDescription() + ", " + getObisCode() + "\n");
        return strBuff.toString();
    }  
	
	protected ObisCode getObisCode() {
		return obisCode;
	}

}

