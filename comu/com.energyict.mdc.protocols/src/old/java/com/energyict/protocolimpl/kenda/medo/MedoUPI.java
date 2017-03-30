/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.kenda.medo;


public class MedoUPI {
	private char m=0, d=0;
	
	MedoUPI(){
	}
	MedoUPI(char[] md){
		d=md[1];
		m=md[0];
	}
	MedoUPI(char m, char d){
		this.d=d;
		this.m=m;
	}
	public String toString(){
		return "m: "+(int) (m & 0x00FF)+ "d: "+(int) (d & 0x00FF);
	}
	public void setm(char m){
		this.m=m;
	}
	public void setm(byte m){
		this.m=(char) m;
	}
	public void setd(char d){
		this.d=d;
	}
	public void setd(byte d){
		this.d=(char) d;
	}
	public char getdchar(){
		return d;
	}
	public byte getdbyte(){
		return (byte) d;
	}
	public char getmchar(){
		return m;		
	}
	public byte getmbyte(){
		return (byte) m;
	}
	public byte[] getbyteArray(){
		byte[] array = new byte[2];
		array[0]=(byte) m;
		array[1]=(byte) d;
		return array;
	}
	public char[] getcharArray(){
		char[] chararray={m, d};
		return chararray;
	}
}
