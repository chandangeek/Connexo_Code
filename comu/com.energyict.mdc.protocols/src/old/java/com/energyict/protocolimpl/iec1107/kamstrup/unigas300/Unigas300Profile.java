/*
 * Unigas300Profile.java
 *
 * Created on 12 mei 2003, 15:00
 */

package com.energyict.protocolimpl.iec1107.kamstrup.unigas300;

import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.vdew.AbstractVDEWRegistry;
import com.energyict.protocolimpl.iec1107.vdew.VDEWProfile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;


/**
 *
 * @author  Koen
 * Changes:
 * KV	25032004 parseDateTime forced seconds to 0
 * JME	28052009 Fixed bug in profile parser: Last 3 channels are hex status channels and no decimal values.
 */
public class Unigas300Profile extends VDEWProfile {

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

    private static final int NUMBER_OF_STATUS_CHANNELS = 3;

	private static final int DEBUG = 0;

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

    private static final Unit[] KAMSTRUP_PROTILEDATAUNITS = {
    	Unit.get("m3"),	Unit.get("m3"), Unit.get("m3"),
    	Unit.get("m3"),	Unit.get("m3"),	Unit.get("m3"),
    	Unit.get(BaseUnit.DEGREE_CELSIUS, -2), Unit.get("mbar"),
    	Unit.get(""), Unit.get(""), Unit.get("")
    };

    /** Creates a new instance of Unigas300Profile */
    public Unigas300Profile(ProtocolLink protocolLink,AbstractVDEWRegistry abstractVDEWRegistry) {
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
       calendar.set(calendar.YEAR,(int)(2000+(int)ProtocolUtils.bcd2byte(data,1+iOffset)));
       calendar.set(calendar.MONTH,(int)((int)ProtocolUtils.bcd2byte(data,3+iOffset)-1));
       calendar.set(calendar.DAY_OF_MONTH,(int)ProtocolUtils.bcd2byte(data,5+iOffset));
       calendar.set(calendar.HOUR_OF_DAY,(int)ProtocolUtils.bcd2byte(data,7+iOffset));
       calendar.set(calendar.MINUTE,(int)ProtocolUtils.bcd2byte(data,9+iOffset));
       // KV 25032004
       int seconds = (int)ProtocolUtils.bcd2byte(data,11+iOffset);
       if (seconds != 0) {
		getProtocolLink().getLogger().severe ("Unigas300Profile, parseDateTime, seconds != 0 ("+seconds+")");
	}
       calendar.set(calendar.SECOND,0); //(int)ProtocolUtils.bcd2byte(data,11+iOffset));
       calendar.set(calendar.MILLISECOND,0);
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

    ProfileData buildProfileData(byte[] responseData,int nrOfChannels) throws IOException {
        if (DEBUG >= 1) {
			System.out.println("\nresponseData = \n\n" + new String(responseData) + "\n");
		}
        if (DEBUG >= 1) {
			System.out.println("nrOfChannels = " + nrOfChannels);
		}
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
               if (DEBUG >= 1) {
				System.out.println("t = " + t + " Channelinfo = " + chi.getName() + " [" + chi.getUnit() + "]");
			}
               if ((t>=0) && (t<=5)) {
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
                   if (DEBUG >= 1) {
					System.out.println("Status = " + status);
				}
                   for (t=0;t<16;t++) {
                      long statPart = status & (0x01<<t);
                	   if (statPart != 0) {
                           profileData.addEvent(createMeterEvent((int) statPart, calendar));
                      }
                   }

                   i=gotoNextOpenBracket(responseData,i+1);
                   bInterval = (byte)Integer.parseInt(parseFindString(responseData,i));
                   if (DEBUG >= 1) {
					System.out.println("bInterval = " + bInterval);
				}
                   i=gotoNextOpenBracket(responseData,i+1);
                   bNROfValues = ProtocolUtils.bcd2byte(responseData,i+1);
                   if (DEBUG >= 1) {
					System.out.println("bNROfValues = " + bNROfValues);
				}
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
                      if (t < (bNROfValues - NUMBER_OF_STATUS_CHANNELS)) {
                    	  intervalData.addValue(new BigDecimal(parseFindString(responseData,i)));
                      } else {
                          intervalData.addValue(new BigDecimal(Integer.parseInt(parseFindString(responseData,i), 16)));
                      }
                      i++;
                   }
                   if ((status & DISTURBED_MEASURE) != 0) {
					intervalData.addStatus(IntervalData.CORRUPTED);
				}
                   profileData.addInterval(intervalData);
                   calendar.add(calendar.MINUTE,bInterval);
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

	private MeterEvent createMeterEvent(int status, Calendar calendar) {
		final int FATAL_ERROR 	= 0x00001;
		final int ALARM_ACTIVE 	= 0x00002;
		final int SUMMERTIME 	= 0x00008;
		final int COUNTER_RESET	= 0x00010;
		final int CLOCK_SET 	= 0x00020;
		final int POWER_UP 		= 0x00040;
		final int EXT_POWER 	= 0x00080;
		final int MET_SETTINGS	= 0x00100;
		final int CONV_ER_START	= 0x00200;
		final int CONV_ER_END	= 0x00400;
		final int LOGBOOKS_ERASED = 0x02000;
		final int LOGGERS_ERASED = 0x04000;

		Date eventDate = new Date(((Calendar)calendar.clone()).getTime().getTime());
		String message = null;
		int eiCode = 0;

		switch (status) {
		case FATAL_ERROR:
			message = "Fatal error";
			eiCode = MeterEvent.FATAL_ERROR;
			break;
		case ALARM_ACTIVE:
			message = "Alarms active";
			eiCode = MeterEvent.METER_ALARM;
			break;
		case SUMMERTIME:
			message = "Summertime active";
			eiCode = MeterEvent.OTHER;
			break;
		case COUNTER_RESET:
			message = "Counter has been reset to 0";
			eiCode = MeterEvent.CLEAR_DATA;
			break;
		case CLOCK_SET:
			message = "Clock has been moved more than xx s";
			eiCode = MeterEvent.SETCLOCK;
			break;
		case POWER_UP:
			message = "A power up or reset has occurred";
			eiCode = MeterEvent.POWERUP;
			break;
		case EXT_POWER:
			message = "No external power supply";
			eiCode = MeterEvent.OTHER;
			break;
		case MET_SETTINGS:
			message = "Metrological setting modified";
			eiCode = MeterEvent.CONFIGURATIONCHANGE;
			break;
		case CONV_ER_START:
			message = "Conversion error: error in p or T";
			eiCode = MeterEvent.APPLICATION_ALERT_START;
			break;
		case CONV_ER_END:
			message = "Conversion error ended";
			eiCode = MeterEvent.APPLICATION_ALERT_STOP;
			break;
		case LOGBOOKS_ERASED:
			message = "Logbooks erased";
			eiCode = MeterEvent.CLEAR_DATA;
			break;
		case LOGGERS_ERASED:
			message = "Loggers erased";
			eiCode = MeterEvent.CLEAR_DATA;
			break;
		default:
			message = "Unknown meterEvent! Status = " + status;
			eiCode = MeterEvent.OTHER;
			break;
		}


		return new MeterEvent(eventDate, eiCode, status, message);
	}


} // Unigas300Profile
