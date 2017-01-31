/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * AS220Profile.java
 *
 */

package com.energyict.protocolimpl.iec1107.as220;

import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.protocol.api.MeterExceptionInfo;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.protocolimpl.base.DataParser;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.vdew.AbstractVDEWRegistry;
import com.energyict.protocolimpl.iec1107.vdew.VDEWProfile;
import com.energyict.protocolimpl.iec1107.vdew.VDEWProfileHeader;
import com.energyict.protocolimpl.iec1107.vdew.VDEWTimeStamp;

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
 *
 * @author Koen
 * @author fbo
 *
 */
public class AS220Profile extends VDEWProfile {

	private static final int DEBUG = 0;

	private static final int REVERSE_POWER 				= 0x00000800;
	private static final int INPUT_EVENT1 				= 0x00001000;
	private static final int INPUT_EVENT2 				= 0x00008000;
	private static final int CHANGE_OF_IMPULSECONSTANT 	= 0x00010000;
	private static final int TERMINAL_COVER_OPENED 		= 0x00020000;
	private static final int MAIN_COVER_OPENED 			= 0x00040000;
	private static final int WRONG_PASSWORD_USED		= 0x00080000;
	private static final int PLUS_A_STORED				= 0x00100000;
	private static final int L1_MISSING					= 0x00200000;
	private static final int L2_MISSING					= 0x00400000;
	private static final int L3_MISSING					= 0x00800000;


	private AS220ProfileHeader aS220ProfileHeader = null;
	private static final boolean KEEPSTATUS = false;

	private static final int POWERQUALITY_PROFILE = 2;
	private int loadProfileNumber;

	/** Creates a new instance of AS220Profile */
	public AS220Profile(MeterExceptionInfo meterExceptionInfo, ProtocolLink protocolLink, AbstractVDEWRegistry abstractVDEWRegistry) {
		super(meterExceptionInfo,protocolLink,abstractVDEWRegistry,KEEPSTATUS);
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
		while (it.hasNext()) {
			result.add(it.next());
		}
		return result;
	}


	public ProfileData getProfileData(Calendar fromCalendar, Calendar toCalendar, boolean includeEvents, int profileNumber) throws IOException {
		this.loadProfileNumber = profileNumber;

		ProfileData profileData =  doGetProfileData(fromCalendar,toCalendar, this.loadProfileNumber);
		if (includeEvents) {
			List meterEvents = doGetLogBook(fromCalendar,toCalendar);
			for (Iterator iterator = meterEvents.iterator(); iterator.hasNext();) {
				MeterEvent meterEventItem = (MeterEvent) iterator.next();
				int deviceCode = meterEventItem.getProtocolCode();
				int eiServerCode = mapEvent2EiEvent(deviceCode);
				String message = mapEvent2Message(deviceCode);
				MeterEvent newMeterEvent =
					new MeterEvent(
							meterEventItem.getTime(),
							eiServerCode,
							deviceCode,
							message
					);
				meterEvents = checkOnOverlappingEvents(meterEvents);
				profileData.getMeterEvents().add(newMeterEvent);
			}
			profileData.sort();
		}

		profileData.applyEvents(getProtocolLink().getProfileInterval()/60);
		return profileData;
	}


	public AS220ProfileHeader getProfileHeader(int profileNumber) throws IOException {
		this.loadProfileNumber = profileNumber;
		if (this.aS220ProfileHeader == null) {
			this.aS220ProfileHeader = new AS220ProfileHeader(getProtocolLink().getFlagIEC1107Connection(), this.loadProfileNumber);
		}
		return this.aS220ProfileHeader;
	}

	// override the default getProfileHeader() to prevent bypass of the new getProfileHeader(int profileNumber) method
	@Override
	public VDEWProfileHeader getProfileHeader() throws IOException {
		throw new IOException("Internal protocol error. Method getProfileHeader() not supported. No support for loadProfileNumber");
	}

	/* Overrides VDEWProfile#getMeterEvent().
	 *
	 * Difference:
	 * map bit 4 (reset of cumulation) to MeterEvent.MAXIMUM_DEMAND_RESET
	 *
	 * (non-Javadoc)
	 * @see VDEWProfile#getMeterEvent(Date, long, String)
	 */
	protected MeterEvent getMeterEvent(Date date, int logcode, String msg) {
		return new MeterEvent(date,getMeterEvent(logcode),logcode);
	}

	private int getMeterEvent(int logcode){
		if (DEBUG >= 1) {
			System.out.println("getMeterEvent: " + logcode);
		}
		switch(logcode) {
		case FATAL_DEVICE_ERROR:            return MeterEvent.FATAL_ERROR;
		case RUNNING_RESERVE_EXHAUSTED:     return MeterEvent.OTHER;
		case DISTURBED_MEASURE:             return MeterEvent.OTHER;
		case SEASONAL_SWITCHOVER:           return MeterEvent.OTHER;

		case DEVICE_RESET:                  return MeterEvent.MAXIMUM_DEMAND_RESET;
		case DEVICE_CLOCK_SET_INCORRECT:    return MeterEvent.SETCLOCK;
		case POWER_RECOVERY:                return MeterEvent.POWERUP;
		case POWER_FAILURE:                 return MeterEvent.POWERDOWN;

		case VARIABLE_SET:                  return MeterEvent.CONFIGURATIONCHANGE;
		case BEGIN_OF_ERROR:                return MeterEvent.METER_ALARM;
		case END_OF_ERROR:                  return MeterEvent.METER_ALARM;
		case REVERSE_POWER:					return MeterEvent.OTHER;

		case INPUT_EVENT1:					return MeterEvent.OTHER;
		case CLEAR_LOGBOOK:                 return MeterEvent.CLEAR_DATA;
		case CLEAR_LOADPROFILE:             return MeterEvent.CLEAR_DATA;
		case INPUT_EVENT2:					return MeterEvent.OTHER;

		case CHANGE_OF_IMPULSECONSTANT:		return MeterEvent.CONFIGURATIONCHANGE;
		case TERMINAL_COVER_OPENED:			return MeterEvent.OTHER;
		case MAIN_COVER_OPENED:				return MeterEvent.OTHER;
		case WRONG_PASSWORD_USED:			return MeterEvent.OTHER;

		case PLUS_A_STORED:					return MeterEvent.OTHER;
		case L1_MISSING:					return MeterEvent.PHASE_FAILURE;
		case L2_MISSING:					return MeterEvent.PHASE_FAILURE;
		case L3_MISSING:					return MeterEvent.PHASE_FAILURE;

		default:                            return MeterEvent.OTHER;
		}

	}

	private int mapEvent2EiEvent(int deviceEventCode) {
		int eiEventCode = 0;
		for (int t=0; t<24; t++) {
			int logBit = (deviceEventCode & (0x0001<<t));
			if (logBit != 0) {
				eiEventCode |= getMeterEvent(logBit);
			}
		}
		return eiEventCode;
	}

	/* Overrides VDEWProfile#mapStatus2IntervalStateBits().
	 *
	 * Difference:
	 * map bit 2 (Measure value disturbed) to IntervalStateBits.SHORTLONG
	 *
	 * (non-Javadoc)
	 * @see VDEWProfile#mapStatus2IntervalStateBits(int)
	 */
	protected int mapStatus2IntervalStateBits(int status) {
		switch(status) {
		case CLEAR_LOADPROFILE:         return IntervalStateBits.OTHER;
		case CLEAR_LOGBOOK:             return IntervalStateBits.OTHER;
		case END_OF_ERROR:              return IntervalStateBits.OTHER;
		case BEGIN_OF_ERROR:            return IntervalStateBits.OTHER;
		case VARIABLE_SET:              return IntervalStateBits.CONFIGURATIONCHANGE;
		case DEVICE_CLOCK_SET_INCORRECT:return IntervalStateBits.SHORTLONG;
		case SEASONAL_SWITCHOVER:       return IntervalStateBits.SHORTLONG;
		case FATAL_DEVICE_ERROR:        return IntervalStateBits.OTHER;
		case DISTURBED_MEASURE:         return IntervalStateBits.SHORTLONG;
		case POWER_FAILURE:             return IntervalStateBits.POWERDOWN;
		case POWER_RECOVERY:            return IntervalStateBits.POWERUP;
		case DEVICE_RESET:              return IntervalStateBits.OTHER;
		case RUNNING_RESERVE_EXHAUSTED: return IntervalStateBits.OTHER;
		default:                        return IntervalStateBits.OTHER;
		}

	}

	private String mapEvent2Message(int deviceCode) {
		switch(deviceCode) {
		case FATAL_DEVICE_ERROR:            return "Fatal device error";
		case RUNNING_RESERVE_EXHAUSTED:     return "Running reserve exhaused";
		case DISTURBED_MEASURE:             return "Measuring value disturbed";
		case SEASONAL_SWITCHOVER:           return "Seasonal switchover (summer/winter time)";

		case DEVICE_RESET:                  return "Demand reset";
		case DEVICE_CLOCK_SET_INCORRECT:    return "Change of internal clock";
		case POWER_RECOVERY:                return "Power up";
		case POWER_FAILURE:                 return "Power down (3 phase)";

		case VARIABLE_SET:                  return "Parameter changed";
		case BEGIN_OF_ERROR:                return "Error conditions: Fatal or non fatal error";
		case END_OF_ERROR:                  return "End of error conditions";
		case REVERSE_POWER:					return "Reverse power detected";

		case INPUT_EVENT1:					return "Input event 1 detected";
		case CLEAR_LOGBOOK:                 return "Reset of logfile";
		case CLEAR_LOADPROFILE:             return "Reset of loadprofile";
		case INPUT_EVENT2:					return "Input event 2 detected";

		case CHANGE_OF_IMPULSECONSTANT:		return "Change of impulse constant";
		case TERMINAL_COVER_OPENED:			return "Terminal cover was opened";
		case MAIN_COVER_OPENED:				return "Main cover was opened";
		case WRONG_PASSWORD_USED:			return "Wrong password was used";

		case PLUS_A_STORED:					return "+A has been stored";
		case L1_MISSING:					return "Phase L1 is missing";
		case L2_MISSING:					return "Phase L2 is missing";
		case L3_MISSING:					return "Phase L3 is missing";

		default:                            return "Unknown event: " + deviceCode;
		}
	}

	// Override buildProfileData from VDEWProfile to fix duplicate entries in same interval
	// The original function adds the two values. We need to take the average.
	@Override
	protected ProfileData buildProfileData(byte[] responseData) throws IOException {
		ProfileData profileData;
		Calendar calendar=null;
		byte bStatus=0;
		byte bNROfValues=0;
		int profileInterval=0;
		DataParser dp = new DataParser(getProtocolLink().getTimeZone());
		Unit[] units=null;
		boolean buildChannelInfos=false;
		int t;
		int eiCode=0;
		IntervalData intervalDataSave=null;
		boolean partialInterval=false;
		String[] edisCodes=null;

		// We suppose that the profile contains nr of channels!!
		VDEWTimeStamp vts = new VDEWTimeStamp(getProtocolLink().getTimeZone());
		profileData = new ProfileData();

		int i=0;
		while(true) {

			if (responseData[i] == 'P') {
				i+=4; // skip P.01
				i=gotoNextOpenBracket(responseData,i);

				if (dp.parseBetweenBrackets(responseData,i).compareTo("ERROR") == 0) {
					throw new IOException("No entries in object list.");
				}

				//--------------------------------------------------------
				// Data part 1: Read timestamp from loadprofile
				//--------------------------------------------------------
				vts.parse(dp.parseBetweenBrackets(responseData,i));
				calendar = vts.getCalendar();

				//--------------------------------------------------------
				// Data part 2: Read status from loadprofile
				//--------------------------------------------------------
				i=gotoNextOpenBracket(responseData,i+1);
				bStatus =  parseIntervalStatus(responseData, i);

				eiCode = 0;
				for (t=0;t<8;t++) {
					if ((bStatus & (byte)(0x01<<t)) != 0) {
						eiCode |= mapStatus2IntervalStateBits((bStatus&(byte)(0x01<<t))&0xFF);
					}
				}

				// KV 02112005
				if (((bStatus&SEASONAL_SWITCHOVER)==SEASONAL_SWITCHOVER) &&
						(vts.getMode()==VDEWTimeStamp.MODE_SUMMERTIME) &&
						(!getProtocolLink().getTimeZone().inDaylightTime(calendar.getTime())) &&
						((bStatus&DEVICE_CLOCK_SET_INCORRECT)==DEVICE_CLOCK_SET_INCORRECT)) {
					calendar.add(Calendar.MILLISECOND,-1*getProtocolLink().getTimeZone().getDSTSavings());
				}

				if (!ParseUtils.isOnIntervalBoundary(calendar,getProtocolLink().getProfileInterval())) {
					partialInterval=true;
					// roundup to the first interval boundary
					ParseUtils.roundUp2nearestInterval(calendar,getProtocolLink().getProfileInterval());
				}
				else {
					partialInterval=false;
				}

				//--------------------------------------------------------
				// Data part 3: Read profile interval from loadprofile
				//--------------------------------------------------------
				i=gotoNextOpenBracket(responseData,i+1);
				profileInterval = Integer.parseInt(dp.parseBetweenBrackets(responseData,i));
				if ((profileInterval*60) != getProtocolLink().getProfileInterval()) {
					throw new IOException("buildProfileData() error, mismatch between configured profileinterval ("+getProtocolLink().getProfileInterval()+") and meter profileinterval ("+(profileInterval*60)+")!");
				}

				//--------------------------------------------------------
				// Data part 4: Read number of channels from loadprofile
				//--------------------------------------------------------
				i=gotoNextOpenBracket(responseData,i+1);
				// KV 06092005 K&P
				//bNROfValues = ProtocolUtils.bcd2nibble(responseData,i+1);
				bNROfValues = (byte)Integer.parseInt(dp.parseBetweenBrackets(responseData,i));

				if (bNROfValues > getProtocolLink().getNumberOfChannels()) {
					throw new IOException("buildProfileData() error, mismatch between configured nrOfChannels ("+getProtocolLink().getNumberOfChannels()+") and meter profile nrOfChannels ("+bNROfValues+")!");
				}

				//--------------------------------------------------------
				// Data part 5 + 6: Read the channel units
				// and ediscodes from loadprofile
				//--------------------------------------------------------
				units = new Unit[bNROfValues];
				edisCodes = new String[bNROfValues];
				for (t=0;t<bNROfValues;t++) {// skip all obis codes
					i=gotoNextOpenBracket(responseData,i+1);
					edisCodes[t] = dp.parseBetweenBrackets(responseData,i);
					i=gotoNextOpenBracket(responseData,i+1);
					units[t] = Unit.get(dp.parseBetweenBrackets(responseData,i));
				}

				//--------------------------------------------------------
				// On first run, build the channelinfo using the ediscodes
				// and the units in the loadprofile header.
				//--------------------------------------------------------
				// KV 06092005 K&P changes
				if (!buildChannelInfos) {
					int id=0;
					for (t=0;t<bNROfValues;t++) {
						ChannelInfo chi=null;
						if (getProtocolLink().getProtocolChannelMap().isMappedChannels()) {
							int fysical0BasedChannelId = getFysical0BasedChannelId(edisCodes[t]);
							if (getProtocolLink().getProtocolChannelMap().getProtocolChannel(fysical0BasedChannelId).getIntValue(0) != -1) {
								chi = new ChannelInfo(id,getProtocolLink().getProtocolChannelMap().getProtocolChannel(fysical0BasedChannelId).getIntValue(0), "channel_"+id,units[t]);
								id++;
							}
							if (!getProtocolLink().isRequestHeader() && (getProtocolLink().getProtocolChannelMap().getProtocolChannel(fysical0BasedChannelId).isCumul())) {
								chi.setCumulativeWrapValue(getProtocolLink().getProtocolChannelMap().getProtocolChannel(fysical0BasedChannelId).getWrapAroundValue());
							}
						} else {
							chi = new ChannelInfo(t,"channel_"+t,units[t]);
							if (!getProtocolLink().isRequestHeader() && (getProtocolLink().getProtocolChannelMap().getProtocolChannel(t).isCumul())) {
								chi.setCumulativeWrapValue(getProtocolLink().getProtocolChannelMap().getProtocolChannel(t).getWrapAroundValue());
							}
						}

						// KV 06092005 K&P changes
						if (chi != null) {
							profileData.addChannel(chi);
						}
					}
					buildChannelInfos = true;
				}

				// skip data until next CR
				i= gotoNextCR(responseData,i+1);
			} else if ((responseData[i] == '\r') || (responseData[i] == '\n')) {
				i+=1; // skip additional CR's or LF's
			} else {
				//------------------------------------------------------------
				// Data at responseData[i] is channel data:
				// eg (027.012)(000.103)(014.847)(005.123)
				//------------------------------------------------------------

				// Fill profileData
				IntervalData intervalData = new IntervalData(new Date(calendar.getTime().getTime()),eiCode,bStatus);

				if (!KEEPSTATUS) {
					eiCode=0;
				}

				for (t=0;t<bNROfValues;t++) { // skip all obis codes
					i=gotoNextOpenBracket(responseData,i);
					BigDecimal bd = new BigDecimal(dp.parseBetweenBrackets(responseData,i));
					//long lVal = bd.longValue();
					// KV 06092005 K&P changes
					if (getProtocolLink().getProtocolChannelMap().isMappedChannels()) {
						int fysical0BasedChannelId = getFysical0BasedChannelId(edisCodes[t]);
						if (getProtocolLink().getProtocolChannelMap().getProtocolChannel(fysical0BasedChannelId).getIntValue(0) != -1) {
							intervalData.addValue(bd); //new Long(lVal));
						}
					}
					else {
						intervalData.addValue(bd); //new Long(lVal));
					}

					i++;
				}

				if (partialInterval) {

					if (intervalDataSave != null) {
						if (intervalData.getEndTime().getTime() == intervalDataSave.getEndTime().getTime()) {
							if (DEBUG >= 1) {
								System.out.println("KV_DEBUG> partialInterval, add partialInterval to currentInterval");
							}
							intervalData = addIntervalData(intervalDataSave,intervalData, units); // add intervals together to avoid double interval values...
						}
						else {
							if (DEBUG >= 1) {
								System.out.println("KV_DEBUG> partialInterval, save partialInterval to profiledata and assign currentInterval to partialInterval");
							}
							profileData.addInterval(intervalDataSave); // save the partial interval. Timestamp has been adjusted to the next intervalboundary
							intervalDataSave=intervalData;
						}
					}
					else {
						if (DEBUG >= 1) {
							System.out.println("KV_DEBUG> partialInterval, assign currentInterval to partialInterval");
						}
						intervalDataSave=intervalData;
					}
				}
				else {
					// If there was a partial interval within interval x and the next interval has the same timestamp as interval x,
					// then we must add them together!
					// If the next interval's timestamps != timestamp of interval x, save the partial interval as separate entry for interval x.
					if (intervalDataSave != null) {
						if (intervalData.getEndTime().getTime() == intervalDataSave.getEndTime().getTime()) {
							if (DEBUG >= 1) {
								System.out.println("KV_DEBUG> add partialInterval to currentInterval");
							}
							intervalData = addIntervalData(intervalDataSave,intervalData, units); // add intervals together to avoid double interval values...
						}
						else {
							if (DEBUG >= 1) {
								System.out.println("KV_DEBUG> save partialInterval to profiledata");
							}
							profileData.addInterval(intervalDataSave); // save the partial interval. Timestamp has been adjusted to the next intervalboundary
						}
						intervalDataSave = null;
					}

					if (DEBUG >= 1) {
						System.out.println("KV_DEBUG> save currentInterval to profiledata");
					}
					// save the current interval
					profileData.addInterval(intervalData);
				}

				calendar.add(Calendar.MINUTE,profileInterval);

				i= gotoNextCR(responseData,i+1);

			}

			if (i>=responseData.length) {
				break;
			}

		} // while(true)

		return profileData;

	} // private ProfileData buildProfileData(byte[] responseData) throws IOException

	@Override
	protected List buildMeterEvents(byte[] responseData) throws IOException {

		List meterEvents = new ArrayList();
		int t;
		Calendar calendar=null;
		DataParser dp = new DataParser(getProtocolLink().getTimeZone());

		try {
			VDEWTimeStamp vts = new VDEWTimeStamp(getProtocolLink().getTimeZone());
			int i=0;
			while(true) {
				if (responseData[i] == 'P') {
					i+=4; // skip P.01
					i=gotoNextOpenBracket(responseData,i);

					// geen entries in logbook
					if (dp.parseBetweenBrackets(responseData,i).compareTo("ERROR") == 0) {
						return meterEvents;
					}


					// P.98 (ZSTs13)(Status)()(nrofentries)(KZ1)()..(KZz)()(Element1)..(Elementz)
					//         0        1    2      3(eg 2)  4   5    6   7    8           9
					vts.parse(dp.parseBetweenBrackets(responseData,i,0));
					calendar = (Calendar)vts.getCalendar().clone();

					long status = Long.parseLong(dp.parseBetweenBrackets(responseData,i,1),16);

					// KV 02112005
					if (((status&SEASONAL_SWITCHOVER)==SEASONAL_SWITCHOVER) &&
							(vts.getMode()==VDEWTimeStamp.MODE_SUMMERTIME) &&
							(!getProtocolLink().getTimeZone().inDaylightTime(calendar.getTime())) &&
							((status&DEVICE_CLOCK_SET_INCORRECT)==DEVICE_CLOCK_SET_INCORRECT)) {
						calendar.add(Calendar.MILLISECOND,-1*getProtocolLink().getTimeZone().getDSTSavings());
					}


					// See A1500 product description on page 45 for the explenation of the 16 statusbits.
					// Use status to parse the meterevents. Lower statusbyte of meterevents is the same as
					// the intervalstatus byte. So, therefor, we omit the reading of the logbook

					for (t=0;t<24;t++) {
						String msg = null;
						long logBit = (status & (0x0001<<t));
						if (logBit != 0) {
							if ((logBit == DEVICE_CLOCK_SET_INCORRECT) || (logBit == SEASONAL_SWITCHOVER)) {
								String datePart = dp.parseBetweenBrackets(responseData,i,8);
								String timePart = dp.parseBetweenBrackets(responseData,i,9);
								vts.parse(datePart,timePart);
								msg = vts.getCalendar().getTime().toString();
							}
							meterEvents.add(getMeterEvent(new Date(calendar.getTime().getTime()),(int)logBit,msg));
						}
					}


					i= gotoNextCR(responseData,i+1);
				}
				else if ((responseData[i] == '\r') || (responseData[i] == '\n')) {
					i+=1; // skip
				}
				else {
					i= gotoNextCR(responseData,i+1);
				}

				if (i>=responseData.length) {
					break;
				}

			} // while(true)
		}
		catch(IOException e) {
			throw new IOException("buildProfileData> "+e.getMessage());
		}

		return meterEvents;

	} // private List buildMeterEvents(byte[] responseData)

	protected int gotoNextOpenBracket(byte[] responseData,int index) {
		int i = index;
		while(true) {
			if (responseData[i] == '(') {break;}
			i++;
			if (i>=responseData.length) {break;}
		}
		return i;
	}

	protected int gotoNextCR(byte[] responseData, int index) {
		int i = index;
		while(true) {
			if (responseData[i] == '\r') { break;}
			i++;
			if (i>=responseData.length) {break;}
		}
		return i;
	}

	protected int getFysical0BasedChannelId(String edisCode) {
		return Integer.parseInt(edisCode.substring(edisCode.indexOf('-')+1,edisCode.indexOf(':')))-1;
	}

	private IntervalData addIntervalData(IntervalData cumulatedIntervalData,IntervalData currentIntervalData, Unit[] unitlist) {
		int currentCount = currentIntervalData.getValueCount();
		IntervalData intervalData = new IntervalData(currentIntervalData.getEndTime());

		if (this.loadProfileNumber == POWERQUALITY_PROFILE) {
			for (int i=0;i<currentCount;i++) {
				Unit unit =  unitlist[i];
				BigDecimal val1 = (BigDecimal)currentIntervalData.get(i);
				BigDecimal val2 = (BigDecimal)cumulatedIntervalData.get(i);

				if ((unit != null) && (unit.isVolumeUnit())) {
					intervalData.addValue(val1.add(val2));
				} else {
					intervalData.addValue((val1.add(val2)).divide(new BigDecimal(2), BigDecimal.ROUND_HALF_UP));
				}
			}
		} else {
			int i;
			for (i=0;i<currentCount;i++) {
				BigDecimal val1 = (BigDecimal)currentIntervalData.get(i);
				BigDecimal val2 = (BigDecimal)cumulatedIntervalData.get(i);
				intervalData.addValue(val1.add(val2));
			}
			return intervalData;
		}

		return intervalData;

	}
}
