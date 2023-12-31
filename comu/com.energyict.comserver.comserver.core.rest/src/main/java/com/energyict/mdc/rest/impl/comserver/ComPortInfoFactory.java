/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl.comserver;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.comserver.CoapBasedInboundComPort;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.InboundComPort;
import com.energyict.mdc.common.comserver.ModemBasedInboundComPort;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.comserver.ServletBasedInboundComPort;
import com.energyict.mdc.common.comserver.TCPBasedInboundComPort;
import com.energyict.mdc.common.comserver.UDPBasedInboundComPort;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.ports.ComPortType;

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
        switch (comPort.getComPortType()) {
            case TCP:
                return (InboundComPortInfo) setLocalisedValue(new TcpInboundComPortInfo((TCPBasedInboundComPort) comPort));
            case SERIAL:
                return (InboundComPortInfo) setLocalisedValue(new ModemInboundComPortInfo((ModemBasedInboundComPort) comPort));
            case UDP:
                return (InboundComPortInfo) setLocalisedValue(new UdpInboundComPortInfo((UDPBasedInboundComPort) comPort));
            case COAP:
                return (InboundComPortInfo) setLocalisedValue(new CoapInboundComPortInfo((CoapBasedInboundComPort) comPort));
            case SERVLET:
                return (InboundComPortInfo) setLocalisedValue(new ServletInboundComPortInfo((ServletBasedInboundComPort) comPort));
            default:
                throw new IllegalArgumentException("Unsupported InboundComPort type " + comPort.getClass().getSimpleName());
        }
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
