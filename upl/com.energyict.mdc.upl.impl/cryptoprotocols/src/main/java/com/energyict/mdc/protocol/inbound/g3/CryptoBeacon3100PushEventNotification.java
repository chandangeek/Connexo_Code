package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.properties.PropertySpecService;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 10/11/2016 - 17:19
 */
public class CryptoBeacon3100PushEventNotification extends Beacon3100PushEventNotification {

    public CryptoBeacon3100PushEventNotification(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory) {
        super(propertySpecService, collectedDataFactory);
    }

    protected BeaconPSKProvider getPskProvider() {
        return CryptoBeaconPSKProviderFactory.getInstance(provideProtocolJavaClasName).getPSKProvider(getDeviceIdentifier());
    }

    @Override
    public String getVersion() {
        return "$Date: 2018-08-02 14:50:00 +0300 (Thu, 02 Aug 2018) $";
    }

    @Override
    protected EventPushNotificationParser getEventPushNotificationParser() {
        if (parser == null) {
            parser = new CryptoBeaconEventPushNotificationParser(comChannel, getContext(), OBIS_STANDARD_EVENT_LOG);
        }
        return parser;
    }
}