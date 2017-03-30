/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.kenda.medo;


public class MedoTelno {
	/*
	 * holds telephone numbers, char because ASCII is requested
	 */
	private int num;
	private char[] telno; // zeros
	
	MedoTelno(){
		num=16; // default 16 char lang
		telno=new char[num];
	}
	
	MedoTelno(String telno){
		num=16;
		this.telno=new char[num];
		numberPatch(telno.toCharArray());
	}
	
	MedoTelno(char[] telno){
		// what if telephone number is not 16 bytes? => fills in spaces at the end
		// longer than 16 bytes is cut of number
		num=16;
		this.telno=new char[num];
		numberPatch(telno);
	}
		
	protected void numberPatch(char[] telno){
		if(telno.length==num){
			this.telno=telno;			
		}else{
			for(int i=0; i<num; i++){
				if(i<telno.length){
					this.telno[i]=telno[i];
				}else{
					this.telno[i]=(char) 0x20; // space
				}
			}
		}
	}
	public String toString(){
		String s="";		
		for(int ii=0; ii<telno.length; ii++){
			char c=telno[ii];
			s+=(int) c;
		}
		return s;
	}
	public byte[] getbyteArray(){ // parse to byte array
		byte[] telnobyte=new byte[num];
		for (int i=0;i<num;i++){
			telnobyte[i]=(byte) telno[i];
		}
		return telnobyte;
	}
	public char[] getcharArray(){ // parse to byte array
		char[] telnochar=new char[num];
		for (int i=0;i<num;i++){
			telnochar[i]= telno[i];
		}
		return telnochar;
	}
	
	public void setTelno(char[] telno){
		this.telno=telno;
	}
	public void setTelno(String stringTelno){
		numberPatch(stringTelno.toCharArray());
	}
	public char[] getTelno(){
		return telno;
	}
	public String toStringASCII(){
		String s=new String(getcharArray());
		return s;
	}
	/**
	 * @return the num
	 */
	public int getNum() {
		return num;
	}

	/**
	 * @param num the num to set
	 */
	public void setNum(int num) {
		this.num = num;
	}

	/*
	public void setTelno(int[] telno){
	}
	*/
}
