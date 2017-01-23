/*
 * MT83Profile.java
 *
 */

package com.energyict.protocolimpl.iec1107.iskraemeco.mt83;

import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MeterExceptionInfo;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.base.ProtocolChannel;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.iskraemeco.mt83.vdew.AbstractVDEWRegistry;
import com.energyict.protocolimpl.iec1107.iskraemeco.mt83.vdew.VDEWLogbook;
import com.energyict.protocolimpl.iec1107.iskraemeco.mt83.vdew.VDEWProfile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author  jme
 */
public class MT83Profile extends VDEWProfile {

    private static final int DEBUG=0;

    /** Creates a new instance of MT83Profile */
    public MT83Profile(MeterExceptionInfo meterExceptionInfo,ProtocolLink protocolLink, AbstractVDEWRegistry abstractVDEWRegistry) {
        super(meterExceptionInfo,protocolLink,abstractVDEWRegistry);
    }

    private byte[] getRawProfileData(int profileInterval, int profileId) throws IOException {
    	Calendar fromCalendar = Calendar.getInstance();
    	fromCalendar.setTime(new Date());
    	fromCalendar.add(Calendar.SECOND, (profileInterval * (-2)));

    	Calendar toCalendar = Calendar.getInstance();
    	toCalendar.setTime(new Date());

    	return readRawData(fromCalendar, toCalendar, profileId);
    }

    public ProtocolChannelMap buildChannelMap(int profileInterval, int profileId) throws IOException {
    	int numberOfChannels = 0;
    	int meterProfileInterval = 0;
    	byte[] responseData = getRawProfileData(profileInterval, profileId);
    	String channelMapString = "";

    	meterProfileInterval = Integer.valueOf(getDataBetweenBrackets(responseData, 3)).intValue() * 60;
    	numberOfChannels = Integer.valueOf(getDataBetweenBrackets(responseData, 4)).intValue();

    	if (DEBUG>=1) {
			System.out.println("  Settings profile interval = " + meterProfileInterval);
			System.out.println("  Meter profile interval =    " + meterProfileInterval);
			System.out.println("  Number of channels =        " + numberOfChannels);
		}

		for (int i = 0; i < numberOfChannels; i++) {
    		String channelMapName = getDataBetweenBrackets(responseData, (i*2) + 5);
    		String channelUnit = getDataBetweenBrackets(responseData, (i*2) + 6);
        	if (DEBUG>=1) System.out.println("    CHANNEL[" + i + "]: " + channelMapName + " Unit = " + channelUnit);
    		if (i>0) channelMapString += ":";
    		channelMapString += channelMapName;
    	}

    	return new ProtocolChannelMap(channelMapString);
	}

    private String getDataBetweenBrackets(byte[] responseData, int bracketNumber) {
    	int bp = 0;
    	String returnValue = null;

    	for (int i = 0; i < bracketNumber; i++) {
    		bp = gotoNextOpenBracket(responseData, bp);
        	if (++bp>responseData.length) break;
		}

    	if (bp < responseData.length) {
    		returnValue = new String(ProtocolUtils.getSubArray(responseData, bp, gotoNextCloseBracket(responseData, bp) - 1));
    	}

		return returnValue;
	}

    public ProfileData getProfileData(Calendar fromCalendar, Calendar toCalendar, int nrOfChannels, int profileId, boolean includeEvents, boolean readCurrentDay) throws IOException {
        byte[] data;
        byte[] logbook = null;


        try {
        	data = readRawData(fromCalendar, toCalendar, profileId);
        } catch (IOException e) {
        	data = null;
        	if (e.getMessage().toUpperCase().indexOf("ER(08)") == -1) throw e;
        }

        try {
            if (includeEvents) logbook = new VDEWLogbook(getMeterExceptionInfo(),getProtocolLink()).readRawLogbookData(fromCalendar, toCalendar, 98);
        } catch (IOException e) {
        	logbook = null;
        	if (e.getMessage().toUpperCase().indexOf("ER(08)") == -1) throw e;
        }

        return parse(logbook, data, nrOfChannels, readCurrentDay);
    }

    private ProfileData parse(byte[] logbook, byte[] data, int nrOfChannels, boolean readCurrentDay) throws IOException {
        ProfileData profileData;

        if (data != null) {
        	profileData = buildProfileData(data,nrOfChannels);
        } else {
        	profileData = new ProfileData();
        	for (int i = 0; i < nrOfChannels; i++)
        		profileData.addChannel(new ChannelInfo(i,"mt83_channel_"+i, Unit.get("")));
        }

        if (logbook != null) addLogbookEvents(logbook,profileData);

        // remove current day
        if (!readCurrentDay) {

            // today 00:00
            Calendar calLastInterval = ProtocolUtils.getCalendar(getProtocolLink().getTimeZone());
            calLastInterval.set(Calendar.HOUR_OF_DAY,0);
            calLastInterval.set(Calendar.MINUTE,0);
            calLastInterval.set(Calendar.SECOND,0);
            calLastInterval.set(Calendar.MILLISECOND,0);
            Date dateLastInterval = calLastInterval.getTime();

            List intervalDatas =  profileData.getIntervalDatas();
            Iterator it = intervalDatas.iterator();
            while(it.hasNext()) {
                IntervalData intervalData = (IntervalData)it.next();
                if (intervalData.getEndTime().after(dateLastInterval))
                    it.remove();
            }
        } // if (readCurrentDay)

        return profileData;
    }

    private int gotoNextOpenBracket(byte[] responseData,int i) {
        while(true) {
            if (responseData[i] == '(') break;
            i++;
            if (i>=responseData.length) break;
        }
        return i;
    }

    private int gotoNextCloseBracket(byte[] responseData,int i) {
        while(true) {
            if (responseData[i] == ')') break;
            i++;
            if (i>=responseData.length) break;
        }
        return i;
    }

    private int gotoNextCR(byte[] responseData,int i) {
        while(true) {
            if (responseData[i] == '\r') break;
            i++;
            if (i>=responseData.length) break;
        }
        return i;
    }

    private Calendar parseDateTime(byte[] data,int iOffset) throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(getProtocolLink().getTimeZone());
        calendar.clear();
        calendar.set(calendar.YEAR,(int)(2000+(int)ProtocolUtils.bcd2byte(data,0+iOffset)));
        calendar.set(calendar.MONTH,(int)((int)ProtocolUtils.bcd2byte(data,2+iOffset)-1));
        calendar.set(calendar.DAY_OF_MONTH,(int)ProtocolUtils.bcd2byte(data,4+iOffset));
        calendar.set(calendar.HOUR_OF_DAY,(int)ProtocolUtils.bcd2byte(data,6+iOffset));
        calendar.set(calendar.MINUTE,(int)ProtocolUtils.bcd2byte(data,8+iOffset));
        return calendar;
    }

    private Calendar parseLogbookDateTime(byte[] data,int iOffset) throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(getProtocolLink().getTimeZone());
        calendar.clear();
        calendar.set(calendar.YEAR,(int)(2000+(int)ProtocolUtils.bcd2byte(data,0+iOffset)));
        calendar.set(calendar.MONTH,(int)((int)ProtocolUtils.bcd2byte(data,2+iOffset)-1));
        calendar.set(calendar.DAY_OF_MONTH,(int)ProtocolUtils.bcd2byte(data,4+iOffset));
        calendar.set(calendar.HOUR_OF_DAY,(int)ProtocolUtils.bcd2byte(data,6+iOffset));
        calendar.set(calendar.MINUTE,(int)ProtocolUtils.bcd2byte(data,8+iOffset));
        calendar.set(calendar.SECOND,(int)ProtocolUtils.bcd2byte(data,10+iOffset));
        return calendar;
    }

    private String parseFindString(byte[] data,int iOffset) {
        int start=0,stop=0,i=0;
        if (iOffset >= data.length) return null;
        for (i=0;i<data.length;i++) {
            if (data[i+iOffset]=='(')  start = i;
            if (data[i+iOffset]==')') {
                stop = i;
                break;
            }
        }
        byte[] strparse=new byte[stop-start-1];
        for (i=0;i<(stop-start-1);i++) strparse[i]=data[i+start+1+iOffset];
        return new String(strparse);
    } // private String parseFindString(byte[] data,int iOffset)

    private void verifyChannelMap(ProtocolChannelMap channelMap) throws IOException {
        if (!getProtocolLink().getProtocolChannelMap().hasEqualRegisters(channelMap))
            throw new InvalidPropertyException("verifyChannelMap() profile channelmap registers ("+channelMap.getChannelRegisterMap()+") different from given configuration channelmap registers ("+getProtocolLink().getChannelMap().getChannelRegisterMap()+")");
    }

    ProfileData buildProfileData(byte[] responseData, int nrOfChannels) throws IOException {
        ProfileData profileData;
        Calendar calendar;
        int status=0,eiStatus=0;
        byte bNROfValues=0;
        byte bInterval=0;
        String response;
        int channelMask=0;
        int t;


        if (DEBUG>=1) MT83.sendDebug(responseData.toString(), DEBUG);


        // We suppose that the profile contains nr of channels!!
        try {
            calendar = ProtocolUtils.getCalendar(getProtocolLink().getTimeZone());
            profileData = new ProfileData();
            for (t=0;t<nrOfChannels;t++) {
                ChannelInfo chi = new ChannelInfo(t,"mt83_channel_"+t,Unit.get(""));
                ProtocolChannel channel = getProtocolLink().getProtocolChannelMap().getProtocolChannel(t);
                if (channel.isCumul()) {
                   chi.setCumulativeWrapValue(channel.getWrapAroundValue());
                }
                profileData.addChannel(chi);
            }

            int i=0;
            while(true) {
                if (responseData[i] == 'P') {
                    i+=4; // skip P.0x
                    i=gotoNextOpenBracket(responseData,i);

                    response = parseFindString(responseData,i);
                    calendar = parseDateTime(responseData,i+1);
                    i=gotoNextOpenBracket(responseData,i+1);
                    status = Integer.parseInt(parseFindString(responseData,i),16);
                    eiStatus = MT83CodeMapper.mapInterval2EiStatus(status);

                    MT83.sendDebug("Status: " + status + " EIStatus: " + eiStatus, DEBUG);

                    i=gotoNextOpenBracket(responseData,i+1);
                    bInterval = (byte)Integer.parseInt(parseFindString(responseData,i));
                    i=gotoNextOpenBracket(responseData,i+1);
                    bNROfValues = ProtocolUtils.bcd2nibble(responseData,i+1);
                    if (bNROfValues > nrOfChannels)
                        throw new IOException("buildProfileData() error, mismatch between nrOfChannels (" + nrOfChannels + ") and profile columns (" + bNROfValues + ")! Adjust the ChannelMap!");

                    List channels=new ArrayList();
                    for (t=0;t<bNROfValues;t++) {
                        i=gotoNextOpenBracket(responseData,i+1);
                        // add channel to list
                        channels.add(new ProtocolChannel(parseFindString(responseData,i)));

                        i=gotoNextOpenBracket(responseData,i+1);
                        // set channel unit
                        ((ChannelInfo)profileData.getChannel(t)).setUnit(Unit.get(parseFindString(responseData,i)));
                    }

                    verifyChannelMap(new ProtocolChannelMap(channels));

                    i= gotoNextCR(responseData,i+1);
                }
                else if ((responseData[i] == '\r') || (responseData[i] == '\n')) {
                    i+=1; // skip
                }
                else {
                    // Fill profileData
                    IntervalData intervalData = new IntervalData(new Date(((Calendar)calendar.clone()).getTime().getTime()));
                    for (t=0;t<bNROfValues;t++) {
                        i=gotoNextOpenBracket(responseData,i);
                        intervalData.addValue(new BigDecimal(parseFindString(responseData,i)));
                        i++;
                    }
                    intervalData.addStatus(eiStatus);
                    profileData.addInterval(intervalData);
                    calendar.add(calendar.MINUTE,bInterval);
                    i= gotoNextCR(responseData,i+1);
                    eiStatus=0;
                }
                if (i>=responseData.length) break;
            } // while(true)
        }
        catch(IOException e) {
            throw new IOException("buildProfileData> "+e.getMessage());
        }

        return profileData;
    } // ProfileData buildProfileData(byte[] responseData) throws IOException

    private void addLogbookEvents(byte[] logBook, ProfileData profileData) throws IOException {
        MT83EventType eventtype = null;
        Calendar calendar;
        int status=0;
        try {
            int i=0;
            while(true) {
                i=gotoNextOpenBracket(logBook,i);
                if (i>=logBook.length) break;
                i++;
                calendar = parseLogbookDateTime(logBook,i);
                i=gotoNextOpenBracket(logBook,i);
                i++;
                status = Integer.parseInt(parseFindString(logBook,i-1),16);
                eventtype = (MT83EventType)MT83CodeMapper.LogBookEvent.get(new Integer(status));
                if (eventtype == null) eventtype = new MT83EventType("Unknown event", MeterEvent.OTHER);

                profileData.addEvent(new MeterEvent(new Date(
						((Calendar) calendar.clone()).getTime().getTime()),
						eventtype.getEventCode(),
						status & 0xFFFF,
						eventtype.getMessage()));
            } // while(true) {

            // Check on duplicate event dates/time. CommServer overwrites events with the same timestamp.
            profileData.setMeterEvents(checkOnOverlappingEvents(profileData.getMeterEvents()));

        }
        catch(IOException e) {
            throw new IOException("addLogbookEvents> "+e.getMessage());
        }
    }

    public static List checkOnOverlappingEvents(List meterEvents) {
    	Map eventsMap = new HashMap();
        int size = meterEvents.size();
	    for (int i = 0; i < size; i++) {
	    	MeterEvent event = (MeterEvent) meterEvents.get(i);
	    	Date time = event.getTime();
	    	MeterEvent eventInMap = (MeterEvent) eventsMap.get(time);
	    	while (eventInMap != null) {
	    		time.setTime(time.getTime() + 1000); // add one second
				eventInMap = (MeterEvent) eventsMap.get(time);
	    	}
	    	MeterEvent newMeterEvent=
	    		new MeterEvent(time, event.getEiCode(), event.getProtocolCode(),event.getMessage());
    		eventsMap.put(time, newMeterEvent);
	    }
	    Iterator it = eventsMap.values().iterator();
		List result = new ArrayList();
	    while (it.hasNext())
	        result.add((MeterEvent) it.next());
		return result;
    }

} // MT83Profile

