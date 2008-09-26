package com.energyict.protocolimpl.cm10;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import com.energyict.cbo.TimeZoneManager;
import com.energyict.protocol.ProfileData;

public class CM10Profile {
	
	private CM10 cm10Protocol;
	
	public CM10Profile(CM10 cm10Protocol) {
        this.cm10Protocol=cm10Protocol;
    }
	
	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
		StatusTable statusTable = cm10Protocol.getStatusTable();
		int mostHistoricDemandCount = statusTable.getMostHistoricDemandCount();
		Calendar cal = Calendar.getInstance(cm10Protocol.getTimeZone());
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date startOfYear = cal.getTime();
		int noHHours = (int) ((to.getTime() - from.getTime()) / (1000 * 1800));
		int toCount = (int) ((to.getTime() - startOfYear.getTime()) / (1000 * 1800));
		int stPeriod = toCount - noHHours + 1;
		if (stPeriod < mostHistoricDemandCount) {
			int lessHHToRead = mostHistoricDemandCount - stPeriod;
			stPeriod = mostHistoricDemandCount;
			noHHours = noHHours - lessHHToRead;
		}
		ProfileData profileData =  getProfileData(stPeriod, noHHours, from);
        return profileData;
	}
	
	
	protected ProfileData getProfileData(int stPeriod, int noHHours, Date from) throws IOException {
		cm10Protocol.getLogger().info("stPeriod: " + stPeriod);
		cm10Protocol.getLogger().info("noHHours: " + noHHours);
		CommandFactory commandFactory = cm10Protocol.getCommandFactory();
		Response response = 
			commandFactory.getReadMeterDemandsCommand(stPeriod, noHHours).invoke();
		MeterDemandsTable meterDemandsTable = new MeterDemandsTable(cm10Protocol);
		meterDemandsTable.parse(response.getData(), from);
		cm10Protocol.getLogger().info(meterDemandsTable.toString());
        return meterDemandsTable.getProfileData();
	}
	
}
	