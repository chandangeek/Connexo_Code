/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.kenda.meteor;

import java.util.TimeZone;

public class MeteorPowerFailDetails extends Parsers {
	private MeteorCLK firstFailure= new MeteorCLK();
	private MeteorCLK lastRecovery= new MeteorCLK();
	private MeteorReadDialReadings dialPf=new MeteorReadDialReadings();
	private long perOut=0;
	private short secOut=0;
	private short lpfCNT=0;
	private MeteorCLK[] pfhist= new MeteorCLK[14];
	private char[] pffree={0,0,0,0,0,0};  // unused
	private MeteorCLK timeOp2= new MeteorCLK();
	private MeteorReadDialReadings dialseOp2=new MeteorReadDialReadings();
	private TimeZone tz;
	
	
	MeteorPowerFailDetails(){
		for (int i=0; i<14; i++){
			pfhist[i]=firstFailure; // zeros
		}
	}
	
	MeteorPowerFailDetails(char[] c, TimeZone tz){
		this.tz=tz;
		process(c);
	}
	
	MeteorPowerFailDetails(byte[] b, TimeZone tz){
		this.tz=tz;
		process(parseBArraytoCArray(b));
	}
	
	private void process(char[] c){
		String s=new String(c);
		firstFailure=new MeteorCLK(s.substring(0,6).toCharArray(),tz);
		lastRecovery=new MeteorCLK(s.substring(6,12).toCharArray(),tz);
		dialPf=new MeteorReadDialReadings(s.substring(12,204).toCharArray());
		perOut=parseCharToLong(s.substring(204, 208).toCharArray());
		secOut=parseCharToShort(s.substring(208, 210).toCharArray());
		lpfCNT=parseCharToShort(s.substring(210, 212).toCharArray());
		pfhist[0]=firstFailure;
		pfhist[1]=lastRecovery;
		
		for (int i=0; i<14; i++){
			pfhist[i]=new MeteorCLK(s.substring(212+i*6,212+(i+1)*6).toCharArray(),tz);
		}
		pffree[0]=c[296];
		pffree[1]=c[297];
		pffree[2]=c[298];
		pffree[3]=c[299];
		pffree[4]=c[300];
		pffree[5]=c[301];
	}
	
	public void printData(){		
		System.out.println("timPf:          "+firstFailure.toString());
		System.out.println("timPr:          "+lastRecovery.toString());
		System.out.println("dialPf:         "+dialPf.toString());
		System.out.println("perOut:         "+NumberToString(perOut));
		System.out.println("secOut:         "+NumberToString(secOut));
		System.out.println("lpfCNT:         "+NumberToString(lpfCNT));
		for(int i=0; i<14; i++){
			System.out.println("pfhist           :"+pfhist[i].toString());
		}
		System.out.println("pffree:          ");
		for(int i=0; i<6; i++){
			System.out.print(pffree[i]+" ");
		}
		System.out.println();
	}

	/**
	 * @return the firstFailure
	 */
	public MeteorCLK getFirstFailure() {
		return firstFailure;
	}

	/**
	 * @return the lastRecovery
	 */
	public MeteorCLK getLastRecovery() {
		return lastRecovery;
	}

	/**
	 * @return the dialPf
	 */
	public MeteorReadDialReadings getDialPf() {
		return dialPf;
	}

	/**
	 * @return the perOut
	 */
	public long getPerOut() {
		return perOut;
	}

	/**
	 * @return the secOut
	 */
	public short getSecOut() {
		return secOut;
	}

	/**
	 * @return the lpfCNT
	 */
	public short getLpfCNT() {
		return lpfCNT;
	}

	/**
	 * @return the pfhist
	 */
	public MeteorCLK[] getPfhist() {
		return pfhist;
	}

	/**
	 * @return the pffree
	 */
	public char[] getPffree() {
		return pffree;
	}

	byte[] parseToByteArray() {
		return null;
	}

}
