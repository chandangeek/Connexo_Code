/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.kenda.medo;

public class MedoDialReadingTimes extends Parsers{
	private char rdghr1=0;
	private char rdghr2=0;
	private char eopday=0;
	
	MedoDialReadingTimes(){}

	MedoDialReadingTimes(char rdghr1, char rdghr2, char eopday){
		this.eopday=eopday;
		this.rdghr1=rdghr1;
		this.rdghr2=rdghr2;
	}
	MedoDialReadingTimes(byte[] b){
		processReadingTimes(parseBArraytoCArray(b));
	}
	

	MedoDialReadingTimes(char[] c){
		processReadingTimes(c);
	}
	
	private void processReadingTimes(char[] c) {
		rdghr1=c[0];
		rdghr2=c[1];
		eopday=c[2];
	}

	public byte[] parseToByteArray(){
		String serial=""+rdghr1+""+rdghr2+""+eopday;
		return parseCArraytoBArray(serial.toCharArray());
	}

	
	/**
	 * @return the rdghr1
	 */
	public char getRdghr1() {
		return rdghr1;
	}
	/**
	 * @param rdghr1 the rdghr1 to set
	 */
	public void setRdghr1(char rdghr1) {
		this.rdghr1 = rdghr1;
	}
	/**
	 * @return the rdghr2
	 */
	public char getRdghr2() {
		return rdghr2;
	}
	/**
	 * @param rdghr2 the rdghr2 to set
	 */
	public void setRdghr2(char rdghr2) {
		this.rdghr2 = rdghr2;
	}
	/**
	 * @return the eopday
	 */
	public char getEopday() {
		return eopday;
	}
	/**
	 * @param eopday the eopday to set
	 */
	public void setEopday(char eopday) {
		this.eopday = eopday;
	}

}
