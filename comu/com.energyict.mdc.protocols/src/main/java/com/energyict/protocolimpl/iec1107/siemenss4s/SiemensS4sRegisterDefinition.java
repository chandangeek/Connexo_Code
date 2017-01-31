/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.siemenss4s;

import com.energyict.mdc.common.ObisCode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Describes a Siemens S4s register.
 * @author gna
 *
 */
public class SiemensS4sRegisterDefinition {

	private String location;			// the memory address in the device
	private ObisCode obisCode;			// the corresponding obisCode
	private String length;				// the amount of NIBBLES to read or write

	public SiemensS4sRegisterDefinition(String location, String length){
		this(location, length, null);
	}

	public SiemensS4sRegisterDefinition(String location, String length, ObisCode oc){
		this.location = location;
		this.length = length;
		this.obisCode = oc;
	}

	public String getLocation(){
		return this.location;
	}

	public ObisCode getObisCode(){
		return this.obisCode;
	}

	public String length(){
		return this.length;
	}

	public byte[] prepareRead() throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write(location.getBytes());
		baos.write(("(" + length() + ")").getBytes());
		return baos.toByteArray();
	}

	public boolean isObisCode(ObisCode oc){
		if(oc.toString().equalsIgnoreCase(this.obisCode.toString())){
			return true;
		}
		return false;
	}

	protected void setLength(String length){
		this.length = length;
	}
}
