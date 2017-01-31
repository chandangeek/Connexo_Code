/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl.comserver;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.ModemBasedInboundComPort;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.ServletBasedInboundComPort;
import com.energyict.mdc.engine.config.TCPBasedInboundComPort;
import com.energyict.mdc.engine.config.UDPBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;

import javax.inject.Inject;

public class ComPortInfoFactory {
    private final Thesaurus thesaurus;

    @Inject
    public ComPortInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public ComPortInfo asInfo(ComPort comPort, EngineConfigurationService engineConfigurationService) {
        if (InboundComPort.class.isAssignableFrom(comPort.getClass())) {
            InboundComPortInfo comPortInfo =  asInboundInfo(comPort);
            comPortInfo.direction = TranslationKeys.COMPORT_INBOUND.getDisplayName(thesaurus);
            return comPortInfo;
        } else {
            OutboundComPortInfo comPortInfo =  asOutboundInfo(comPort, engineConfigurationService);
            comPortInfo.direction = TranslationKeys.COMPORT_OUTBOUND.getDisplayName(thesaurus);
            return comPortInfo;
        }
    }

    public InboundComPortInfo asInboundInfo(ComPort comPort) {
        if (ComPortType.TCP.equals(comPort.getComPortType())) {
            return (InboundComPortInfo)setLocalisedValue(new TcpInboundComPortInfo((TCPBasedInboundComPort) comPort));
        }
        if (ComPortType.SERIAL.equals(comPort.getComPortType())) {
            return (InboundComPortInfo)setLocalisedValue(new ModemInboundComPortInfo((ModemBasedInboundComPort) comPort));
        }
        if (ComPortType.UDP.equals(comPort.getComPortType())) {
            return (InboundComPortInfo)setLocalisedValue(new UdpInboundComPortInfo((UDPBasedInboundComPort) comPort));
        }
        if (ComPortType.SERVLET.equals(comPort.getComPortType())) {
            return (InboundComPortInfo)setLocalisedValue(new ServletInboundComPortInfo((ServletBasedInboundComPort) comPort));
        }
        throw new IllegalArgumentException("Unsupported InboundComPort type "+comPort.getClass().getSimpleName());
    }

    public OutboundComPortInfo asOutboundInfo(ComPort comPort, EngineConfigurationService engineConfigurationService) {
        if (ComPortType.TCP.equals(comPort.getComPortType())) {
            return (OutboundComPortInfo)setLocalisedValue(new TcpOutboundComPortInfo((OutboundComPort) comPort, engineConfigurationService));
        }
        if (ComPortType.UDP.equals(comPort.getComPortType())) {
            return (OutboundComPortInfo)setLocalisedValue(new UdpOutboundComPortInfo((OutboundComPort) comPort, engineConfigurationService));
        }
        if (ComPortType.SERIAL.equals(comPort.getComPortType())) {
            return (OutboundComPortInfo)setLocalisedValue(new ModemOutboundComPortInfo((OutboundComPort) comPort, engineConfigurationService));
        }
        throw new IllegalArgumentException("Unsupported OutboundComPort type "+comPort.getComPortType());
    }

    private ComPortInfo setLocalisedValue(ComPortInfo info) {
        if (info != null) {
            if(info.comPortType != null && info.comPortType.id != null) {
                ComPortTypeAdapter comPortTypeAdapter = new ComPortTypeAdapter();
                info.comPortType.localizedValue = thesaurus.getString(comPortTypeAdapter.marshal(info.comPortType.id), comPortTypeAdapter.marshal(info.comPortType.id));
            }
            if(info.flowControl != null && info.flowControl.id != null) {
                FlowControlAdapter flowControlAdapter = new FlowControlAdapter();
                info.flowControl.localizedValue = thesaurus.getString(flowControlAdapter.marshal(info.flowControl.id), flowControlAdapter.marshal(info.flowControl.id));
            }
            if(info.parity != null && info.parity.id != null) {
                ParitiesAdapter paritiesAdapter = new ParitiesAdapter();
                info.parity.localizedValue = thesaurus.getString(paritiesAdapter.marshal(info.parity.id), paritiesAdapter.marshal(info.parity.id));
            }
        }
        return info;
    }
}
