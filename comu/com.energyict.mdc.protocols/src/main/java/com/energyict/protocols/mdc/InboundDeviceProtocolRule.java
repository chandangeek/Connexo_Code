package com.energyict.protocols.mdc;

import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.protocols.mdc.inbound.g3.Beacon3100PushEventNotification;
import com.energyict.protocols.mdc.inbound.general.DialHomeIdRequestDiscover;
import com.energyict.protocols.mdc.inbound.general.DoubleIframeDiscover;
import com.energyict.protocols.mdc.inbound.general.IframeDiscover;

import com.energyict.protocolimpl.edmi.mk10.MK10InboundDeviceProtocol;
import com.energyict.protocolimplv2.ace4000.ACE4000Inbound;

public enum InboundDeviceProtocolRule implements PluggableClassDefinition<InboundDeviceProtocol> {

    EIWebBulk(com.energyict.protocolimplv2.eict.eiweb.EIWebBulk.class),
    DlmsSerialNumberDiscover(com.energyict.protocols.mdc.inbound.dlms.DlmsSerialNumberDiscover.class),
    IFrameDiscover(IframeDiscover.class),
    DoubleIFrameDiscover(DoubleIframeDiscover.class),
    RequestDiscover(com.energyict.protocols.mdc.inbound.general.RequestDiscover.class),
    DialHome(DialHomeIdRequestDiscover.class),
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
