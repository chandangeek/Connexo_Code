package com.energyict.protocolimplv2.umi.ei4;

import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.BinaryInboundDeviceProtocol;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocolcommon.Pair;
import com.energyict.protocolimplv2.umi.UmiConfigurationSupport;
import com.energyict.protocolimplv2.umi.ei4.structures.UmiHelper;
import com.energyict.protocolimplv2.umi.packet.payload.ReadObjPartRspPayload;
import com.energyict.protocolimplv2.umi.properties.UmiPropertiesBuilder;
import com.energyict.protocolimplv2.umi.properties.UmiSessionProperties;
import com.energyict.protocolimplv2.umi.session.IUmiSession;
import com.energyict.protocolimplv2.umi.session.UmiSession;
import com.energyict.protocolimplv2.umi.types.ResultCode;
import com.energyict.protocolimplv2.umi.types.UmiCode;
import com.energyict.protocolimplv2.umi.types.UmiObjectPart;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class EI4UmiInbound implements BinaryInboundDeviceProtocol {
    private static final UmiCode UMI_ID_CODE = new UmiCode("umi.0.0.0.0");

    protected ComChannel comChannel;
    private InboundDiscoveryContext context;
    private DeviceIdentifier deviceIdentifier;
    protected UmiConfigurationSupport configurationSupport;
    private PropertySpecService propertySpecService;

    public EI4UmiInbound(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public void initComChannel(ComChannel comChannel) {
        this.comChannel = comChannel;
    }

    @Override
    public String getVersion() {
        return "$Date: 2021-03-04 12:00:00 +0200 (Thu, 3 Mar 2021) $";
    }

    @Override
    public void initializeDiscoveryContext(InboundDiscoveryContext context) {
        this.context = context;
    }

    @Override
    public InboundDiscoveryContext getContext() {
        return context;
    }

    @Override
    public DiscoverResultType doDiscovery() {
        readRemoteDeviceUmiId();
        return DiscoverResultType.IDENTIFIER;
    }

    @Override
    public void provideResponse(DiscoverResponseType responseType) {
        // Nothing to do here - the device doesn't expect an answer from the head-end system
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    @Override
    public String getAdditionalInformation() {
        return "";
    }

    @Override
    public List<CollectedData> getCollectedData() {
        return Collections.emptyList();
    }

    @Override
    public boolean hasSupportForRequestsOnInbound() {
        return true;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return getConfigurationSupport().getUPLPropertySpecs();
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        getConfigurationSupport().setUPLProperties(properties);
    }

    protected HasDynamicProperties getConfigurationSupport() {
        if (configurationSupport == null) {
            configurationSupport = new UmiConfigurationSupport(this.propertySpecService);
        }
        return configurationSupport;
    }

    protected Logger getLogger() {
        return getContext().getLogger();
    }

    private void readRemoteDeviceUmiId() {
        UmiSessionProperties properties = new UmiPropertiesBuilder().sourceUmiId(UmiSession.thisUmiId).build();
        IUmiSession session = new UmiSession(comChannel, properties);
        try {
            ResultCode result = session.startSession();
            if (!result.equals(ResultCode.OK)) {
                getLogger().warning("No umi link session can be established");
                throw new ProtocolException("No umi link session can be established");
            }

            Pair<ResultCode, ReadObjPartRspPayload> pairSN = session.readObjectPart(new UmiObjectPart("umi.1.1.0.0/3"));
            if (pairSN.getFirst() == ResultCode.OK) {
                byte[] receivedSN = pairSN.getLast().getValue();
                String serialNumber = String.valueOf(UmiHelper.convertBytesToChars(receivedSN)).trim();
                this.deviceIdentifier = new DeviceIdentifierBySerialNumber(serialNumber);
                getLogger().info("Device serial number is: " + serialNumber);
            } else {
                getLogger().warning("Error while getting remote device modem serial number, result=" + pairSN.getFirst().getDescription());
                throw new ProtocolException("Error while getting remote device modem serial number, result=" + pairSN.getFirst().getDescription());
            }
        } catch (Exception e) {
            getLogger().warning(e.getMessage());
            throw CommunicationException.protocolConnectFailed(e);
        }
    }
}
