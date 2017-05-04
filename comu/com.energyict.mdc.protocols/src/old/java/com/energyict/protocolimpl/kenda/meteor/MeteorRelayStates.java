/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.kenda.meteor;

public class MeteorRelayStates extends Parsers implements MeteorCommandAbstract {
	private char states=0; // 1 bit per relay
	private char mask=0;   // relay affect mask
	
	// constructors
	MeteorRelayStates(){}
	MeteorRelayStates(char[] c){
		process(c);
	}
	MeteorRelayStates(byte[] b){
		process(parseBArraytoCArray(b));
	}
	
	private void process(char[] c){
		states=c[0];
		mask=c[1];
	}
	public byte[] parseToByteArray() {
		byte[] b=new byte[2];
		b[0]=(byte) states;
		b[1]=(byte) mask;
		return b;
	}

	public void printData() {
		// binary data
		System.out.println("states:         "+Integer.toBinaryString((int) states));
		System.out.println("mask:         "+Integer.toBinaryString((int) mask));
	}
	public String toString(){
		return ("states: "+Integer.toBinaryString((int) states)+" mask: "+Integer.toBinaryString((int) mask)+" ");
	}
	/**
	 * @return the states
	 */
	public char getStates() {
		return states;
	}
	/**
	 * @param states the states to set
	 */
	public void setStates(char states) {
		this.states = states;
	}
	/**
	 * @return the mask
	 */
	public char getMask() {
		return mask;
	}
	/**
	 * @param mask the mask to set
	 */
	public void setMask(char mask) {
		this.mask = mask;
	}

}
