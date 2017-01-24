/*
 * KamstrupProfile.java
 *
 * Created on 04 mei 2004, 10:00
 */

package com.energyict.protocolimpl.iec1107.ferranti;

import com.energyict.mdc.protocol.api.MeterExceptionInfo;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.vdew.AbstractVDEWRegistry;
import com.energyict.protocolimpl.iec1107.vdew.VDEWProfile;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
/**
 *
 * @author  Koen
 * Changes:
 * KV 04052004 Initial version
 */
public class FerrantiProfile extends VDEWProfile {

    private static final int DEBUG=0;


    private static final Unit[] FERRANTI_PROTILEDATAUNITS = {Unit.get("m3"),Unit.get("m3"),Unit.get("")};

    /** Creates a new instance of KamstrupProfile */
    public FerrantiProfile(MeterExceptionInfo meterExceptionInfo,ProtocolLink protocolLink, AbstractVDEWRegistry abstractVDEWRegistry) {
        super(meterExceptionInfo,protocolLink,abstractVDEWRegistry);
    }

    public ProfileData getProfileData(Calendar fromCalendar, Calendar toCalendar, int nrOfChannels) throws IOException {
        byte[] data = vdewReadR6(buildData(fromCalendar,toCalendar));
        return parse(data, nrOfChannels);
    }

    private byte[] buildData(Calendar fromCalendar, Calendar toCalendar) throws IOException {
        StringBuffer strBuff = new StringBuffer();
        String obisB = getProtocolLink().getChannelMap().getChannel(0).getRegister();
        strBuff.append("7-"+obisB+":99.1.0*255(");
        DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        FieldPosition fieldPosition= new FieldPosition(0);
        format.format(fromCalendar.getTime(),strBuff, fieldPosition);
        strBuff.append(";");
        fieldPosition= new FieldPosition(DateFormat.YEAR_FIELD);
        format.format(toCalendar.getTime(),strBuff, fieldPosition);
        strBuff.append(";10)");
        return strBuff.toString().getBytes();
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
    private int gotoNextClosedBracket(byte[] responseData,int i) {
        while(true) {
            if (responseData[i] == ')') {
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
        while(true) {
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
        TimeZone timeZone;
        if (DEBUG >= 1) {
            // added for unit testing...
            if (getProtocolLink() == null) {
				timeZone = TimeZone.getTimeZone("ECT");
			} else {
				timeZone= getProtocolLink().getTimeZone();
			}
        } else {
			timeZone = TimeZone.getTimeZone("ECT");
		}

        Calendar calendar=ProtocolUtils.getCleanCalendar(timeZone);

        calendar = ProtocolUtils.getCalendar(timeZone);
        int year = ProtocolUtils.bcd2int(data,0+iOffset)*100;
        calendar.set(Calendar.YEAR,ProtocolUtils.bcd2int(data,2+iOffset)+year);
        calendar.set(Calendar.MONTH,ProtocolUtils.bcd2int(data,4+iOffset)-1);
        calendar.set(Calendar.DAY_OF_MONTH,ProtocolUtils.bcd2int(data,6+iOffset));
        calendar.set(Calendar.HOUR_OF_DAY,ProtocolUtils.bcd2int(data,8+iOffset));
        calendar.set(Calendar.MINUTE,ProtocolUtils.bcd2int(data,10+iOffset));
        calendar.set(Calendar.SECOND,ProtocolUtils.bcd2int(data,12+iOffset));
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
            if ((data[i+iOffset]==')') || (data[i+iOffset]=='*')) {
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


    private static final int RESERVED = 0x80;
    private static final int PARAMETER_SETTING = 0x40;
    private static final int SEASONAL_SWITCHOVER = 0x20;
    private static final int DEVICE_CLOCK_SET = 0x10;
    private static final int LOW_POWER_CONVERTOR = 0x08;
    private static final int POWER_FAILURE = 0x04;
    private static final int METROLOGICAL_ALARM = 0x02;
    private static final int FATAL_DEVICE_ERROR = 0x01;

    private static final String[] statusstr={"FATAL_DEVICE_ERROR",
                                      "METROLOGICAL_ALARM",
                                      "POWER_FAILURE",
                                      "LOW_POWER_CONVERTOR",
                                      "DEVICE_CLOCK_SET",
                                      "SEASONAL_SWITCHOVER",
                                      "PARAMETER_SETTING",
                                      "RESERVED"};

    private long mapLogCodes(long lLogCode) {
        switch((int)lLogCode) {
            case PARAMETER_SETTING:
                return(MeterEvent.CONFIGURATIONCHANGE);

            case DEVICE_CLOCK_SET:
                return(MeterEvent.SETCLOCK);

            case FATAL_DEVICE_ERROR:
                return(MeterEvent.FATAL_ERROR);

            case LOW_POWER_CONVERTOR:
            case POWER_FAILURE:
                return(MeterEvent.POWERDOWN);

            case METROLOGICAL_ALARM:
                return(MeterEvent.METER_ALARM);

            case RESERVED:
            case SEASONAL_SWITCHOVER:
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
            profileData = new ProfileData();
            for (t=0;t<nrOfChannels;t++) {
                ChannelInfo chi = new ChannelInfo(t,"ferranti_channel_"+t,FERRANTI_PROTILEDATAUNITS[t]);
                if ((t>=0) && (t<=1)) {
					chi.setCumulativeWrapValue(new BigDecimal("100000000"));
				}
                profileData.addChannel(chi);
            }

            int i=0;
            i=gotoNextOpenBracket(responseData,i);
            i++; // skip '('
            while(true) {
                if ((responseData[i] == '7') && (responseData[i+1] == '-')) {
                    // skip header
                    i= gotoNextCR(responseData,i+1);
                    i+=2;
                } // if header data line
                else {

                    // get calendar
                    i=gotoNextOpenBracket(responseData,i);
                    calendar = parseDateTime(responseData,i+1);

                    // create intervaldata
                    IntervalData intervalData = new IntervalData(new Date(((Calendar)calendar.clone()).getTime().getTime()));
                    // get bruto volume
                    i=gotoNextOpenBracket(responseData,i+1);
                    intervalData.addValue(new BigDecimal(parseFindString(responseData,i)));
                    // get netto volume
                    i=gotoNextOpenBracket(responseData,i+1);
                    intervalData.addValue(new BigDecimal(parseFindString(responseData,i)));
                    // get status
                    i=gotoNextOpenBracket(responseData,i+1);
                    status =Integer.parseInt(parseFindString(responseData,i),16);
                    intervalData.addValue(new Integer(status));
                    if ((status & METROLOGICAL_ALARM) != 0) {
						intervalData.addStatus(IntervalData.CORRUPTED);
					}
                    profileData.addInterval(intervalData);

                    for (t=0;t<8;t++) {
                        if ((status & (0x01<<t)) != 0) {
                            profileData.addEvent(new MeterEvent(new Date(((Calendar)calendar.clone()).getTime().getTime()),
                            (int)mapLogCodes((long)(status&(0x01<<t))&0xFF),
                            status&0xff));
                        }
                    }

                    i= gotoNextClosedBracket(responseData,i+1);
                    i++;
                    //i= gotoNextCR(responseData,i+1);
                    //i+=2;
                } // if response data line


                if (i>=responseData.length) {
					break;
				}

            } // while(true)
        }
        catch(IOException e) {
            throw new IOException("buildProfileData> "+e.getMessage());
        }

        if (DEBUG >= 1) {
            // for the Unit testing
            if (getProtocolLink() == null) {
				profileData.applyEvents(60);
			}
        }

        profileData.sort();

        return profileData;
    }

}