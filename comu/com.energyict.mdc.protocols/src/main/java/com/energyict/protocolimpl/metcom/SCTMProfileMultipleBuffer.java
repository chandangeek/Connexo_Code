/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * SCTMProfile.java
 *
 * Created on 6 februari 2003, 11:45
 */

package com.energyict.protocolimpl.metcom;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;

import com.energyict.protocolimpl.sctm.base.SCTMProfileFlags;
import com.energyict.protocolimpl.siemens7ED62.SCTMTimeData;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.TimeZone;
/**
 *
 * @author  Koen
 * Changes:
 * KV 07052004 Extend for multibuffer with more then 1 channel per buffer. Also extend ChannelMap
 */
public abstract class SCTMProfileMultipleBuffer implements SCTMProfileFlags {
    List frames;
    List bufferStructures;
    ChannelMap channelMap;

    protected abstract int getEIStatusFromMeteringValueStatus(int status, int deviceStatus);
    protected abstract int getEIStatusFromDeviceStatus(int status);

    /** Creates a new instance of SCTMProfile */
    public SCTMProfileMultipleBuffer(List frames, ChannelMap channelMap, List bufferStructures) {
        this.frames = frames;
        this.channelMap = channelMap;
        this.bufferStructures=bufferStructures;
    }

    protected BufferStructure getBufferStructure(int bufferNr) {
        return (BufferStructure)bufferStructures.get(bufferNr);
    }


    public ProfileData getProfileData(int interval, TimeZone timeZone, boolean removePowerOutageIntervals) throws IOException {

        if (channelMap.getNrOfUsedBuffers() == 0) {
            throw new IOException("SCTMProfileMetcom3, getProfileData, no buffers used in ChannelMap, all entries < 0!");
        }
        ProfileData[] profileDatas = new ProfileData[channelMap.getNrOfUsedBuffers()];
        int framePtr,z;
        byte[] status = new byte[4];

        byte[] flag = new byte[2];
        byte[] frame;
        int iStatus;
        int profileIndex=0;

        // build the profiledata for each buffer
        for (int bufferNr=0;bufferNr<channelMap.getNrOfBuffers();bufferNr++) {
            byte[] val=new byte[getBufferStructure(bufferNr).getNrOfDecades()==-1?4:getBufferStructure(bufferNr).getNrOfDecades()]; // KV 13122004
            if (channelMap.useBuffer(bufferNr)) {
                profileDatas[profileIndex] = new ProfileData();  // create profile to store parsed buffer data into
                frame = (byte[])frames.get(profileIndex);  // get buffer data
                if (frame.length < 10) {
                    throw new IOException("SCTMProfileMetcom3, getProfileData, error invalid buffer length");
                }

                // create channels
                for (int channelNr=0;channelNr<channelMap.getBuffers()[bufferNr];channelNr++) {
                    if (channelMap.isBufferCumulative(bufferNr)) {
                        ChannelInfo chi = new ChannelInfo(channelNr,"Metcom3"+channelNr, Unit.get(BaseUnit.COUNT));
                        long wrapAround = (long)Math.pow((double)10, (double)getBufferStructure(bufferNr).getNrOfDecades());
                        chi.setCumulativeWrapValue(BigDecimal.valueOf(wrapAround));
                        profileDatas[profileIndex].addChannel(chi);
                    }
                    else {
                        profileDatas[profileIndex].addChannel(new ChannelInfo(channelNr,"Metcom3"+channelNr,Unit.get(BaseUnit.COUNT)));
                    }
                }

                // init variables
                framePtr=0;
                SCTMTimeData from = new SCTMTimeData(getDateTime(framePtr,frame));
                Calendar calendar = from.getCalendar(timeZone);

                framePtr+=20;

                // nr of channels
                do {
                    IntervalData intervalData = new IntervalData(new Date(calendar.getTime().getTime()));

                    // read status, 4 digits
                    for (z=0;z<status.length;z++) {
                        status[z] = frame[framePtr++];
                    }
                    iStatus = Integer.parseInt(new String(status),16);
//if (iStatus!=0) {
//   System.out.println(calendar.getTime()+", iStatus=0x"+Integer.toHexString(iStatus));
//}

                    // If DST active flag is not correct against the java timezone...
                    // don't use this test for the moment...
                    //if (timeZone.inDaylightTime(calendar.getTime()) != ((iStatus&SUMMERTIMEACTIVE)!=0))
                    //    throw new IOException("Meter device status DST is different from the configured timezone's DST status.");
                    // read value and value status for each channel
                    for (int channelNr=0;channelNr<channelMap.getBuffers()[bufferNr];channelNr++) {
                        for (z=0;z<val.length;z++) {
                            val[z] = frame[framePtr++];
                        }
                        for (z=0;z<flag.length;z++) {
                            flag[z] = frame[framePtr++];
                        }
                        int iflag = Integer.parseInt(new String(flag),16);
//if (iflag!=0) {
//   System.out.println(calendar.getTime()+", iflag=0x"+Integer.toHexString(iflag));
//}
                        intervalData.addValue(new Integer(Integer.parseInt(new String(val))),iflag,getEIStatusFromMeteringValueStatus(iflag,iStatus));
                    } // for (channelNr=0;channelNr<channelMap.getBuffers()[bufferNr];channelNr++)
                    intervalData.setEiStatus(getEIStatusFromDeviceStatus(iStatus));
                    intervalData.setProtocolStatus(iStatus);
                    // When we have all phases failed and null integrating set, then there was a power fail to the meter, we should not add these values into the profiledata



                    if (((iStatus & NP_BIT) == 0) || (!removePowerOutageIntervals)) {
                        profileDatas[profileIndex].addInterval(intervalData);
                    }

                    calendar.add(Calendar.SECOND, interval);
                } while (framePtr<frame.length);
                profileIndex++;
            } // if (channelMap.useBuffer(bufferNr))

        } // for (bufferNr=0;bufferNr<channelMap.getNrOfBuffers();bufferNr++)

        // verify the profiledatas
        for (int profileNr=1;profileNr<profileDatas.length;profileNr++) {
            String info0 = "profile "+(profileNr-1)+": from "+profileDatas[profileNr-1].getIntervalData(0).getEndTime().toString()+" to "+profileDatas[profileNr-1].getIntervalData(profileDatas[profileNr-1].getIntervalDatas().size()-1).getEndTime().toString();
            String info1 = "profile "+profileNr+": from "+profileDatas[profileNr].getIntervalData(0).getEndTime().toString()+" to "+profileDatas[profileNr].getIntervalData(profileDatas[profileNr].getIntervalDatas().size()-1).getEndTime().toString();
            if ((profileDatas[profileNr-1].getIntervalData(profileDatas[profileNr-1].getIntervalDatas().size()-1).getEndTime().getTime()) != (profileDatas[profileNr].getIntervalData(profileDatas[profileNr].getIntervalDatas().size()-1)
                    .getEndTime().getTime())) {
                throw new IOException("SCTMProfileMetcom3, profiledatas end on different timestamp --> ("+info0+", "+info1+")");
            }
            if ((profileDatas[profileNr-1].getIntervalData(0).getEndTime().getTime()) != (profileDatas[profileNr].getIntervalData(0).getEndTime().getTime())) {
                throw new IOException("SCTMProfileMetcom3, profiledatas start from different timestamp --> ("+info0+", "+info1+")");

                // new code
//                int maxNrOfIntervals=0;
//                Date earliestStartDate=null;
//                if ((((IntervalData)profileDatas[profileNr-1].getIntervalData(0)).getEndTime().getTime()) < (((IntervalData)profileDatas[profileNr].getIntervalData(0)).getEndTime().getTime())) {
//                    earliestStartDate=((IntervalData)profileDatas[profileNr-1].getIntervalData(0)).getEndTime();
//                    maxNrOfIntervals=profileDatas[profileNr-1].getIntervalDatas().size();
//                }
//                else {
//                    earliestStartDate=((IntervalData)profileDatas[profileNr].getIntervalData(0)).getEndTime();
//                }


            }
            if ((profileDatas[profileNr-1].getIntervalDatas().size()) != (profileDatas[profileNr].getIntervalDatas().size())) {
                throw new IOException("SCTMProfileMetcom3, profiledatas contain different nr of elements --> ("+info0+", "+info1+")");
            }
        }


        // combine the profiledatas to one profiledata
        ProfileData profileData = profileDatas[0];
        for (int profileNr=1;profileNr<profileDatas.length;profileNr++) {
            profileData.getChannelInfos().addAll(profileDatas[profileNr].getChannelInfos());
            profileData.getMeterEvents().addAll(profileDatas[profileNr].getMeterEvents());
            //profileData.getIntervalDatas().addAll(profileDatas[profileNr].getIntervalDatas());
            ListIterator it = profileData.getIntervalIterator();
            ListIterator it2copy = profileDatas[profileNr].getIntervalIterator();
            while(it.hasNext()) {
                IntervalData intervalData = (IntervalData)it.next();
                IntervalData intervalData2copy = (IntervalData)it2copy.next();
                intervalData.addEiStatus(intervalData2copy.getEiStatus());
                intervalData.addProtocolStatus(intervalData2copy.getProtocolStatus());
                intervalData.getIntervalValues().addAll(intervalData2copy.getIntervalValues());
            }
        }
        ListIterator it = profileData.getChannelIterator();
        int id=0;
        while(it.hasNext()) {
            ChannelInfo channelInfo = (ChannelInfo)it.next();
            channelInfo.setId(id);
            channelInfo.setChannelId(id++);
        }

        return profileData;
    }


    protected byte[] getDateTime(int offset,byte[] frame) {
        byte[] arr = new byte[10];
        for (int i=0;i<10;i++) {
            arr[i] = frame[i+offset];
        }
        return arr;
    }

} // public class SCTMProfile
