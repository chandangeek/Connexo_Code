package com.energyict.protocolimpl.meteor;

public class MeteorFirmwareVersion extends Parsers implements MeteorCommandAbstract{
	private String version;
	MeteorFirmwareVersion(){}
	MeteorFirmwareVersion(char[] c){
		processFirmwareVersion(c);
	}
	MeteorFirmwareVersion(byte[]b ){
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
	public byte[] parseToByteArray() {
		return parseCArraytoBArray(version.toCharArray());
	}
	public void printData() {
		System.out.println(version);
	}
}
