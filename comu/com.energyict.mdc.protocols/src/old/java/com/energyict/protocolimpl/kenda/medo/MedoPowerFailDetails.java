package com.energyict.protocolimpl.kenda.medo;

import java.util.TimeZone;


public class MedoPowerFailDetails extends Parsers {
	private MedoCLK timPf= new MedoCLK();
	private MedoCLK timPr= new MedoCLK();
	private MedoReadDialReadings dialPf=new MedoReadDialReadings();
	private short perOut=0;
	private short secOut=0;
	private short lpfCNT=0;
	private MedoCLK[] pfhist= new MedoCLK[14];
	private char[] pffree={0,0,0,0,0,0};  // unused
	private TimeZone tz;
	
	MedoPowerFailDetails(){
		for (int i=0; i<14; i++){
			pfhist[i]=timPf; // zeros
		}
	}
	
	MedoPowerFailDetails(char[] c, TimeZone tz){
		this.tz=tz;
		processMedoPowerFailDetails(c);
	}
	
	MedoPowerFailDetails(byte[] b, TimeZone tz){
		this.tz=tz;
		processMedoPowerFailDetails(parseBArraytoCArray(b));
	}
	
	private void processMedoPowerFailDetails(char[] c){
		String s=new String(c);
		timPf=new MedoCLK(s.substring(0,6).toCharArray(),tz);
		timPr=new MedoCLK(s.substring(6,12).toCharArray(),tz);
		dialPf=new MedoReadDialReadings(s.substring(12,156).toCharArray());
		perOut=parseCharToShort(s.substring(156, 158).toCharArray());
		secOut=parseCharToShort(s.substring(158, 160).toCharArray());
		lpfCNT=parseCharToShort(s.substring(160, 162).toCharArray());
		for (int i=0; i<14; i++){
			pfhist[i]=new MedoCLK(s.substring(164+i*6,170+i*6).toCharArray(),tz);
		}
		pffree[0]=c[246];
		pffree[1]=c[247];
		pffree[2]=c[248];
		pffree[3]=c[249];
		pffree[4]=c[250];
		pffree[5]=c[251];
	}
	
	public void printData(){		
		System.out.println("timPf:          "+timPf.toString());
		System.out.println("timPr:          "+timPr.toString());
		System.out.println("dialPf:         "+dialPf.toString());
		System.out.println("perOut:         "+NumberToString(perOut));
		System.out.println("secOut:         "+NumberToString(secOut));
		System.out.println("lpfCNT:         "+NumberToString(lpfCNT));
		for(int i=0; i<16; i++){
			System.out.println("pfhist           :"+pfhist[i].toString());
		}
		System.out.println("pffree:          ");
		for(int i=0; i<6; i++){
			System.out.print(pffree[i]+" ");
		}
		System.out.println();
	}
	
	/**
	 * @return the timPf
	 */
	public MedoCLK getTimPf() {
		return timPf;
	}

	/**
	 * @param timPf the timPf to set
	 */
	public void setTimPf(MedoCLK timPf) {
		this.timPf = timPf;
	}

	/**
	 * @return the timPr
	 */
	public MedoCLK getTimPr() {
		return timPr;
	}

	/**
	 * @param timPr the timPr to set
	 */
	public void setTimPr(MedoCLK timPr) {
		this.timPr = timPr;
	}


	/**
	 * @return the dialPf
	 */
	public MedoReadDialReadings getDialPf() {
		return dialPf;
	}

	/**
	 * @param dialPf the dialPf to set
	 */
	public void setDialPf(MedoReadDialReadings dialPf) {
		this.dialPf = dialPf;
	}

	/**
	 * @return the perOut
	 */
	public short getPerOut() {
		return perOut;
	}

	/**
	 * @param perOut the perOut to set
	 */
	public void setPerOut(short perOut) {
		this.perOut = perOut;
	}

	/**
	 * @return the secOut
	 */
	public short getSecOut() {
		return secOut;
	}

	/**
	 * @param secOut the secOut to set
	 */
	public void setSecOut(short secOut) {
		this.secOut = secOut;
	}

	/**
	 * @return the lpfCNT
	 */
	public short getLpfCNT() {
		return lpfCNT;
	}

	/**
	 * @param lpfCNT the lpfCNT to set
	 */
	public void setLpfCNT(short lpfCNT) {
		this.lpfCNT = lpfCNT;
	}

	/**
	 * @return the pfhist
	 */
	public MedoCLK[] getPfhist() {
		return pfhist;
	}

	/**
	 * @param pfhist the pfhist to set
	 */
	public void setPfhist(MedoCLK[] pfhist) {
		this.pfhist = pfhist;
	}

	/**
	 * @return the pffree
	 */
	public char[] getPffree() {
		return pffree;
	}

	/**
	 * @param pffree the pffree to set
	 */
	public void setPffree(char[] pffree) {
		this.pffree = pffree;
	}

	byte[] parseToByteArray() {
		// TODO Auto-generated method stub
		return null;
	}	
}
