/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * MeasuredValueNorm.java
 *
 * Created on 7 juli 2003, 8:50
 */

package com.energyict.protocolimpl.iec870.datawatt;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.iec870.AddressMap;
import com.energyict.protocolimpl.iec870.CP24Time2a;
import com.energyict.protocolimpl.iec870.IEC870InformationObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.TimeZone;

/**
 *
 * @author  Koen
 */
public class MeasuredNormValue extends Information {


    //final String[] statusbits={"IV","NT","SB","BL",null,null,null,"OV"};


    /** Creates a new instance of MeasuredNormValue */
    public MeasuredNormValue(Calendar calendar, TimeZone timeZone, IEC870InformationObject io) throws IOException {
        status = ProtocolUtils.getIntLE(io.getObjData(),2,1);
        int channelId = io.getAddress()&AddressMap.MAX_ADDRESS;
        int addresstype = io.getAddress()&(AddressMap.MAX_ADDRESS^0xFFFF);
        channel = new Channel(channelId, Channel.toChannelType(addresstype),0);

//      Blijkbaar wordt bij Datawatt de normalized fixed point value zoals in IEC870-5-4 aangegeven niet zo gebruikt!!!
//      value = (BigDecimal)Calculate.convertNormSignedFP2NumberLE(io.getObjData(),0);
//      Dit blijkt de Datawatt interpretatie te zijn!

        // Blijkt wanneer de sign bit true is dat ik er 2048 moet bij optellen...
        long val = (long)((ProtocolUtils.getIntLE(io.getObjData(),0,2)&0x7FF0)>>4);
        if ((ProtocolUtils.getIntLE(io.getObjData(),0,2) & 0x8000) == 0) val += 2048;

        value = BigDecimal.valueOf(val);
        if (calendar!=null) {
            calendar.set(Calendar.MINUTE,0);
            calendar.set(Calendar.SECOND,0);
            cp24 = new CP24Time2a(timeZone,io.getObjData(),3);
            calendar.add(Calendar.SECOND, cp24.getSeconds());
            date = calendar.getTime();
        }
    }


}
