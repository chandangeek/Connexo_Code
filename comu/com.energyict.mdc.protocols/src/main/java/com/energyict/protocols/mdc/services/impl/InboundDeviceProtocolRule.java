package com.energyict.protocols.mdc.services.impl;

import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.mdc.protocol.inbound.dlms.DlmsSerialNumberDiscover;
import com.energyict.mdc.protocol.inbound.g3.Beacon3100PushEventNotification;
import com.energyict.mdc.protocol.inbound.g3.CryptoBeacon3100PushEventNotification;
import com.energyict.mdc.protocol.inbound.g3.PushEventNotification;
import com.energyict.mdc.protocol.inbound.general.DoubleIframeDiscover;
import com.energyict.mdc.protocol.inbound.general.IframeDiscover;
import com.energyict.mdc.protocol.inbound.general.RequestDiscover;
import com.energyict.mdc.protocol.inbound.idis.AM122PushEventNotification;
import com.energyict.mdc.protocol.inbound.idis.DataPushNotification;
import com.energyict.mdc.protocol.inbound.idis.T210DPushEventNotification;
import com.energyict.mdc.protocol.inbound.mbus.Merlin;
import com.energyict.mdc.protocol.inbound.nfc.NFCDataPushNotification;
import com.energyict.mdc.upl.InboundDeviceProtocol;

import com.energyict.protocolimpl.edmi.mk10.MK10InboundDeviceProtocol;
import com.energyict.protocolimplv2.ace4000.ACE4000Inbound;
import com.energyict.protocolimplv2.dlms.a2.A2Inbound;
import com.energyict.protocolimplv2.dlms.acud.AcudGatewayInbound;
import com.energyict.protocolimplv2.dlms.ei6v2021.EI6v2021Inbound;
import com.energyict.protocolimplv2.dlms.ei7.EI7Inbound;
import com.energyict.protocolimplv2.eict.eiweb.EIWebBulk;
import com.energyict.protocolimplv2.eict.webcatch.WebCatchInboundProtocol;
import com.energyict.protocolimplv2.elster.ctr.MTU155.discover.CtrInboundDeviceProtocol;
import com.energyict.protocolimplv2.elster.ctr.MTU155.discover.ProximusSMSInboundDeviceProtocol;
import com.energyict.protocolimplv2.umi.ei4.EI4UmiInbound;

public enum InboundDeviceProtocolRule implements PluggableClassDefinition<InboundDeviceProtocol> {

    IframeDiscover(IframeDiscover.class),
    DoubleIframeDiscover(DoubleIframeDiscover.class),
    RequestDiscover(RequestDiscover.class),
    A2Inbound(A2Inbound.class),
    EI7Inbound(EI7Inbound.class),
    ACE4000Inbound(ACE4000Inbound.class),
    AM122PushEventNotification(AM122PushEventNotification.class),
    DataPushNotification(DataPushNotification.class),
    T210DPushEventNotification(T210DPushEventNotification.class),
    MK10InboundDeviceProtocol(MK10InboundDeviceProtocol.class),
    PushEventNotification(PushEventNotification.class),
    Beacon3100PushEventNotification(Beacon3100PushEventNotification.class),
    CryptoBeacon3100PushEventNotification(CryptoBeacon3100PushEventNotification.class),
    ProximusSMSInboundDeviceProtocol(ProximusSMSInboundDeviceProtocol.class),
    EIWebBulk(EIWebBulk.class),
    DlmsSerialNumberDiscover(DlmsSerialNumberDiscover.class),
    CtrInboundDeviceProtocol(CtrInboundDeviceProtocol.class),
    NFCDataPushNotification(NFCDataPushNotification.class),
    WebCatchInboundProtocol(WebCatchInboundProtocol.class),
    EI6v2021Inbound(EI6v2021Inbound.class),
    EI4UmiInbound(EI4UmiInbound.class),
    Merlin(com.energyict.mdc.protocol.inbound.mbus.Merlin.class),
    AcudGatewayInbound(AcudGatewayInbound.class);

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