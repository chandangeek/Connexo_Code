package com.energyict.protocolimplv2.dlms.ei6v2021;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cbo.Unit;
import com.energyict.protocolimplv2.dlms.ei7.EI7DataPushNotificationParser;
import com.energyict.protocolimplv2.dlms.ei7.frames.Frame30;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;

import static com.energyict.protocolimplv2.dlms.ei6v2021.properties.EI6ConfigurationSupport.DATA_VOLUME_SCALAR_PROPERTY;
import static com.energyict.protocolimplv2.dlms.ei6v2021.properties.EI6ConfigurationSupport.DATA_VOLUME_UNIT_PROPERTY;
import static com.energyict.protocolimplv2.dlms.ei6v2021.properties.EI6ConfigurationSupport.EI6_DEFAULT_DATA_VOLUME_SCALAR_PROPERTY;
import static com.energyict.protocolimplv2.dlms.ei6v2021.properties.EI6ConfigurationSupport.EI6_DEFAULT_DATA_VOLUME_UNIT_PROPERTY;
import static com.energyict.protocolimplv2.dlms.ei7.properties.EI7ConfigurationSupport.COMMUNICATION_TYPE_STR;

public class EI6DataPushNotificationParser extends EI7DataPushNotificationParser {
    private Unit dataVolumeUnitScalar;

    public EI6DataPushNotificationParser(ComChannel comChannel, InboundDiscoveryContext context) {
        super(comChannel, context);
    }

    @Override
    public Unit getDataVolumeUnitScalar() {
        if (this.dataVolumeUnitScalar == null) {
            TypedProperties typedProperties = getInboundDAO().getDeviceLocalProtocolProperties(getDeviceIdentifier());
            this.dataVolumeUnitScalar = Unit.get(
                    typedProperties.getTypedProperty(DATA_VOLUME_UNIT_PROPERTY, EI6_DEFAULT_DATA_VOLUME_UNIT_PROPERTY),
                    typedProperties.getTypedProperty(DATA_VOLUME_SCALAR_PROPERTY, EI6_DEFAULT_DATA_VOLUME_SCALAR_PROPERTY));
        }
        return this.dataVolumeUnitScalar;
    }

    @Override
    protected void readCompactFrame30(byte[] compactFrame) {
        try {
            boolean isGPRS = inboundDAO.getDeviceProtocolProperties(getDeviceIdentifier()).getProperty(COMMUNICATION_TYPE_STR)
                    .equals(NetworkConnectivityMessage.TimeoutType.GPRS);
            Frame30.deserialize(compactFrame, false).save(this::addCollectedRegister, this::readLoadProfile, this::getDateTime, isGPRS);
        } catch (Exception e) {
            log("Error while reading compact frame 30:\n" + e.getMessage());
        }
    }
}
