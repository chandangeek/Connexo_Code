/*
 * SCTMProfile.java
 *
 * Created on 6 februari 2003, 11:45
 */

package com.energyict.protocolimpl.siemens7ED62;

import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.sctm.base.SCTMProfileFlags;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author  Koen
 */
public abstract class SCTMProfileSingleBuffer implements SCTMProfileFlags {
    private static final int DEBUG=0;

    protected final int METCOM2=0;
    protected final int SIEMENS7ED62=1;

    protected abstract int getEIStatusFromDeviceStatus(int status);
    protected abstract int getEIStatusFromMeteringValueStatus(int status, int deviceStatus);
    protected abstract int getMeterType();

    byte[] frame;
    private int intervalStatusBehaviour;

    /** Creates a new instance of SCTMProfile */
    public SCTMProfileSingleBuffer(byte[] frame) {
        this.frame = frame;
    }
    public ProfileData getProfileData(int interval, TimeZone timeZone, int nrOfChannels, int digitsPerDecade, boolean removePowerOutageIntervals) throws IOException {
        return getProfileData(interval,timeZone,nrOfChannels,digitsPerDecade,removePowerOutageIntervals,0);
    }
    public ProfileData getProfileData(int interval, TimeZone timeZone, int nrOfChannels, int digitsPerDecade, boolean removePowerOutageIntervals, int intervalStatusBehaviour) throws IOException {

        this.intervalStatusBehaviour=intervalStatusBehaviour;
        ProfileData profileData = new ProfileData();
        int i,t,z;
        byte[] status = new byte[4];
        byte[] flag = new byte[2];
        byte[] val=new byte[digitsPerDecade==-1?4:digitsPerDecade]; // KV 04102004

        if (DEBUG>=1) System.out.println("KV_DEBUG> status.length="+status.length+", flag.length="+flag.length+", val.length="+val.length);

        int iStatus;
        int iFlag;

        if (frame.length < 10) throw new IOException("SCTMProfile, getProfileData, error invalid length");

        for (i=0;i<nrOfChannels;i++)
           profileData.addChannel(new ChannelInfo(i,"Siemens7ED62"+i, Unit.get(BaseUnit.COUNT)));
        i=0;
        SCTMTimeData from = new SCTMTimeData(getDateTime(i));
        i+=20;
        Calendar calendar = from.getCalendar(timeZone);

        // KV 11012006
        if (getMeterType()==SIEMENS7ED62)
            ParseUtils.roundUp2nearestInterval(calendar,interval);

        IntervalData intervalDataSaved=null;
        while(i<frame.length) {
            for (z=0;z<status.length;z++) status[z] = frame[i++];
            iStatus = Integer.parseInt(new String(status),16);

            IntervalData intervalData = new IntervalData(new Date(calendar.getTime().getTime()),
                                                         getEIStatusFromDeviceStatus(iStatus),
                                                         iStatus);

            for (t=0;t<nrOfChannels;t++) {
                for (z=0;z<val.length;z++) val[z] = frame[i++];
                for (z=0;z<flag.length;z++) flag[z] = frame[i++]; // ??? what to do with this, reports voltagedrop on a channel...
                iFlag = Integer.parseInt(new String(flag),16);
                intervalData.addValue(new Integer(Integer.parseInt(new String(val))),iFlag,getEIStatusFromMeteringValueStatus(iFlag,iStatus));
            }

            // KV 11012006
            if (getMeterType()==SIEMENS7ED62) {

                if (intervalDataSaved!=null) {
                    ParseUtils.addIntervalValues(intervalDataSaved,intervalData);
                    intervalData=intervalDataSaved;
                    intervalDataSaved=null;
                }

                if ((iStatus&0xC000)==0xC000) {
                    intervalDataSaved=intervalData;
                }

                if (intervalDataSaved==null) {
                    // When we have all phases failed and null integrating set, then there was a power fail to the meter, we should not add these values into the profiledata
                    if (((iStatus & NP_BIT) == 0) || (!removePowerOutageIntervals))
                        profileData.addInterval(intervalData);
                    calendar.add(Calendar.SECOND, interval);
                }
            }
            else {
                // When we have all phases failed and null integrating set, then there was a power fail to the meter, we should not add these values into the profiledata
                if (((iStatus & NP_BIT) == 0) || (!removePowerOutageIntervals))
                    profileData.addInterval(intervalData);
                calendar.add(Calendar.SECOND, interval);
            }
        } // while(i<frame.length)

        return profileData;
    }

    private boolean isDST(int status) {
       return ((status & S_BIT) != 0);
    }


    private byte[] getDateTime(int offset) {
       byte[] arr = new byte[10];
       for (int i=0;i<10;i++) {
           arr[i] = frame[i+offset];
       }
       return arr;
    }

    public int getIntervalStatusBehaviour() {
        return intervalStatusBehaviour;
    }

} // public class SCTMProfile
