/*
 * Utils.java
 *
 * Created on 7 november 2006, 13:52
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel.logicalid;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * 
 * @author Koen
 */
public class Utils {

	/** Creates a new instance of Utils */
	private Utils() {
	}

	public static Date parseTimeStamp(long secondsSince01012000, TimeZone timeZone) {
		Calendar cal = ProtocolUtils.getCleanCalendar(timeZone);
		cal.set(Calendar.YEAR, 2000);
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.SECOND, (int) secondsSince01012000);
		return cal.getTime();
	}

}
