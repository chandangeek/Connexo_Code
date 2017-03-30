/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.kenda.medo;

public class MedoFirmwareVersion extends Parsers{
	private String version;
	MedoFirmwareVersion(){}
	MedoFirmwareVersion(char[] c){
		processFirmwareVersion(c);
	}
	MedoFirmwareVersion(byte[]b ){
		processFirmwareVersion(parseBArraytoCArray(b));
	}
	private void processFirmwareVersion(char[] c) {
		version=new String(c);
	}
	/**
	 * @return the version
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
	byte[] parseToByteArray() {
		// TODO Auto-generated method stub
		return null;
	}
}
