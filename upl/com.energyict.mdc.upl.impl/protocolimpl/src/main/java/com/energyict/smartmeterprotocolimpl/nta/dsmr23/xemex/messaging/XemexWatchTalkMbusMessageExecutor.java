package com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex.messaging;

import com.energyict.dlms.cosem.Disconnector;
import com.energyict.protocolimpl.generic.messages.MessageHandler;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MbusMessageExecutor;

import java.io.IOException;
import java.util.logging.Level;

/**
 * @author sva
 * @since 24/03/2014 - 11:34
 */
public class XemexWatchTalkMbusMessageExecutor extends Dsmr23MbusMessageExecutor {

    public XemexWatchTalkMbusMessageExecutor(AbstractSmartNtaProtocol protocol) {
        super(protocol);
    }

    @Override
    protected void doConnectMessage(MessageHandler messageHandler, String serialNumber) throws IOException {
        log(Level.INFO, "Handling MbusMessage Connect");

        // Do an immediate connect
        log(Level.INFO, "Doing immediate remote connect");
        Disconnector connector = getCosemObjectFactory().getDisconnector(getMeterConfig().getMbusDisconnectControl(getMbusAddress(serialNumber)).getObisCode());
        connector.remoteReconnect();
    }

    @Override
    protected void doDisconnectMessage(MessageHandler messageHandler, String serialNumber) throws IOException {
        log(Level.INFO, "Handling MbusMessage Disconnect");

        // Do an immediate disconnect
        log(Level.INFO, "Doing immediate remote connect");
        Disconnector connector = getCosemObjectFactory().getDisconnector(getMeterConfig().getMbusDisconnectControl(getMbusAddress(serialNumber)).getObisCode());
        connector.remoteDisconnect();
    }
}