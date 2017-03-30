/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Class17LoadProfileData.java
 *
 * Created on 25 juli 2005, 10:45
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes;

import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.elster.alpha.core.classes.ClassParseUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 *
 * @author koen
 */
public class Class17LoadProfileData extends AbstractClass {


    ClassIdentification classIdentification = new ClassIdentification(17,0,false);
    private int nrOfDays;

    List intervalDatas;

    /** Creates a new instance of Class17LoadProfileData */
    public Class17LoadProfileData(ClassFactory classFactory) {
        super(classFactory);
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        Iterator it = intervalDatas.iterator();
        while(it.hasNext()) {
            IntervalData intervalData = (IntervalData)it.next();
            strBuff.append(intervalData.toString());
        }
        return strBuff.toString();
    }



    protected void parse(byte[] data) throws IOException {
        // end-of-data flag validation
        int end = ProtocolUtils.getInt(data, data.length-8,2) & 0x3FFF;
        if (end != 0x3FFF)
            throw new IOException("Class17LoadProfileData, parse(), no 0x3FFF end-of-data flag found");

        // get last interval ending timestamp
        Date lastIntervalEnding = ClassParseUtils.getDate6(data, data.length-6, getClassFactory().getAlpha().getTimeZone());

        // prepare intervalcalendar
        Calendar intervalCalendar = ProtocolUtils.getCleanCalendar(getClassFactory().getAlpha().getTimeZone());
        intervalCalendar.setTime(lastIntervalEnding);

        intervalDatas= new ArrayList(); // of type IntervalData
        int nrOfChannels = getClassFactory().getClass14LoadProfileConfiguration().getNrOfChannels();
        // from las record to first retrieved
        for (int interval=data.length-(8+2*nrOfChannels); interval>=0; interval-=(2*nrOfChannels)) {
            // for each channel
            IntervalData intervalData = new IntervalData(((Calendar)intervalCalendar.clone()).getTime());
            for (int channel=0;channel<=nrOfChannels;channel++) {

                // KV_TO_DO  what to do with channelstatus, how to translate into eistatus and is it 2 bits or 1 bit ???
                int channelStatus; // bit 16 = parity, bit 15 = outage (Call to Mike thorpe on 12102005)
                int eiChannelStatus=0;
                int value = ProtocolUtils.getInt(data,interval+channel*2, 2);
                channelStatus = value >> 14;
                value &= 0x3FFF;
                if ((channelStatus  & 0x01) == 0x01)
                    eiChannelStatus = IntervalStateBits.POWERDOWN | IntervalStateBits.POWERUP;
                intervalData.addValue(new Integer(value), channelStatus, eiChannelStatus);
            }
            intervalCalendar.add(Calendar.SECOND,(-1)*getClassFactory().getClass14LoadProfileConfiguration().getLoadProfileInterval());
            intervalDatas.add(intervalData);
        }
    }



    protected ClassIdentification getClassIdentification() {
        return classIdentification;
    }

    public int getNrOfDays() {
        return nrOfDays;
    }

    public void setNrOfDays(int nrOfDays) throws IOException {
        this.nrOfDays = nrOfDays;
        int length = nrOfDays * getClassFactory().getClass14LoadProfileConfiguration().getDayRecordSize();
        classIdentification.setLength(length);
    }

    public List getIntervalDatas() {
        return intervalDatas;
    }


}
