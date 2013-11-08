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
	MedoRequestReadMeterDemands(Calendar start, Calendar stop,
			int intervaltime) {
		// januari-december problem solved, will never happen here.
		Calendar yr2=Calendar.getInstance(start.getTimeZone());
     	yr2.set(start.get(Calendar.YEAR),0,1,0,0,0);
		yr2.set(Calendar.MILLISECOND, 0);
	
		long c2=(long) Math.floor((stop.getTimeInMillis()-yr2.getTimeInMillis())/(1000*intervaltime));
		this.stPeriod  = (int) (Math.floor((start.getTimeInMillis()-yr2.getTimeInMillis())/(1000*intervaltime)));
		this.noPeriods = (short) (c2-stPeriod+1);
		System.out.println(" stperiod: " + this.stPeriod);
		System.out.println(" noperiods: " + this.noPeriods);

	}
//	MedoRequestReadMeterDemands(Calendar start, Calendar stop,
//			int intervaltime) {
//		int startinterval =  (int) (1+Math.floor((start.get(Calendar.HOUR_OF_DAY)*60+start.get(Calendar.MINUTE))/(intervaltime/60)));
//		int stopinterval =  (int) (1+Math.floor((stop.get(Calendar.HOUR_OF_DAY)*60+stop.get(Calendar.MINUTE))/(intervaltime/60)));
//		this.stPeriod  = ((start.get(Calendar.DAY_OF_YEAR)-1)*((24*60*60)/intervaltime)+startinterval);
//		this.noPeriods = (short) (((stop.get(Calendar.DAY_OF_YEAR)-1)*((24*60*60)/intervaltime)+stopinterval)-stPeriod);
//	}

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
