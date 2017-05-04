/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.kenda.medo;

public class MedoDirectMemoryAccess extends Parsers{
	private short ptr;
	private short cnt;
	private char[] datafield;
	
	MedoDirectMemoryAccess(){}
	
	MedoDirectMemoryAccess(short ptr, short cnt, char[] datafield){
		this.ptr=ptr;
		this.cnt=cnt;
		this.datafield=datafield;
	}
	
	public byte[] getSendRequestArray(){
		String serial="";
		serial+=new String(parseShortToChar(ptr));
		serial+=new String(parseShortToChar(cnt));
		return parseCArraytoBArray(serial.toCharArray());
	}
	public void setReceivedDataArray(char[] datafield){
		this.datafield=datafield;
	}
	public void setReceivedDataArray(byte[] datafield){
		this.datafield=parseBArraytoCArray(datafield);
	}

	/**
	 * @return the ptr
	 */
	public short getPtr() {
		return ptr;
	}

	/**
	 * @param ptr the ptr to set
	 */
	public void setPtr(short ptr) {
		this.ptr = ptr;
	}

	/**
	 * @return the cnt
	 */
	public short getCnt() {
		return cnt;
	}

	/**
	 * @param cnt the cnt to set
	 */
	public void setCnt(short cnt) {
		this.cnt = cnt;
	}

	/**
	 * @return the datafield
	 */
	public char[] getDatafield() {
		return datafield;
	}

	/**
	 * @param datafield the datafield to set
	 */
	public void setDatafield(char[] datafield) {
		this.datafield = datafield;
	}

	byte[] parseToByteArray() {
		// TODO Auto-generated method stub
		return null;
	}
}
