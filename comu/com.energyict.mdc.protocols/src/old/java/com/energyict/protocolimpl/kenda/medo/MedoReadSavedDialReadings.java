/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.kenda.medo;

import java.util.TimeZone;


public class MedoReadSavedDialReadings extends Parsers{
	/*
	 * Read saved dial readings
	 */
	private MedoCLK timHr1=new MedoCLK();
	private MedoReadDialReadings[] dialShr1=new MedoReadDialReadings[36];
	private MedoCLK timHr2=new MedoCLK();
	private MedoReadDialReadings[] dialShr2=new MedoReadDialReadings[36];
	private MedoCLK timeOp1=new MedoCLK();
	private MedoReadDialReadings[] dialSeOp1=new MedoReadDialReadings[36];
	private MedoCLK timeOp2=new MedoCLK();
	private MedoReadDialReadings[] dialSeOp2=new MedoReadDialReadings[36];
	private TimeZone tz;
	
	
	MedoReadSavedDialReadings(){
		MedoReadDialReadings m = new MedoReadDialReadings();
		for (int i=0; i<36; i++){
			dialShr1[i]=m;
			dialShr2[i]=m;
			dialSeOp1[i]=m;
			dialSeOp2[i]=m;
		}
	}	
	MedoReadSavedDialReadings(char[] c, TimeZone tz){
		this.tz=tz;
		processMedoReadSavedDialReadings(c);
	}

	MedoReadSavedDialReadings(byte[] b, TimeZone tz){
		this.tz=tz;
		processMedoReadSavedDialReadings(parseBArraytoCArray(b));
	}
	private void processMedoReadSavedDialReadings(char[] c) {
		String s=new String(c);
		timHr1=new MedoCLK(s.substring(0,6).toCharArray(),tz);
		for(int i=0; i<36; i++){
			dialShr1[i]=new MedoReadDialReadings(s.substring(6+i*4,10+i*4).toCharArray());
		}
		timHr2=new MedoCLK(s.substring(150,156).toCharArray(),tz);
		for(int i=0; i<36; i++){
			dialShr2[i]=new MedoReadDialReadings(s.substring(156+i*4,160+i*4).toCharArray());
		}
		timeOp1=new MedoCLK(s.substring(300,306).toCharArray(),tz);
		for(int i=0; i<36; i++){
			dialSeOp1[i]=new MedoReadDialReadings(s.substring(306+i*4,310+i*4).toCharArray());
		}
		timeOp2=new MedoCLK(s.substring(350,356).toCharArray(),tz);
		for(int i=0; i<36; i++){
			dialSeOp2[i]=new MedoReadDialReadings(s.substring(356+i*4,360+i*4).toCharArray());
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
	public MedoCLK getTimHr1() {
		return timHr1;
	}

	/**
	 * @param timHr1 the timHr1 to set
	 */
	public void setTimHr1(MedoCLK timHr1) {
		this.timHr1 = timHr1;
	}

	/**
	 * @return the dialShr1
	 */
	public MedoReadDialReadings[] getDialShr1() {
		return dialShr1;
	}

	/**
	 * @param dialShr1 the dialShr1 to set
	 */
	public void setDialShr1(MedoReadDialReadings[] dialShr1) {
		this.dialShr1 = dialShr1;
	}

	/**
	 * @return the timHr2
	 */
	public MedoCLK getTimHr2() {
		return timHr2;
	}

	/**
	 * @param timHr2 the timHr2 to set
	 */
	public void setTimHr2(MedoCLK timHr2) {
		this.timHr2 = timHr2;
	}

	/**
	 * @return the dialShr2
	 */
	public MedoReadDialReadings[] getDialShr2() {
		return dialShr2;
	}

	/**
	 * @param dialShr2 the dialShr2 to set
	 */
	public void setDialShr2(MedoReadDialReadings[] dialShr2) {
		this.dialShr2 = dialShr2;
	}

	/**
	 * @return the timeOp1
	 */
	public MedoCLK getTimeOp1() {
		return timeOp1;
	}

	/**
	 * @param timeOp1 the timeOp1 to set
	 */
	public void setTimeOp1(MedoCLK timeOp1) {
		this.timeOp1 = timeOp1;
	}

	/**
	 * @return the dialSeOp1
	 */
	public MedoReadDialReadings[] getDialSeOp1() {
		return dialSeOp1;
	}

	/**
	 * @param dialSeOp1 the dialSeOp1 to set
	 */
	public void setDialSeOp1(MedoReadDialReadings[] dialSeOp1) {
		this.dialSeOp1 = dialSeOp1;
	}

	/**
	 * @return the timeOp2
	 */
	public MedoCLK getTimeOp2() {
		return timeOp2;
	}

	/**
	 * @param timeOp2 the timeOp2 to set
	 */
	public void setTimeOp2(MedoCLK timeOp2) {
		this.timeOp2 = timeOp2;
	}

	/**
	 * @return the dialSeOp2
	 */
	public MedoReadDialReadings[] getDialSeOp2() {
		return dialSeOp2;
	}

	/**
	 * @param dialSeOp2 the dialSeOp2 to set
	 */
	public void setDialSeOp2(MedoReadDialReadings[] dialSeOp2) {
		this.dialSeOp2 = dialSeOp2;
	}
	byte[] parseToByteArray() {
		// TODO Auto-generated method stub
		return null;
	}
				
	
}
