package com.energyict.protocolimpl.kenda.meteor;

public class MeteorReadDialReadings extends Parsers{
	private int[] cnt= new int[48];
	
	MeteorReadDialReadings(){}
	MeteorReadDialReadings(int[] i){
		for(int ii=0; ii<i.length && ii <48; ii++){
			cnt[ii]=i[ii];
		}
	}
	MeteorReadDialReadings(byte[] b){
		processMeteorReadDialReadings(parseBArraytoCArray(b));		
	}

	MeteorReadDialReadings(char[] c){
		processMeteorReadDialReadings(c);
	}
	private void processMeteorReadDialReadings(char[] c){
		char[] tempc=new char[4];
		for(int i=0; i<48; i++){
			tempc[0]=c[(i*4)+0];
			tempc[1]=c[(i*4)+1];
			tempc[2]=c[(i*4)+2];
			tempc[3]=c[(i*4)+3];
			cnt[i]=parseCharToInt(tempc);
		}
	}
	public String toString(){
		String s = "cnt:        ";
		for(int i=0; i<48; i++){
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
