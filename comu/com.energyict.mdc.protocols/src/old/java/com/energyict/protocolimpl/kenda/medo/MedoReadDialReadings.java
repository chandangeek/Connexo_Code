/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.kenda.medo;


public class MedoReadDialReadings extends Parsers{
	private int[] cnt= new int[36];
	
	MedoReadDialReadings(){}
	MedoReadDialReadings(int[] i){
		for(int ii=0; ii<i.length && ii <36; ii++){
			cnt[ii]=i[ii];
		}
	}
	MedoReadDialReadings(byte[] b){
		processMedoReadDialReadings(parseBArraytoCArray(b));		
	}

	MedoReadDialReadings(char[] c){
		processMedoReadDialReadings(c);
	}
	
	private void processMedoReadDialReadings(char[] c){
		char[] tempc=new char[4];
		for(int i=0; i<36; i++){
			tempc[0]=c[(i*4)+0];
			tempc[1]=c[(i*4)+1];
			tempc[2]=c[(i*4)+2];
			tempc[3]=c[(i*4)+3];
			cnt[i]=parseCharToInt(tempc);
		}
	}
	public String toString(){
		String s = "cnt:        ";
		for(int i=0; i<36; i++){
			s+=cnt[i] + " "; 
		}
		return s;
	}
	/**
	 * @return the cnt
	 */
	public int[] getCnt() {
		return cnt;
	}

	/**
	 * @param cnt the cnt to set
	 */
	public void setCnt(int[] cnt) {
		this.cnt = cnt;
	}
	public void printData() {
		System.out.print(this.toString());
		
	}
	byte[] parseToByteArray() {
		// TODO Auto-generated method stub
		return null;
	}
}
