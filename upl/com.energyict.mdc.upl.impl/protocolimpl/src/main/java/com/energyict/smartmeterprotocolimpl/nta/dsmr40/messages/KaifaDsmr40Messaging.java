package com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages;

import com.energyict.protocolimpl.generic.MessageParser;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 7/01/2015 - 11:26
 */
public class KaifaDsmr40Messaging extends Dsmr40Messaging {

    /**
     * Same like the DSMR4.0 standard messages, but the message to reset the MBus client using the serial no
     */
    public KaifaDsmr40Messaging(MessageParser messageExecutor) {
        super(messageExecutor);
        setUseSerialNoInClearMBusClientMessage(true);
    }

}