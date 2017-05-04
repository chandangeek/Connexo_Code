package com.energyict.protocols.mdc.services.impl;

import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.mdc.protocol.inbound.dlms.DlmsSerialNumberDiscover;
import com.energyict.mdc.protocol.inbound.g3.Beacon3100PushEventNotification;
import com.energyict.mdc.protocol.inbound.g3.PushEventNotification;
import com.energyict.mdc.protocol.inbound.general.DoubleIframeDiscover;
import com.energyict.mdc.protocol.inbound.general.IframeDiscover;
import com.energyict.mdc.protocol.inbound.general.RequestDiscover;
import com.energyict.mdc.protocol.inbound.idis.DataPushNotification;
import com.energyict.mdc.protocol.inbound.idis.T210DPushEventNotification;
import com.energyict.mdc.upl.InboundDeviceProtocol;
import com.energyict.protocolimpl.edmi.mk10.MK10InboundDeviceProtocol;
import com.energyict.protocolimplv2.ace4000.ACE4000Inbound;
import com.energyict.protocolimplv2.eict.eiweb.EIWebBulk;
import com.energyict.protocolimplv2.elster.ctr.MTU155.discover.CtrInboundDeviceProtocol;
import com.energyict.protocolimplv2.elster.ctr.MTU155.discover.ProximusSMSInboundDeviceProtocol;

public enum InboundDeviceProtocolRule implements PluggableClassDefinition<InboundDeviceProtocol> {

    IframeDiscover(IframeDiscover.class),
    DoubleIframeDiscover(DoubleIframeDiscover.class),
    RequestDiscover(RequestDiscover.class),
    ACE4000Inbound(ACE4000Inbound.class),
    DataPushNotification(DataPushNotification.class),
    T210DPushEventNotification(T210DPushEventNotification.class),
    MK10InboundDeviceProtocol(MK10InboundDeviceProtocol.class),
    PushEventNotification(PushEventNotification.class),
    Beacon3100PushEventNotification(Beacon3100PushEventNotification.class),
    ProximusSMSInboundDeviceProtocol(ProximusSMSInboundDeviceProtocol.class),
    EIWebBulk(EIWebBulk.class),
    DlmsSerialNumberDiscover(DlmsSerialNumberDiscover.class),
    CtrInboundDeviceProtocol(CtrInboundDeviceProtocol.class);

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