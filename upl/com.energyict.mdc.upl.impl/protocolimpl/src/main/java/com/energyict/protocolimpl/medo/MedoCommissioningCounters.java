package com.energyict.protocolimpl.medo;

public class MedoCommissioningCounters extends Parsers {
	char[] cntr= new char[48];
	
	MedoCommissioningCounters(){}
	MedoCommissioningCounters(byte[] b){
		cntr=parseBArraytoCArray(b);
	}
	MedoCommissioningCounters(char[] c){
		cntr=c;
	}
	public String toString(){
		String s="";
		for(int i=0; i<48; i++){
			s+= cntr[i]+" ";
		}
		return s;
	}
	public void printData(){
		System.out.println("cntr:   "+this.toString());
	}
	/**
	 * @return the cntr
	 */
	public char[] getCntr() {
		return cntr;
	}
	/**
	 * @param cntr the cntr to set
	 */
	public void setCntr(char[] cntr) {
		this.cntr = cntr;
	}
	
}
