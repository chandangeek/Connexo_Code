package com.energyict.protocolimplv2.dlms.ei6v2021;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cbo.Unit;
import com.energyict.protocolimplv2.dlms.ei7.EI7DataPushNotificationParser;

import static com.energyict.protocolimplv2.dlms.ei6v2021.properties.EI6ConfigurationSupport.DATA_VOLUME_SCALAR_PROPERTY;
import static com.energyict.protocolimplv2.dlms.ei6v2021.properties.EI6ConfigurationSupport.DATA_VOLUME_UNIT_PROPERTY;
import static com.energyict.protocolimplv2.dlms.ei6v2021.properties.EI6ConfigurationSupport.EI6_DEFAULT_DATA_VOLUME_SCALAR_PROPERTY;
import static com.energyict.protocolimplv2.dlms.ei6v2021.properties.EI6ConfigurationSupport.EI6_DEFAULT_DATA_VOLUME_UNIT_PROPERTY;

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
}
