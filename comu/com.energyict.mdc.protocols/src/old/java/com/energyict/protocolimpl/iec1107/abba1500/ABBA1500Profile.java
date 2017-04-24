/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * A1500Profile.java
 *
 * Created on 23 december 2004, 16:23
 */

package com.energyict.protocolimpl.iec1107.abba1500;

import com.energyict.mdc.protocol.api.MeterExceptionInfo;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.vdew.AbstractVDEWRegistry;
import com.energyict.protocolimpl.iec1107.vdew.VDEWProfile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author  Koen
 * Changes:
 * KV 20012005 Initial version
 */
public class ABBA1500Profile extends VDEWProfile {

    private static final int DEBUG=0;
    private String fwVersion = "";

    /** Creates a new instance of ABBA1500Profile */
    public ABBA1500Profile(MeterExceptionInfo meterExceptionInfo, ProtocolLink protocolLink, AbstractVDEWRegistry abstractVDEWRegistry) {
        super(meterExceptionInfo,protocolLink,abstractVDEWRegistry,false);
    }


    public ProfileData getProfileData(Date lastReading,boolean includeEvents) throws IOException {
        return  getProfileData(lastReading, ProtocolUtils.getCalendar(getProtocolLink().getTimeZone()).getTime(), includeEvents);
    }

    public ProfileData getProfileData(Date fromReading, Date toReading, boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(getProtocolLink().getTimeZone());
        fromCalendar.setTime(fromReading);
        Calendar toCalendar = ProtocolUtils.getCleanCalendar(getProtocolLink().getTimeZone());
        toCalendar.setTime(toReading);

        int readMode = 6;
        ProfileData profileData = new ProfileData();
        if (Float.parseFloat(getFirmwareVersion()) < (float) 3.02) {
            readMode = 5;
        }

        if ((getMaxNrOfMilliSecProfileData() != 0) && (toCalendar.getTimeInMillis() - fromCalendar.getTimeInMillis()) > getMaxNrOfMilliSecProfileData()) {
            Calendar fromWorkingCalendar = (Calendar) fromCalendar.clone();
            Calendar toWorkingCalendar = (Calendar) toCalendar.clone();

            ArrayList<long[]> periods = splitUpInterval(fromCalendar, toCalendar);
            getProtocolLink().getLogger().log(Level.INFO, "Reading of profile data for a period larger than the configured maximum period (" + (getMaxNrOfMilliSecProfileData() / (1000 * 3600 * 24)) + " days). The read wll be split up in " + periods.size() + " separate readings.");
            Iterator<long[]> iterator = periods.iterator();
            while (iterator.hasNext()) {
                long[] period = iterator.next();
                fromWorkingCalendar.setTimeInMillis(period[0]);
                toWorkingCalendar.setTimeInMillis(period[1]);
                getProtocolLink().getLogger().log(Level.FINEST,"Retrieving profile data for interval: " + fromWorkingCalendar.getTime() + " to " + toWorkingCalendar.getTime());
                ProfileData profileDataPart = doGetProfileData(fromWorkingCalendar, toWorkingCalendar, 1, readMode);
                profileData = mergeProfileData(profileData, profileDataPart);

                if (includeEvents) {
                    List meterEvents = doGetLogBook(fromWorkingCalendar, toWorkingCalendar);
                    profileData.getMeterEvents().addAll(meterEvents);
                }
            }
        } else {
            profileData = doGetProfileData(fromCalendar, toCalendar, 1, readMode);
            if (includeEvents) {
                List meterEvents = doGetLogBook(fromCalendar, toCalendar);
                profileData.getMeterEvents().addAll(meterEvents);
            }
        }

        profileData.sort();
        removeDuplicateIntervals(profileData.getIntervalDatas());
        profileData.applyEvents(getProtocolLink().getProfileInterval() / 60);

        // JME: filter flags for firmware 3.02
        if (getFirmwareVersion().equalsIgnoreCase("3.02")) {
            profileData = filterDisturbedIntervalFlag(profileData);
        }
        return profileData;
    }

    private ArrayList<long[]> splitUpInterval(Calendar fromCal, Calendar toCal) {
        long fromWorkingCalTime = fromCal.getTimeInMillis();
        long toWorkingCalTime = fromWorkingCalTime + getMaxNrOfMilliSecProfileData();

        ArrayList<long[]> periodListing = new ArrayList<long[]>();
        while (toWorkingCalTime  <= toCal.getTimeInMillis()) {
            long[] period = new long[]{fromWorkingCalTime, toWorkingCalTime};
            periodListing.add(period);

            fromWorkingCalTime =  fromWorkingCalTime + getMaxNrOfMilliSecProfileData();
            toWorkingCalTime = toWorkingCalTime + getMaxNrOfMilliSecProfileData();
        }

        if ((toCal.getTimeInMillis() - fromWorkingCalTime) > 0) {
            long[] lastPeriod = {fromWorkingCalTime, toCal.getTimeInMillis()};
            periodListing.add(lastPeriod);
        }
        return periodListing;
    }

    // Merge the 2 given ProfileData objects.
    private ProfileData mergeProfileData(ProfileData profileData, ProfileData profileDataPart) throws IOException {
        /** If profileData is empty, set the ChannelInfo of the profileDataPart.
         else use the existing ChannelInfo of profileData, just add the IntervalData (but do a check to be sure profilData and profileDataPart have the same ChannelInfo set). **/
        if (profileData.getChannelInfos().size() == 0) {
            profileData.getChannelInfos().addAll(profileDataPart.getChannelInfos());
        } else {
            for (int i = 0; i < profileData.getChannelInfos().size(); i++) {
                ChannelInfo info = (ChannelInfo) profileData.getChannelInfos().get(i);
                ChannelInfo partInfo = (ChannelInfo) profileDataPart.getChannelInfos().get(i);

                if (!info.getName().equals(partInfo.getName()) ||
                        !info.getUnit().equals(partInfo.getUnit()) ||
                        info.getChannelId() != partInfo.getChannelId() ||
                        !info.getMultiplier().equals(partInfo.getMultiplier())) {
                    throw new IOException("ChannelInfo of profilePart doesn't match the ChannelInfo of the previous retrieved profile data.");

                }
            }
        }

        profileData.getIntervalDatas().addAll(profileDataPart.getIntervalDatas());
        return profileData;
    }

    /**
     * @param intervals The list of IntervalData to check for duplicates. If found, duplicate intervals will be removed.
     * @return
     */
    private List<IntervalData> removeDuplicateIntervals(List<IntervalData> intervals) {
        List<IntervalData> returnList = new ArrayList<IntervalData>();
        Collections.sort(intervals);

        for (int i = 0; i < (intervals.size() - 1); i++) {
            IntervalData leftInterval = intervals.get(i);
            IntervalData rightInterval = intervals.get(i + 1);

            if (!leftInterval.getEndTime().equals(rightInterval.getEndTime()) || leftInterval.getEiStatus() != rightInterval.getEiStatus() ||
                    leftInterval.getProtocolStatus() != rightInterval.getProtocolStatus() || leftInterval.getTariffCode() != rightInterval.getTariffCode() ||
                    leftInterval.getIntervalValues().size() != rightInterval.getIntervalValues().size()) {
                returnList.add(leftInterval);
            }
        }
        returnList.add(intervals.get(intervals.size() - 1));
        return returnList;
    }


    // Returns the maximum time period (in ms) that can be read out as one block.
    private long getMaxNrOfMilliSecProfileData() {
        return (((ABBA1500) getMeterExceptionInfo()).getMaxNrOfDaysProfileData() * 24 * 3600 * 1000);
    }

	public void setFirmwareVersion(String firmwareVersion) {
		this.fwVersion  = firmwareVersion;
	}

	private String getFirmwareVersion() {
		return this.fwVersion;
	}

	private ProfileData filterDisturbedIntervalFlag(ProfileData profileData) {
		ProfileData pd = profileData;
		int numberOfIntervals = pd.getNumberOfIntervals();
		int numberOfChannels = pd.getNumberOfChannels();

		for (int i = 0; i < numberOfIntervals; i++) {

			//General statusFlags for all channels
			int statusFlags = pd.getIntervalData(i).getEiStatus();
			if (isEiStatusFlagSet(statusFlags, IntervalData.CORRUPTED) && (isEiStatusFlagSet(statusFlags, IntervalData.POWERUP) || isEiStatusFlagSet(statusFlags, IntervalData.POWERDOWN))) {
				pd.getIntervalData(i).setEiStatus(statusFlags & (~IntervalData.CORRUPTED));
			}

			//statusFlags for all specific channel
			for (int j = 0; j < numberOfChannels; j++) {
				statusFlags = pd.getIntervalData(i).getEiStatus(j);
				if (isEiStatusFlagSet(statusFlags, IntervalData.CORRUPTED) && (isEiStatusFlagSet(statusFlags, IntervalData.POWERUP) || isEiStatusFlagSet(statusFlags, IntervalData.POWERDOWN))) {
					pd.getIntervalData(i).setEiStatus(j, statusFlags & (~IntervalData.CORRUPTED));
				}
			}

		}

		return profileData;
	}

	private boolean isEiStatusFlagSet(int statusFlags, int eiFlag) {
		return ((statusFlags & eiFlag) == eiFlag);
	}

} // ABBA1500Profile

