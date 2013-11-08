package com.energyict.protocolimpl.kenda.medo;

// class not used anymore, to be deleted when project is submitted.
public class MedoComDtls {
	private char[][] details = new char [4][32];
	private byte internalCount; // used when addString is used
	private char[][] tempArray = new char[4][0];
	
	MedoComDtls(){
		internalCount=0;
	}
	MedoComDtls(String s){
		// use only when reading from the meter!!!!
		internalCount=4;
		tempArray[0]=s.substring(0,32).toCharArray();
		tempArray[1]=s.substring(32,64).toCharArray();
		tempArray[2]=s.substring(64,96).toCharArray();
		tempArray[3]=s.substring(96).toCharArray();
	}
	MedoComDtls(String s1,String s2,String s3,String s4){
		internalCount=4;
		tempArray[0]=s1.toCharArray();
		tempArray[1]=s2.toCharArray();
		tempArray[2]=s3.toCharArray();
		tempArray[3]=s4.toCharArray();
		processTempArray();
	}
	
	private void processTempArray(){
		// makes a 128 byte array
		for(int i=0; i<4; i++){
			for(int ii=0; ii<32; ii++){
				if(tempArray[i].length>ii){
					// fill in matrix					
					details[i][ii]=tempArray[i][ii];
				}else{
					// parse with ' '
					details[i][ii]=' ';
				}
			}
		}
	}
	public boolean addString(String s){
		if(internalCount==4){
			return false;
		}else{
			tempArray[internalCount]=s.toCharArray();
			processTempArray();
			internalCount++;
			return true;
		}
	}
	public String[] getStringArray(){
		String[] s=new String[4];
		for(int i=0; i<4; i++){
			s[i]=details[i].toString();
		}
		return s;
	}
	public byte[] getbyteArray(){ // parse and serialize to 1D byte array
		int counter=0;
		byte[] byteArray=new byte[128];
		for(int i=0; i<4; i++){
			for(int ii=0; ii<32; ii++){
				byteArray[counter]=(byte) details[i][ii];
				counter++;
			}
		}				
		return byteArray;
	}
	public char[] getcharArray(){ // serialize to 1D byte array
		int counter=0;
		char[] charArray=new char[128];
		for(int i=0; i<4; i++){
			for(int ii=0; ii<32; ii++){
				charArray[counter]= details[i][ii];
				counter++;
			}
		}				
		return charArray;
	}

	/**
	 * @return the details
	 */
	public char[][] getDetails() {
		return details;
	}

	/**
	 * @param details the details to set
	 */
	public void setDetails(char[][] details) {
		this.details = details;
	}
}
