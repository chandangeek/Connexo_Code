package com.energyict.protocolimpl.kenda.medo;

public class MedoCommissioningCounters extends Parsers {
	char[] cntr= new char[48];

	MedoCommissioningCounters(){}
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
	byte[] parseToByteArray() {
		// TODO Auto-generated method stub
		return null;
	}

}