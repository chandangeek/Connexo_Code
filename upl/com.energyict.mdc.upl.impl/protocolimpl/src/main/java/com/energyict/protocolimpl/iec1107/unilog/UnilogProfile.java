package com.energyict.protocolimpl.iec1107.unilog;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.vdew.AbstractVDEWRegistry;
import com.energyict.protocolimpl.iec1107.vdew.VDEWProfile;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/*
* Structure of the response data
* 
* P.01(0050101230000)(0000)(60)(2)(1:23.0.0)(m3)(2:23.0.0)(m3)
* (00000245.000)(00000255.000)
* P.01(0050102000000)(0000)(60)(2)(1:23.0.0)(m3)(2:23.0.0)(m3)
* (00000245.000)(00000255.000)
* (00000245.000)(00000255.000)
* (00000245.000)(00000255.000)
* (00000245.000)(00000255.000)
* (00000245.000)(00000255.000)
* (00000245.000)(00000255.000)
* (00000245.000)(00000255.000)
* P.01(0050102070000)(0000)(60)(2)(1:23.0.0)(m3)(2:23.0.0)(m3)
* (00000245.000)(00000255.000)
* (00000245.000)(00000255.000)
* (00000245.000)(00000255.000)
* (00000245.000)(00000255.000)
* (00000245.000)(00000255.000)
* (00000245.000)(00000255.000)
* (00000245.000)(00000255.000)
* (00000245.000)(00000255.000)
* 
* Header:
*   (date)(?)(intervalLength)(nmbrChannels)(EDIScode for channel)(Unit for channel)(EDIScode for channel)(Unit for channel)
*
* The protocol sends the hours continuously; unless there is a change in the 
* state flag, then it sends the header again.
 *
 * The state flag in the header is only related to the subsequent interval, not the 
 * other intervals that follow.
*/


/**
 * 
 * @author Koen & fbo
 */
public class UnilogProfile extends VDEWProfile {
    
    private static final Unit[] dataUnits = { 
            Unit.get("m3"), 
            Unit.get("m3"), 
            Unit.get("m3"), 
            Unit.get(BaseUnit.KELVIN, -1), 
            Unit.get("mbar"),
            Unit.get(""),
            Unit.get("") ,
            Unit.get("") ,
            Unit.get("") ,
            Unit.get("")
    };

    
    public UnilogProfile(ProtocolLink protocolLink,
            AbstractVDEWRegistry abstractVDEWRegistry) {

        super(null, protocolLink, abstractVDEWRegistry);
    
    }

    public ProfileData getProfileData(Calendar fromCalendar,
            Calendar toCalendar, int nrOfChannels, int profileId)
            throws IOException {
        
        byte[] data = readRawData(fromCalendar, toCalendar, profileId);
        return parse(data, nrOfChannels);
    
    }

    private ProfileData parse(byte[] data, int nrOfChannels) throws IOException {
        ProfileData profileData = null;
        profileData = buildProfileData(data, nrOfChannels);
        profileData.applyEvents(getProtocolLink().getProfileInterval() / 60);
        return profileData;
    }

    protected int gotoNextOpenBracket(byte[] responseData, int i) {
        while (true) {
            if (responseData[i] == '(') {
				break;
			}
            i++;
            if (i >= responseData.length) {
				break;
			}
        }
        return i;
    }

    protected int gotoNextCR(byte[] responseData, int i) {
        while (true) {
            if (responseData[i] == '\r') {
				break;
			}
            i++;
            if (i >= responseData.length) {
				break;
			}
        }
        return i;
    }

    private Calendar parseDateTime(byte[] data, int offset) throws IOException {
        int dst;
        dst = ProtocolUtils.hex2nibble(data[offset]);
        
        TimeZone t = getProtocolLink().getTimeZone();
        Calendar c = ProtocolUtils.initCalendar(dst == 0x01, t );
        
        
        c.set(Calendar.YEAR, 2000 + (int) ProtocolUtils.bcd2byte( data, 1 + offset));
        c.set(Calendar.MONTH, (int) ProtocolUtils.bcd2byte(data, 3 + offset) - 1);
        c.set(Calendar.DAY_OF_MONTH, (int) ProtocolUtils.bcd2byte(data, 5 + offset));
        c.set(Calendar.HOUR_OF_DAY, (int) ProtocolUtils.bcd2byte(data,  7 + offset));
        c.set(Calendar.MINUTE, (int) ProtocolUtils.bcd2byte(data, 9 + offset));
        
        // KV 25032004
        
        int seconds = (int) ProtocolUtils.bcd2byte(data, 11 + offset);
        
        if (seconds != 0){
            
            String msg = "Unilog Profile, parseDateTime, seconds != 0 (";
            msg += seconds + ")";
            
            getProtocolLink().getLogger().severe( msg );
            
        }
        
        c.set(Calendar.SECOND, 0); 
        	//(int)ProtocolUtils.bcd2byte(data,11+iOffset));
        c.set(Calendar.MILLISECOND, 0);
        
        return c;
    }

    /** Looks for a '( * )' at position iOffset
     */
    private String parseFindString(byte[] data, int iOffset) {
        int start = 0, stop = 0, i = 0;
        if (iOffset >= data.length) {
			return null;
		}
        for (i = 0; i < data.length; i++) {
            if (data[i + iOffset] == '(') {
				start = i;
			}
            if (data[i + iOffset] == ')') {
                stop = i;
                break;
            }
        }
        byte[] strparse = new byte[stop - start - 1];
        for (i = 0; i < (stop - start - 1); i++) {
			strparse[i] = data[i + start + 1 + iOffset];
		}
        return new String(strparse);
    } // private String parseFindString(byte[] data,int iOffset)

    private static final int STATUS_WORD_STATED = 0x8000;
    private static final int LOGGER_CLEARED = 0x4000;
    private static final int LOGBOOK_CLEARED = 0x2000;
    private static final int EXTERNAL_EVENT_ASSIGNED = 0x1000;
    private static final int WAITING_EXTERNAL_EVENT = 0x0800;
    private static final int END_WRONG_OPERATION = 0x0400;
    private static final int WRONG_OPERATION = 0x0200;
    private static final int PARAMETER_SETTING = 0x0100;
    private static final int POWER_FAILURE = 0x80;
    private static final int POWER_RECOVERY = 0x40;
    private static final int DEVICE_CLOCK_SET = 0x20;
    private static final int DEVICE_RESET = 0x10;
    private static final int SEASONAL_SWITCHOVER = 0x08;
    private static final int DISTURBED_MEASURE = 0x04;
    private static final int RUNNING_RESERVE_EXHAUSTED = 0x02;
    private static final int FATAL_DEVICE_ERROR = 0x01;


    private static final String[] statusstr = { 
	    "FATAL_DEVICE_ERROR",
	    "RUNNING_RESERVE_EXHAUSTED", 
	    "DISTURBED_MEASURE",
	    "SEASONAL_SWITCHOVER", 
	    "DEVICE_RESET", 
	    "DEVICE_CLOCK_ERROR",
	    "POWER_RECOVERY", 
	    "POWER_FAILURE", 
	    "STATUS_WORD_STATED",
	    "LOGGER_CLEARED", 
	    "LOGBOOK_CLEARED", 
	    "EXTERNAL_EVENT_ASSIGNED",
	    "WAITING_EXTERNAL_EVENT", 
	    "END_WRONG_OPERATION", 
	    "WRONG_OPERATION",
	    "PARAMETER_SETTING" 
    };
    

    private long mapLogCodes(long lLogCode) {

        switch ( (int) lLogCode ) {
        
	        case PARAMETER_SETTING:
	            return (MeterEvent.CONFIGURATIONCHANGE);
	
	        case DEVICE_CLOCK_SET:
	            return (MeterEvent.SETCLOCK);
	
	        case WRONG_OPERATION:
	            return (MeterEvent.PROGRAM_FLOW_ERROR);
	
	        case FATAL_DEVICE_ERROR:
	            return (MeterEvent.FATAL_ERROR);

	        case POWER_FAILURE:
	            return (MeterEvent.POWERDOWN);

	        case POWER_RECOVERY:
	            return (MeterEvent.POWERUP);

	        case DEVICE_RESET:
	            return (MeterEvent.CLEAR_DATA);
	
	        case STATUS_WORD_STATED:
	        case LOGGER_CLEARED:
	        case LOGBOOK_CLEARED:
	        case EXTERNAL_EVENT_ASSIGNED:
	        case WAITING_EXTERNAL_EVENT:
	        case END_WRONG_OPERATION:
	        case SEASONAL_SWITCHOVER:
	        case DISTURBED_MEASURE:
	        case RUNNING_RESERVE_EXHAUSTED:
	        default:
	            return (MeterEvent.OTHER);

        } 

    } 
    

    ProfileData buildProfileData(byte[] responseData, int nrOfChannels)
            throws IOException {
        
        ProfileData profileData= new ProfileData();
        TimeZone timeZone = getProtocolLink().getTimeZone();
        Calendar calendar = ProtocolUtils.getCalendar( timeZone );
        
        int status = 0;			
        byte bNROfValues = 0;	// = nmbr of channels
        byte bInterval = 0;		// = duration of interval in min

        int t;
        
        // We suppose that the profile contains nr of channels!!
        try {

            int i = 0;
            while (true) {
                if (responseData[i] == 'P') {	// Parse the header info
                    
                    i += 4; // skip P.01
                    i = gotoNextOpenBracket(responseData, i);

                    if (parseFindString(responseData, i).compareTo("ERROR") == 0) {
						throw new IOException("No entries in object list.");
					}

                    calendar = parseDateTime(responseData, i + 1);
                    i = gotoNextOpenBracket(responseData, i + 1);
                    status = Integer.parseInt(parseFindString(responseData, i), 16);
                    status &= (SEASONAL_SWITCHOVER ^ 0xFFFF);
                    
                    // Adding meterEvents
                    for (t = 0; t < 16; t++) {
                        if ((status & (0x01 << t)) != 0) {
                            
                            Date date = new Date( calendar.getTimeInMillis() );
                            long logCode = (long) (status & (0x01 << t)) & 0xFF;
                            long eiLogCode = mapLogCodes( logCode );
                            int protocolCode = status & 0xff;
                            
                            MeterEvent me = 
                                new MeterEvent( date, (int)eiLogCode, protocolCode );
                            
                            profileData.addEvent( me );
                            
                        }
                    }

                    i = gotoNextOpenBracket(responseData, i + 1);
                    bInterval = (byte) Integer.parseInt(parseFindString(
                            responseData, i));
                    i = gotoNextOpenBracket(responseData, i + 1);
                    bNROfValues = ProtocolUtils.bcd2nibble(responseData, i + 1);
                    if (bNROfValues > nrOfChannels) {
						throw new IOException(
                                "buildProfileData() error, mismatch between nrOfChannels(" + nrOfChannels + ") and profile columns(" + bNROfValues + ")!");
					}

                    if(profileData.getChannelInfos().size() == 0){
                    	String channelName;
                    	String channelUnit;
                    	Unit cUnit;
                    	ChannelInfo ci;
                    	// TODO build channelInfos
                    	for (t = 0; t < bNROfValues; t++) {
                    		channelName = "";
                    		channelUnit = "";
                    		i = gotoNextOpenBracket(responseData, i + 1);
                    		channelName = parseFindString(responseData, i);
                    		i = gotoNextOpenBracket(responseData, i + 1);
                    		channelUnit = parseFindString(responseData, i);
                    		if(!channelUnit.equalsIgnoreCase("")){
                    			cUnit = Unit.get(channelUnit);
                    		} else {
                    			cUnit = Unit.getUndefined();
                    		} 
                    		ci = new ChannelInfo(t, "EdisChannel-"+channelName, cUnit);
                    		if(getProtocolLink().getProtocolChannelMap().getProtocolChannel(t).isCumul()){
                    			ci.setCumulativeWrapValue(getProtocolLink().getProtocolChannelMap().getProtocolChannel(t).getWrapAroundValue());
                    		}
                    		profileData.addChannel(ci);
                    		
                    	}
                    }

                    i = gotoNextCR(responseData, i + 1);
                } else if ((responseData[i] == '\r') || (responseData[i] == '\n')) {
                    i += 1; // skip
                } else {					// Parse the profileEntry
                    // Fill profileData
                    IntervalData intervalData = new IntervalData(new Date(
                            ((Calendar) calendar.clone()).getTime().getTime()));
                    for (t = 0; t < bNROfValues; t++) {
                        i = gotoNextOpenBracket(responseData, i);
                        intervalData.addValue(new BigDecimal(parseFindString(
                                responseData, i)));
                        i++;
                    }
                    if ((status & DISTURBED_MEASURE) != 0) {
						intervalData.addStatus(IntervalData.CORRUPTED);
					}
                    profileData.addInterval(intervalData);
                    calendar.add(Calendar.MINUTE, bInterval);
                    i = gotoNextCR(responseData, i + 1);
                }
                if (i >= responseData.length) {
					break;
				}
            }
            
        } catch (IOException e) {
            throw new IOException("buildProfileData> " + e.getMessage());
        }
        
        return profileData;
        
    } 
    

}

