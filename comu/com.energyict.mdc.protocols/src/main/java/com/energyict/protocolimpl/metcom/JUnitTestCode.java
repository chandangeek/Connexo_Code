package com.energyict.protocolimpl.metcom;

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

public class JUnitTestCode {
	
	private static int timeSetMethod = -1;
	private static int delay = 0;

	public static Calendar getCalendar() {
		Calendar cal = Calendar.getInstance();
		return cal;
	}

	public static Date getMeterTime() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, delay);
		return cal.getTime();
	}
	
	public static Logger getLogger(){
		Logger log= Logger.global;
		return log;
	}
	
	public static void sendRequest(int i){
		timeSetMethod = i;
	}
	
	public static int checkMethod(){
		return timeSetMethod;
	}
	
	public static void setDelay(int minutes){
		delay = minutes;
	}

	public static void sendInit() {
		// do Nothing
	}

	public static void waitRoutine() {
		// do nothing, just wait an extra routine ...
	}

}
