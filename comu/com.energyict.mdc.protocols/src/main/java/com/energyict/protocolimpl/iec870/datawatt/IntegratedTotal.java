/*
 * MeterReading.java
 *
 * Created on 4 juli 2003, 16:58
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
public class IntegratedTotal extends Information {

    //private static final String[] statusbits={"IV","CA","CY","SQ4","SQ3","SQ2","SQ1","SQ0"};

    /** Creates a new instance of IntegratedTotal */
     public IntegratedTotal(Calendar calendar, TimeZone timeZone, IEC870InformationObject io) throws IOException {
        value = BigDecimal.valueOf((long)ProtocolUtils.getIntLE(io.getObjData()));
        status = ProtocolUtils.getIntLE(io.getObjData(),4,1);
        int channelId = io.getAddress()&AddressMap.MAX_ADDRESS;
        int addresstype = io.getAddress()&(AddressMap.MAX_ADDRESS^0xFFFF);
        channel = new Channel(channelId, Channel.toChannelType(addresstype),1);

        if (calendar != null) {
            calendar.set(Calendar.MINUTE,0);
            calendar.set(Calendar.SECOND,0);
            cp24 = new CP24Time2a(timeZone,io.getObjData(),5);
            calendar.add(Calendar.SECOND, cp24.getSeconds());
            date = calendar.getTime();
        }
    }

} // public class MeterReading
