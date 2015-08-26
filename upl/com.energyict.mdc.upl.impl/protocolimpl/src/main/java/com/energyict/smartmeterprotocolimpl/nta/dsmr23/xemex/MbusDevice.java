package com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex;

import com.energyict.protocol.MessageProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex.messaging.XemexWatchTalkMbusMessaging;

/**
 * @author sva
 * @since 20/03/2014 - 11:58
 */
public class MbusDevice extends com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.MbusDevice {

    @Override
    public MessageProtocol getMessageProtocol() {
        return new XemexWatchTalkMbusMessaging();
    }

    @Override
    public String getVersion() {
        return "$Date: 2014-03-25 16:05:35 +0100 (Tue, 25 Mar 2014) $";
    }
}
