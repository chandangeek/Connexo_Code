/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.kenda.meteor;

public class MeteorFirmwareVersion extends Parsers implements MeteorCommandAbstract{
	/**
	 * @author pst 
	 */
	private String version;
	MeteorFirmwareVersion(){}
	MeteorFirmwareVersion(char[] c){
		processFirmwareVersion(c);
	}
	MeteorFirmwareVersion(byte[]b ){
		processFirmwareVersion(parseBArraytoCArray(b));
	}
	private void processFirmwareVersion(char[] c) {
		version="";		
		for(int i=0; i<c.length; i++){
			if(c[i]>0){version+=c[i];}
		}
	}
	/**
	 * @return the version of the firmware
	 */
	public String getVersion() {
		return version;
	}
	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	public byte[] parseToByteArray() {
		return parseCArraytoBArray(version.toCharArray());
	}
	public void printData() {
		System.out.println(version);
	}
}
