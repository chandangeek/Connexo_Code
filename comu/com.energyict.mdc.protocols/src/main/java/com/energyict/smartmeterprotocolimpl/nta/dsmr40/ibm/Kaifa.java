/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.nta.dsmr40.ibm;

import com.elster.jupiter.calendar.CalendarService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.mdc.protocol.api.dialer.core.SerialCommunicationChannel;
import com.energyict.protocols.mdc.services.impl.OrmClient;

import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.E350;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.Dsmr40Messaging;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.KaifaDsmr40MessageExecutor;

import javax.inject.Inject;
import java.io.IOException;
import java.time.Clock;

public class Kaifa extends E350 {

    private Dsmr40Messaging messageProtocol = null;

    @Inject
    public Kaifa(PropertySpecService propertySpecService, Clock clock, TopologyService topologyService, CalendarService calendarService, OrmClient ormClient, MdcReadingTypeUtilService readingTypeUtilService, LoadProfileFactory loadProfileFactory) {
        super(propertySpecService, clock, topologyService, calendarService, ormClient, readingTypeUtilService, loadProfileFactory);
    }

    public String getProtocolDescription() {
        return "IBM Kaifa DLMS (NTA DSMR4.0)";
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-05-02 17:50:49 +0200 (do, 02 mei 2013) $";
    }

    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        try {
            getDlmsSession().init();
        } catch (IOException e) {
            getLogger().warning("Failed while initializing the DLMS connection.");
        }
        HHUSignOn hhuSignOn = new KaifaHHUConnection(commChannel, getProperties().getTimeout(), getProperties().getRetries(), 300, 0);
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);                                  //HDLC:         9600 baud, 8N1
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(datareadout);
        getDlmsSession().getDLMSConnection().setHHUSignOn(hhuSignOn, "", 0);            //IEC1107:      300 baud, 7E1
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        if (messageProtocol == null) {
            messageProtocol = new Dsmr40Messaging(new KaifaDsmr40MessageExecutor(this, this.getClock(), this.getTopologyService(), this.getCalendarService()));
        }
        return messageProtocol;
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        try {
            byte[] firmwareVersion = getMeterInfo().getFirmwareVersion().getBytes();
            return ProtocolTools.getHexStringFromBytes(firmwareVersion);
        } catch (IOException e) {
            String message = "Could not fetch the firmwareVersion. " + e.getMessage();
            getLogger().finest(message);
            return "Unknown version";
        }
    }

    @Override
    public DlmsProtocolProperties getProperties() {
        if (this.properties == null) {
            this.properties = new KaifaProperties();
        }
        return this.properties;
    }
}