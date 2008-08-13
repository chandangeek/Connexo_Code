package com.energyict.protocolimpl.kenda.medo;

import java.util.Calendar;


public class MedoRequestReadMeterDemands extends Parsers {
	private int stPeriod=0;
	private short noPeriods=0;
	
	MedoRequestReadMeterDemands(){}
	
	MedoRequestReadMeterDemands(int stperiod, short noperiods){
		this.stPeriod=stperiod;
		this.noPeriods=noperiods;
	}
	MedoRequestReadMeterDemands(char[] c){
		processRequest(c);
	}
	MedoRequestReadMeterDemands(byte[] b){
		processRequest(parseBArraytoCArray(b));	
	}

	public MedoRequestReadMeterDemands(Calendar start, Calendar stop,
			int intervaltime) {
		// TODO Auto-generated constructor stub
	}

	private void processRequest(char[] c) {
		String s = new String(c);
		stPeriod=parseCharToInt(s.substring(0, 4).toCharArray());
		noPeriods=parseCharToShort(s.substring(4, 6).toCharArray());
	}

	public byte[] parseToByteArray(){
		String serial="";
		serial+=new String(parseIntToChar(stPeriod));
		serial+=new String(parseShortToChar(noPeriods));
		return parseCArraytoBArray(serial.toCharArray());
	}	
	/**
	 * @return the stPeriod
	 */
	public int getStperiod() {
		return stPeriod;
	}
	/**
	 * @param stperiod the stPeriod to set
	 */
	public void setStperiod(int stPeriod) {
		this.stPeriod = stPeriod;
	}
	/**
	 * @return the noPeriods
	 */
	public short getNoperiods() {
		return noPeriods;
	}
	/**
	 * @param noPeriods the noPeriods to set
	 */
	public void setNoperiods(short noPeriods) {
		this.noPeriods = noPeriods;
	}
	
	
}
