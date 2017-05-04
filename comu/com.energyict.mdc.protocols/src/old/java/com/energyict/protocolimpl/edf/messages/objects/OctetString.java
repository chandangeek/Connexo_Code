/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.edf.messages.objects;


public class OctetString {
	
	protected final static String ELEMENTNAME = "octetString";
	protected final static String SEPARATOR = ":";
	
	private byte[] octets;

	public OctetString() {
		super();
		octets = new byte[1];
	}
	
	public OctetString(String octets){
		super();
		convertStringToOctetString(octets);
	}
	
	public OctetString(byte[] octets){
		this.octets = octets;
	}
	
	public OctetString(byte value){
		this.octets = new byte[1];
		this.octets[0] = value;
	}
		
	public byte[] getOctets() {
		return octets;
	}

	public void setOctets(byte[] octets) {
		this.octets = octets;
	}

	public String convertOctetStringToString(){
		String result = "";
		for (int i = 0; i < octets.length; i++){
			result += Integer.toHexString(((int)octets[i] & 0xFF)) + SEPARATOR;
		}
		return result;
	}

	public void convertStringToOctetString(String input){
		String[] tokens = input.split(SEPARATOR);
		octets = new byte[tokens.length];
		for (int i = 0; i < tokens.length; i++){
			octets[i] =(byte) (Integer.parseInt(tokens[i],16) & 0x000000FF);
		}
	}
}
