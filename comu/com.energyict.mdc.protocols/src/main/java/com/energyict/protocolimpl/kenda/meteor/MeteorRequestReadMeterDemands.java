package com.energyict.protocolimpl.kenda.meteor;

import java.util.Calendar;

public class MeteorRequestReadMeterDemands extends Parsers {
	private long stPeriod=0;
	private short noPeriods=0;
	
	MeteorRequestReadMeterDemands(){}
	
	MeteorRequestReadMeterDemands(Calendar cal, Calendar cal2, int intervaltime){
		long c2=cal2.getTimeInMillis();
		this.stPeriod  = (long) (Math.floor(cal.getTimeInMillis()/(1000*intervaltime))*intervaltime);
		this.noPeriods = (short) (Math.floor(((c2-stPeriod*1000)/(1000*intervaltime)))+1);
	}
	MeteorRequestReadMeterDemands(char[] c){
		process(c);
	}
	MeteorRequestReadMeterDemands(byte[] b){
		process(parseBArraytoCArray(b));	
	}

	private void process(char[] c) {
		String s = new String(c);
		stPeriod=parseCharToInt(s.substring(0, 4).toCharArray());
		noPeriods=parseCharToShort(s.substring(4, 6).toCharArray());
	}

	public byte[] parseToByteArray(){
		String serial="";
		serial+=new String(parseLongToChar(stPeriod));
		serial+=new String(parseShortToChar(noPeriods));
		return parseCArraytoBArray(serial.toCharArray());
	}	
	/**
	 * @return the stPeriod
	 */
	public long getStperiod() {
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
