/*
 * SinglePointInfo.java
 *
 * Created on 7 juli 2003, 11:36
 */

package com.energyict.protocolimpl.iec870.datawatt;

import com.energyict.protocolimpl.iec870.AddressMap;
import com.energyict.protocolimpl.iec870.CP24Time2a;
import com.energyict.protocolimpl.iec870.IEC870InformationObject;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.TimeZone;

/**
 *
 * @author  Koen
 */
public class SinglePointInfo extends Information {
    
    
    //final String[] statusbits={"IV","NT","SB","BL",null,null,null,"SPI"};
    
    
    /** Creates a new instance of IntegratedTotal */
    public SinglePointInfo(Calendar calendar, TimeZone timeZone, IEC870InformationObject io) throws IOException {
        status = ProtocolUtils.getIntLE(io.getObjData(),0,1) & 0xFE;
        value = BigDecimal.valueOf((long)(ProtocolUtils.getIntLE(io.getObjData(),0,1) & 0x01)); // only spi flag
        int channelId = io.getAddress()&AddressMap.MAX_ADDRESS;
        int addresstype = io.getAddress()&(AddressMap.MAX_ADDRESS^0xFFFF);
        channel = new Channel(channelId, Channel.toChannelType(addresstype),0);
        
        if (calendar != null) {
            calendar.set(Calendar.MINUTE,0);
            calendar.set(Calendar.SECOND,0);
            cp24 = new CP24Time2a(timeZone,io.getObjData(),1);
            calendar.add(Calendar.SECOND, cp24.getSeconds());
            date = calendar.getTime();
        }
    }
    
 
}
