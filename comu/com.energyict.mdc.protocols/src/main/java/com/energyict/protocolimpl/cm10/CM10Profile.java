/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.cm10;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CM10Profile {

	private CM10 cm10Protocol;

	public CM10Profile(CM10 cm10Protocol) {
        this.cm10Protocol=cm10Protocol;
    }

	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
		//cm10Protocol.getLogger().info("from: " + from);
		from = roundDown2nearestInterval(from);
		//Date dateOfMostHistoricDemandValue = getDateOfMostHistoricDemandValue();
		//cm10Protocol.getLogger().info("dateOfMostHistoricDemandValue: " + dateOfMostHistoricDemandValue);
		//cm10Protocol.getLogger().info("from rounded down: " + from);
		//cm10Protocol.getLogger().info("to: " + to);
		//if (from.before(dateOfMostHistoricDemandValue))
			//from = dateOfMostHistoricDemandValue;
		Calendar cal = Calendar.getInstance(cm10Protocol.getTimeZone());
		cal.setTime(from);
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date startOfYear = cal.getTime();
		int noHHours = (int) ((to.getTime() - from.getTime()) / (1000 * 1800));
		int stPeriod = (int) ((from.getTime() - startOfYear.getTime()) / (1000 * 1800)) + 1;
		ProfileData profileData = new ProfileData();
		addChannelInfos(profileData);
		if (includeEvents)
			addEvents(profileData);
		if (noHHours == 0)
			return profileData;
		addChannelData(profileData, stPeriod, noHHours, from);
        return profileData;
	}

	protected void addEvents(ProfileData profileData) throws IOException {
		PowerFailDetailsTable powerFailDetailsTable = cm10Protocol.getPowerFailDetailsTable();
		List powerEvents = powerFailDetailsTable.getEvents();
		int size = powerEvents.size();
		for (int i = 0; i < size; i++) {
			PowerFailEntry entry = (PowerFailEntry) powerEvents.get(i);
			profileData.addEvent(new MeterEvent(entry.getStartTime(),MeterEvent.POWERDOWN));
			//cm10Protocol.getLogger().info("POWERDOWN " + entry.getStartTime());
			profileData.addEvent(new MeterEvent(entry.getEndTime(),MeterEvent.POWERUP));
			//cm10Protocol.getLogger().info("POWERUP " + entry.getEndTime());
		}
		//add battery low event
		StatusTable statusTable = cm10Protocol.getStatusTable();
		if(statusTable.isBatteryLow()) {
			profileData.addEvent(new MeterEvent(cm10Protocol.getTime(),MeterEvent.OTHER,"BATTERY LOW"));
		}
	}


	protected Date roundDown2nearestInterval(Date from) throws IOException {
		Calendar cal = Calendar.getInstance(cm10Protocol.getTimeZone());
		cal.setTime(from);
        int rest = (int)(cal.getTime().getTime()/1000) % cm10Protocol.getProfileInterval();
        if (rest > 0)
            cal.add(Calendar.SECOND,(-1)*rest);
        return cal.getTime();
    }

	protected Date getDateOfMostHistoricDemandValue() throws IOException {
		StatusTable statusTable = cm10Protocol.getStatusTable();
		int mostHistoricDemandCount = statusTable.getMostHistoricDemandCount();
		int intervalInMinutes = cm10Protocol.getProfileInterval() / 60;
		Calendar cal = Calendar.getInstance(cm10Protocol.getTimeZone());
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.MINUTE, (mostHistoricDemandCount - 1) * intervalInMinutes);// - 1 because start interval count
		if (cal.getTime().after(new Date())) {
			cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) - 1);
			return cal.getTime();
		}
		else
			return cal.getTime();
	}



	//Demands: This is the fundamental data required by DCs.
	//Each demand value stored in the outstation is simply a count of pulses
	//received on that channel over a demand period. To derive actual energy you
	//multiply by the UPP factor - as you rightly say. This factor, although stored
	//in a CM-32 is not readable using CM10 protocol. It has been normal for the DC
	//system to maintain a data base of mulipliers for each channel, for each outstation.

	protected void addChannelInfos(ProfileData profileData) throws IOException {
		int numberOfChannels = cm10Protocol.getNumberOfChannels();
		FullPersonalityTable fullPersonalityTable = cm10Protocol.getFullPersonalityTable();
		int[] multipliers = fullPersonalityTable.getMultipliers();
		for (int i = 0; i < numberOfChannels; i++) {
			ChannelInfo channelInfo =
				new ChannelInfo(
						i, "CM10_"+(i+1), Unit.get(BaseUnit.UNITLESS), 0, i, BigDecimal.ONE); // multipliers[i]
			profileData.addChannel(channelInfo);
		}
	}

	//split up the request so we can acknowledge each block the meter sends to prevent
	//the meter sending for a long time before the need to acknowledge
	protected void addChannelData(ProfileData profileData, int stPeriod, int noHHours, Date from) throws IOException {
		//cm10Protocol.getLogger().info("stPeriod: " + stPeriod);
		//cm10Protocol.getLogger().info("noHHours: " + noHHours);
		int numberOfChannels = cm10Protocol.getNumberOfChannels();

        /*
        It's not exactly clear whats the cause, but the meter does NOT respond when we request a value with reference around $0B$60
        We could fix this temporary by reading all the data at once. This reduces the risk of using a 'bad' reference timestamp.
         */
        int numberOfHHoursToRequestPerBlock = noHHours;
        //int numberOfHHoursToRequestPerBlock = (255 - 10) / numberOfChannels / 2; //(2 bytes per value)

        int noHHoursRequested = 0;
		int myStPeriod = stPeriod;
		int myNoHHours = numberOfHHoursToRequestPerBlock;
		CommandFactory commandFactory = cm10Protocol.getCommandFactory();
		int noHHLeft = noHHours - noHHoursRequested;
		ByteArray data = new ByteArray();
		while (noHHLeft > 0) {
			if (noHHLeft > numberOfHHoursToRequestPerBlock)
				myNoHHours = numberOfHHoursToRequestPerBlock;
			else
				myNoHHours = noHHLeft;

			//cm10Protocol.getLogger().info("getting profiledata: myStPeriod =  " + myStPeriod + ", myNoHHours = " + myNoHHours);
			Response response =
				commandFactory.getReadMeterDemandsCommand(myStPeriod, myNoHHours).invoke();
			data.add(response.getData());

			myStPeriod = myStPeriod + myNoHHours;
			noHHoursRequested = noHHoursRequested + myNoHHours;
			noHHLeft = noHHours - noHHoursRequested;
		}


		MeterDemandsTable meterDemandsTable = new MeterDemandsTable(cm10Protocol);
		meterDemandsTable.parse(data.getBytes(), profileData, from);
		//cm10Protocol.getLogger().info(meterDemandsTable.toString());



	}

}
