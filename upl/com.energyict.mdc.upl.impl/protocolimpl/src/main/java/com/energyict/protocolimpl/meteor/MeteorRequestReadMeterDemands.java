package com.energyict.protocolimpl.meteor;

public class MeteorRequestReadMeterDemands extends Parsers {
	private long stPeriod=0;
	private short noPeriods=0;
	
	MeteorRequestReadMeterDemands(){}
	
	MeteorRequestReadMeterDemands(int stperiod, short noperiods){
		this.stPeriod=stperiod;
		this.noPeriods=noperiods;
	}
	MeteorRequestReadMeterDemands(char[] c){
		processRequest(c);
	}
	MeteorRequestReadMeterDemands(byte[] b){
		processRequest(parseBArraytoCArray(b));	
	}

	private void processRequest(char[] c) {
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
