package com.energyict.protocolimpl.kenda.medo;


public class MedoLoadDialReadings extends Parsers{
	private char mtr=0;
	private int cnt=0;
	
	MedoLoadDialReadings(){}
	MedoLoadDialReadings(char mtr, int cnt){
		this.mtr=mtr;
		this.cnt=cnt;
	}
	MedoLoadDialReadings(char[] c){
		processMedoLoadCurrentDialReadings(c);
	}
	MedoLoadDialReadings(byte[] b){
		processMedoLoadCurrentDialReadings(parseBArraytoCArray(b));
	}
	
	private void processMedoLoadCurrentDialReadings(char[] c) {
		mtr=c[0];
		String s=new String(c);
		cnt=parseCharToInt(s.substring(1,5).toCharArray());
	}
	
	public byte[] parseToByteArray(){
		String serial=""+mtr+""+new String(parseIntToChar(cnt));
		return parseCArraytoBArray(serial.toCharArray());
	}
	/**
	 * @return the mtr
	 */
	public char getMtr() {
		return mtr;
	}
	/**
	 * @param mtr the mtr to set
	 */
	public void setMtr(char mtr) {
		this.mtr = mtr;
	}
	/**
	 * @return the cnt
	 */
	public int getCnt() {
		return cnt;
	}
	/**
	 * @param cnt the cnt to set
	 */
	public void setCnt(int cnt) {
		this.cnt = cnt;
	}
	
}
