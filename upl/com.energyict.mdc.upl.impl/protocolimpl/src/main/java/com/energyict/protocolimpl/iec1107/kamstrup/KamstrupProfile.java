/*
 * KamstrupProfile.java
 *
 * Created on 12 mei 2003, 15:00
 */

package com.energyict.protocolimpl.iec1107.kamstrup;

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


/**
 *
 * @author  Koen
 * Changes:
 * KV 25032004 parseDateTime forced seconds to 0
 */
public class KamstrupProfile extends VDEWProfile {
    
    private static final Unit[] KAMSTRUP_PROTILEDATAUNITS = {Unit.get("m3"),Unit.get("m3"),Unit.get("m3"),Unit.get(BaseUnit.KELVIN,-1),Unit.get("mbar"),Unit.get("")};
    
    /** Creates a new instance of KamstrupProfile */
    public KamstrupProfile(ProtocolLink protocolLink,AbstractVDEWRegistry abstractVDEWRegistry) {
        super(null,protocolLink,abstractVDEWRegistry);
    }

    public ProfileData getProfileData(Calendar fromCalendar, Calendar toCalendar, int nrOfChannels, int profileId) throws IOException {
        byte[] data = readRawData(fromCalendar, toCalendar, profileId); 
        return parse(data, nrOfChannels);
    }
    
    private ProfileData parse(byte[] data, int nrOfChannels) throws IOException {
       ProfileData profileData = buildProfileData(data,nrOfChannels);
       profileData.applyEvents(getProtocolLink().getProfileInterval()/60);
       return profileData;  
    }
    
    protected int gotoNextOpenBracket(byte[] responseData,int i) {
        while(true) {
            if (responseData[i] == '(') {
				break;
			}
            i++;
            if (i>=responseData.length) {
				break;
			}
        }
        return i;
    }
    
    protected int gotoNextCR(byte[] responseData,int i) {
        while(true)
        {
            if (responseData[i] == '\r') {
				break;
			}
            i++;
            if (i>=responseData.length) {
				break;
			}
        }
        return i;
    }
    
    private Calendar parseDateTime(byte[] data,int iOffset) throws IOException {
       int dst;
       dst = ProtocolUtils.hex2nibble(data[iOffset]);
       Calendar calendar = ProtocolUtils.initCalendar(dst==0x01, getProtocolLink().getTimeZone());
       calendar.set(Calendar.YEAR, 2000+(int)ProtocolUtils.bcd2byte(data,1+iOffset));
       calendar.set(Calendar.MONTH, (int)ProtocolUtils.bcd2byte(data,3+iOffset)-1);
       calendar.set(Calendar.DAY_OF_MONTH,(int)ProtocolUtils.bcd2byte(data,5+iOffset));
       calendar.set(Calendar.HOUR_OF_DAY,(int)ProtocolUtils.bcd2byte(data,7+iOffset));
       calendar.set(Calendar.MINUTE,(int)ProtocolUtils.bcd2byte(data,9+iOffset));
       // KV 25032004
       int seconds = (int)ProtocolUtils.bcd2byte(data,11+iOffset);       
       if (seconds != 0) {
		getProtocolLink().getLogger().severe ("KamstrupProfile, parseDateTime, seconds != 0 ("+seconds+")");
	}
       calendar.set(Calendar.SECOND,0); //(int)ProtocolUtils.bcd2byte(data,11+iOffset));
       calendar.set(Calendar.MILLISECOND,0);
       return calendar;
    }
    
    private String parseFindString(byte[] data,int iOffset) {
       int start=0,stop=0,i=0;
       if (iOffset >= data.length) {
		return null;
	}
       for (i=0;i<data.length;i++) {
           if (data[i+iOffset]=='(') {
			start = i;
		}
           if (data[i+iOffset]==')') {
               stop = i;
               break;
           }
       }
       byte[] strparse=new byte[stop-start-1];
       for (i=0;i<(stop-start-1);i++) {
		strparse[i]=data[i+start+1+iOffset];
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
    
    private static final String[] statusstr={"FATAL_DEVICE_ERROR",
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
                                      "PARAMETER_SETTING"};
    
    private long mapLogCodes(long lLogCode) {
        switch((int)lLogCode) {
            case PARAMETER_SETTING:
                return(MeterEvent.CONFIGURATIONCHANGE);
                
            case DEVICE_CLOCK_SET: 
                return(MeterEvent.SETCLOCK);
                
            case WRONG_OPERATION: 
                return(MeterEvent.PROGRAM_FLOW_ERROR);
            
            case FATAL_DEVICE_ERROR: 
                return(MeterEvent.FATAL_ERROR);
            case POWER_FAILURE: 
                return(MeterEvent.POWERDOWN);
            case POWER_RECOVERY: 
                return(MeterEvent.POWERUP);
            case DEVICE_RESET:
                return(MeterEvent.CLEAR_DATA);

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
                return(MeterEvent.OTHER);
            
        } // switch(lLogCode)
        
    } // private void mapLogCodes(long lLogCode)
    
    ProfileData buildProfileData(byte[] responseData,int nrOfChannels) throws IOException {
        ProfileData profileData;
        Calendar calendar;
        int status=0;
        byte bNROfValues=0;
        byte bInterval=0;
        
        int t;
        
        // We suppose that the profile contains nr of channels!!
        try {
            calendar = ProtocolUtils.getCalendar(getProtocolLink().getTimeZone());
            profileData = new ProfileData();        
            for (t=0;t<nrOfChannels;t++) {
               ChannelInfo chi = new ChannelInfo(t,"kamstrup_channel_"+t,KAMSTRUP_PROTILEDATAUNITS[t]);
               if ((t>=0) && (t<=2)) {
				chi.setCumulativeWrapValue(new BigDecimal("100000000"));
			}
               profileData.addChannel(chi);
            }

            int i=0;
            while(true) {
                if (responseData[i] == 'P') {
                   i+=4; // skip P.01
                   i=gotoNextOpenBracket(responseData,i);
                   
                   if (parseFindString(responseData,i).compareTo("ERROR") == 0) {
					throw new IOException("No entries in object list.");
				}
                       
                   calendar = parseDateTime(responseData,i+1);
                   i=gotoNextOpenBracket(responseData,i+1);
                   status = Integer.parseInt(parseFindString(responseData,i),16);
                   status &= (SEASONAL_SWITCHOVER^0xFFFF);
                   for (t=0;t<16;t++) {
                      if ((status & (0x01<<t)) != 0) {
                           profileData.addEvent(new MeterEvent(new Date(((Calendar)calendar.clone()).getTime().getTime()),
                                                       (int)mapLogCodes((long)(status&(0x01<<t))&0xFF),
                                                       status&0xff));
                      }
                   }
                   
                   i=gotoNextOpenBracket(responseData,i+1);
                   bInterval = (byte)Integer.parseInt(parseFindString(responseData,i));
                   i=gotoNextOpenBracket(responseData,i+1);
                   bNROfValues = ProtocolUtils.bcd2nibble(responseData,i+1);
                   if (bNROfValues > nrOfChannels) {
					throw new IOException("buildProfileData() error, mismatch between nrOfChannels and profile columns!");
				}
                   for (t=0;t<bNROfValues;t++) {
                      i=gotoNextOpenBracket(responseData,i+1);
                      i=gotoNextOpenBracket(responseData,i+1);
                   }
                   
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
                   if ((status & DISTURBED_MEASURE) != 0) {
					intervalData.addStatus(IntervalData.CORRUPTED);
				}
                   profileData.addInterval(intervalData);
                   calendar.add(Calendar.MINUTE,bInterval);
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
        return profileData;
    } // ProfileData buildProfileData(byte[] responseData) throws IOException
    
    
} // KamstrupProfile
