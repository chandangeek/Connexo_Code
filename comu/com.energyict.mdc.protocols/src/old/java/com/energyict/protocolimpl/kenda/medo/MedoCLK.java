package com.energyict.protocolimpl.kenda.medo;

import java.util.Calendar;
import java.util.TimeZone;

public class MedoCLK extends Parsers{
	
	/*
	 * This clock object saves the time and date (TAD) in char format, 
	 * both char and byte can be entered in the methods and constructor
	 */
	private char[] TAD=new char[6];
	private TimeZone tz;
	private Calendar calendar=Calendar.getInstance();
	
	MedoCLK(){}
	
	MedoCLK(char[] cs, TimeZone tz){
		this.tz=tz;
		calendar.setTimeZone(tz);
		TAD=cs;
		process();
	}
	
	MedoCLK(Calendar calendar){
		this.tz=calendar.getTimeZone();
		this.calendar=calendar;
		changeTAD();
	}
	
	MedoCLK(byte[] rawdata, TimeZone tz) {
		this.tz=tz;
		calendar.setTimeZone(tz);
		TAD=parseBArraytoCArray(rawdata);
		process();
	}
	private void process(){
		calendar.set((int) (TAD[5])+2000,(int) (TAD[4])-1,
				(int) (TAD[3]),(int) (TAD[2]),
				(int) (TAD[1]),(int) (TAD[0]));
	}
	private void changeTAD(){
		TAD[5]=(char) (calendar.get(Calendar.YEAR)%100); // only lowest two numbers
		TAD[4]=(char) (calendar.get(Calendar.MONTH)+1);
		TAD[3]=(char) (calendar.get(Calendar.DAY_OF_MONTH));
		TAD[2]=(char) (calendar.get(Calendar.HOUR_OF_DAY));
		TAD[1]=(char) (calendar.get(Calendar.MINUTE));
		TAD[0]=(char) (calendar.get(Calendar.SECOND));
	}
	public void setTAD(char sec, char min, char hr, char day, char mth, char yr){
		setTime(sec,min,hr);
		setDate(day,mth,yr);
	}
	public void setTAD(byte sec, byte min, byte hr, byte day, byte mth, byte yr){
		setTime(sec,min,hr);
		setDate(day,mth,yr);
	}
	public void setTAD(Calendar calendar){
		this.calendar=calendar;
		changeTAD();
	}
	public void setTime(char sec, char min, char hr){
		TAD[0]=sec;
		TAD[1]=min;
		TAD[2]=hr;
		process();
	}
	public char[] getTime(){
		char[] c={TAD[0], TAD[1], TAD[2]};
		return c;
	}
	public void setTime(byte sec, byte min, byte hr){
		TAD[0]=(char) sec;
		TAD[1]=(char) min;
		TAD[2]=(char) hr;
		process();
	}
	public void setDate( char day, char mth, char yr){
		TAD[3]=day;
		TAD[4]=mth;
		TAD[5]=yr;
		process();
	}
	public char[] getDate(){
		char[] c={TAD[3], TAD[4], TAD[5]};
		return c;
	}
	public void setDate( byte day, byte mth, byte yr){
		TAD[3]=(char) day;
		TAD[4]=(char) mth;
		TAD[5]=(char) yr;
		process();
	}
	public char[] getcharArray(){
		return TAD;
	}
	public byte[] getbyteArray(){
		byte[] b=new byte[6];
		for(int i=0; i<6; i++){
			b[i]=(byte) TAD[i];
		}
		return b;
	}
	public boolean checkValidity(){
		boolean valid=true;
		// if day==0 date and hour invalid
		Calendar now=Calendar.getInstance(tz);
		if(TAD[3]==0 & now.getTimeInMillis()>calendar.getTimeInMillis()){
			valid=false;
		}
		return valid;
	}
	public String toString(){
		return("Time: "+(int) TAD[2]+":"+(int) TAD[1]+":"+(int) TAD[0]+ "  Date: "+(int) TAD[3]+"/"+(int) TAD[4]+"/"+(int) TAD[5]);
	}
	/**
	 * @return the tAD
	 */
	public char[] getTAD() {
		return TAD;
	}
	/**
	 * @param tad the tAD to set
	 */
	public void setTAD(char[] tad) {
		TAD = tad;
	}
	/**
	 * @return the calendar
	 */
	public Calendar getCalendar() {
		return calendar;
	}
	/**
	 * @param calendar the calendar to set
	 */
	public void setCalendar(Calendar calendar) {
		this.calendar = calendar;
	}

	byte[] parseToByteArray() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setTz(TimeZone tz) {
		this.tz = tz;
		calendar=Calendar.getInstance(tz);
	}

}
