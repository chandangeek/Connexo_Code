package com.energyict.protocolimpl.instromet.v444.tables;

import com.energyict.obis.*;

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
