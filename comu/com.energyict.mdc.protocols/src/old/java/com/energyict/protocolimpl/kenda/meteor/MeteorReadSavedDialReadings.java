/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.kenda.meteor;

import java.util.TimeZone;

public class MeteorReadSavedDialReadings extends Parsers{
	/*
	 * Read saved dial readings
	 */
	private MeteorCLK timHr1=new MeteorCLK();
    private MeteorReadDialReadings[] dialShr1=new MeteorReadDialReadings[48];
	private MeteorCLK timHr2=new MeteorCLK();
	private MeteorReadDialReadings[] dialShr2=new MeteorReadDialReadings[48];
	private MeteorCLK timeOp1=new MeteorCLK();
	private MeteorReadDialReadings[] dialSeOp1=new MeteorReadDialReadings[48];
	private MeteorCLK timeOp2=new MeteorCLK();
	private MeteorReadDialReadings[] dialSeOp2=new MeteorReadDialReadings[48];
	private TimeZone tz;
	
	MeteorReadSavedDialReadings(){
		MeteorReadDialReadings m = new MeteorReadDialReadings();
		for (int i=0; i<36; i++){
			dialShr1[i]=m;
			dialShr2[i]=m;
			dialSeOp1[i]=m;
			dialSeOp2[i]=m;
		}
	}	
	MeteorReadSavedDialReadings(char[] c, TimeZone tz){
		this.tz=tz;
		processMeteorReadSavedDialReadings(c);
	}

	MeteorReadSavedDialReadings(byte[] b, TimeZone tz){
		this.tz=tz;
		processMeteorReadSavedDialReadings(parseBArraytoCArray(b));
	}
	private void processMeteorReadSavedDialReadings(char[] c) {
		String s=new String(c);
		timHr1=new MeteorCLK(s.substring(0,6).toCharArray(),tz);
		for(int i=0; i<36; i++){
			dialShr1[i]=new MeteorReadDialReadings(s.substring(6+i*4,10+i*4).toCharArray());
		}
		timHr2=new MeteorCLK(s.substring(150,156).toCharArray(),tz);
		for(int i=0; i<36; i++){
			dialShr2[i]=new MeteorReadDialReadings(s.substring(156+i*4,160+i*4).toCharArray());
		}
		timeOp1=new MeteorCLK(s.substring(300,306).toCharArray(),tz);
		for(int i=0; i<36; i++){
			dialSeOp1[i]=new MeteorReadDialReadings(s.substring(306+i*4,310+i*4).toCharArray());
		}
		timeOp2=new MeteorCLK(s.substring(350,356).toCharArray(),tz);
		for(int i=0; i<36; i++){
			dialSeOp2[i]=new MeteorReadDialReadings(s.substring(356+i*4,360+i*4).toCharArray());
		}
	}
	public void printData(){
		System.out.println("timHr1:          "+  timHr1.toString());
		System.out.print  ("dialShr1         ");
		for (int i=0;i<36; i++){
			System.out.println(dialShr1.toString());
		}
		System.out.println();
		System.out.println("timHr2:          "+  timHr2.toString());
		System.out.print  ("dialShr2         ");
		for (int i=0;i<36; i++){
			System.out.println(dialShr2.toString());
		}
		System.out.println();
		System.out.println("timeOp1:         "+  timeOp1.toString());
		System.out.print  ("dialSeOp1        ");
		for (int i=0;i<36; i++){
			System.out.println(dialSeOp1.toString());
		}
		System.out.println("timeOp2:         "+  timeOp2.toString());
		System.out.print  ("dialSeOp2        ");
		for (int i=0;i<36; i++){
			System.out.println(dialSeOp2.toString());
		}
	}

	/**
	 * @return the timHr1
	 */
	public MeteorCLK getTimHr1() {
		return timHr1;
	}

	/**
	 * @param timHr1 the timHr1 to set
	 */
	public void setTimHr1(MeteorCLK timHr1) {
		this.timHr1 = timHr1;
	}

	/**
	 * @return the dialShr1
	 */
	public MeteorReadDialReadings[] getDialShr1() {
		return dialShr1;
	}

	/**
	 * @param dialShr1 the dialShr1 to set
	 */
	public void setDialShr1(MeteorReadDialReadings[] dialShr1) {
		this.dialShr1 = dialShr1;
	}

	/**
	 * @return the timHr2
	 */
	public MeteorCLK getTimHr2() {
		return timHr2;
	}

	/**
	 * @param timHr2 the timHr2 to set
	 */
	public void setTimHr2(MeteorCLK timHr2) {
		this.timHr2 = timHr2;
	}

	/**
	 * @return the dialShr2
	 */
	public MeteorReadDialReadings[] getDialShr2() {
		return dialShr2;
	}

	/**
	 * @param dialShr2 the dialShr2 to set
	 */
	public void setDialShr2(MeteorReadDialReadings[] dialShr2) {
		this.dialShr2 = dialShr2;
	}

	/**
	 * @return the timeOp1
	 */
	public MeteorCLK getTimeOp1() {
		return timeOp1;
	}

	/**
	 * @param timeOp1 the timeOp1 to set
	 */
	public void setTimeOp1(MeteorCLK timeOp1) {
		this.timeOp1 = timeOp1;
	}

	/**
	 * @return the dialSeOp1
	 */
	public MeteorReadDialReadings[] getDialSeOp1() {
		return dialSeOp1;
	}

	/**
	 * @param dialSeOp1 the dialSeOp1 to set
	 */
	public void setDialSeOp1(MeteorReadDialReadings[] dialSeOp1) {
		this.dialSeOp1 = dialSeOp1;
	}

	/**
	 * @return the timeOp2
	 */
	public MeteorCLK getTimeOp2() {
		return timeOp2;
	}

	/**
	 * @param timeOp2 the timeOp2 to set
	 */
	public void setTimeOp2(MeteorCLK timeOp2) {
		this.timeOp2 = timeOp2;
	}

	/**
	 * @return the dialSeOp2
	 */
	public MeteorReadDialReadings[] getDialSeOp2() {
		return dialSeOp2;
	}

	/**
	 * @param dialSeOp2 the dialSeOp2 to set
	 */
	public void setDialSeOp2(MeteorReadDialReadings[] dialSeOp2) {
		this.dialSeOp2 = dialSeOp2;
	}
	byte[] parseToByteArray() {
		// TODO Auto-generated method stub
		return null;
	}
				
	
}
