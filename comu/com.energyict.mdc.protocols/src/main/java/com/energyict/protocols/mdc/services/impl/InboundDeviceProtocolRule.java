package com.energyict.protocols.mdc.services.impl;

import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.mdc.protocol.inbound.g3.Beacon3100PushEventNotification;
import com.energyict.mdc.protocol.inbound.general.DoubleIframeDiscover;
import com.energyict.mdc.protocol.inbound.general.IframeDiscover;
import com.energyict.mdc.upl.InboundDeviceProtocol;

import com.energyict.protocolimpl.edmi.mk10.MK10InboundDeviceProtocol;
import com.energyict.protocolimplv2.ace4000.ACE4000Inbound;

public enum InboundDeviceProtocolRule implements PluggableClassDefinition<InboundDeviceProtocol> {

    EIWebBulk(com.energyict.protocolimplv2.eict.eiweb.EIWebBulk.class),
    DlmsSerialNumberDiscover(com.energyict.mdc.protocol.inbound.dlms.DlmsSerialNumberDiscover.class),
    IFrameDiscover(IframeDiscover.class),
    DoubleIFrameDiscover(DoubleIframeDiscover.class),
    RequestDiscover(com.energyict.mdc.protocol.inbound.general.RequestDiscover.class),
    DialHome(com.energyict.mdc.protocol.inbound.general.RequestDiscover.class),
    Ace4000(ACE4000Inbound.class),
    MK10_INBOUND(MK10InboundDeviceProtocol.class),
    BEACON_3100_PUSH(Beacon3100PushEventNotification.class);

    private final Class<? extends InboundDeviceProtocol> inboundDeviceProtocolClass;

    InboundDeviceProtocolRule(Class<? extends InboundDeviceProtocol> inboundDeviceProtocolClass) {
        this.inboundDeviceProtocolClass = inboundDeviceProtocolClass;
    }

    @Override
    public String getName() {
        return this.name();
    }

    public Class<? extends InboundDeviceProtocol> getProtocolTypeClass() {
        return inboundDeviceProtocolClass;
    }

}