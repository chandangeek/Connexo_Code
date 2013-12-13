package com.energyict.protocols.mdc;

import com.energyict.mdc.protocol.api.PluggableClassDefinition;
import com.energyict.mdc.protocol.inbound.BinaryInboundDeviceProtocol;
import com.energyict.mdc.protocol.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.inbound.ServletBasedInboundDeviceProtocol;
import com.energyict.protocolimpl.edmi.mk10.MK10InboundDeviceProtocol;
import com.energyict.protocolimplv2.ace4000.ACE4000Inbound;
import com.energyict.protocols.mdc.inbound.general.DialHomeIdRequestDiscover;
import com.energyict.protocols.mdc.inbound.general.DoubleIframeDiscover;
import com.energyict.protocols.mdc.inbound.general.IframeDiscover;

public enum InboundDeviceProtocolRule implements PluggableClassDefinition<InboundDeviceProtocol> {
    ServletInbound(ServletBasedInboundDeviceProtocol.class),
    EIWebBulk(com.energyict.protocolimplv2.eict.eiweb.EIWebBulk.class),
    Binary(BinaryInboundDeviceProtocol.class),
    DlmsSerialNumberDiscover(com.energyict.protocols.mdc.inbound.dlms.DlmsSerialNumberDiscover.class),
    IFrameDiscover(IframeDiscover.class),
    DoubleIFrameDiscover(DoubleIframeDiscover.class),
    RequestDiscover(com.energyict.protocols.mdc.inbound.general.RequestDiscover.class),
    DialHome(DialHomeIdRequestDiscover.class),
    Ace4000(ACE4000Inbound.class),
    MK10(MK10InboundDeviceProtocol.class);

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
