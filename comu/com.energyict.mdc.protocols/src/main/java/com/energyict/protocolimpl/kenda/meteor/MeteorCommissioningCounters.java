package com.energyict.protocolimpl.kenda.meteor;

public class MeteorCommissioningCounters extends Parsers {
	char[] cntr= new char[48];
	
	MeteorCommissioningCounters(){}
	MeteorCommissioningCounters(byte[] b){
		cntr=parseBArraytoCArray(b);
	}
	MeteorCommissioningCounters(char[] c){
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
	byte[] parseToByteArray() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
