package com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocol.MessageProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex.messaging.XemexWatchTalkMbusMessaging;

/**
 * @author sva
 * @since 20/03/2014 - 11:58
 */
public class MbusDevice extends com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.MbusDevice {

    public MbusDevice(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        return new XemexWatchTalkMbusMessaging();
    }

    @Override
    public String getVersion() {
        return "$Date: 2015-08-26 14:01:32 +0200 (Wed, 26 Aug 2015) $";
    }
}
