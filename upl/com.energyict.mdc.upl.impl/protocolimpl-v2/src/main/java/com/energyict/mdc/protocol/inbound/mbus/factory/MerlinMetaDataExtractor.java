package com.energyict.mdc.protocol.inbound.mbus.factory;

import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.protocol.inbound.mbus.InboundContext;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.Telegram;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.properties.TypedProperties;

import java.time.ZoneId;
import java.util.TimeZone;

public class MerlinMetaDataExtractor {
    public static final String PROPERTY_DEVICE_TIME_ZONE = "deviceTimeZone";
    public static final String PROPERTY_ENCRYPTION_KEY_MOCKED_PROPERTY = "callHomeId"; // used for tests only, to migrate to a proper HSM / security accessor

    private final InboundContext inboundContext;
    private final Telegram encryptedTelegram;
    private DeviceIdentifierBySerialNumber deviceIdentifier;

    public MerlinMetaDataExtractor(Telegram encryptedTelegram, InboundContext inboundContext) {
        this.encryptedTelegram = encryptedTelegram;
        this.inboundContext = inboundContext;
        lookupDeviceProperties();
    }

    public DeviceIdentifier getDeviceIdentifier() {
        if (this.deviceIdentifier == null) {
            this.deviceIdentifier = new DeviceIdentifierBySerialNumber(encryptedTelegram.getSerialNr());
        }
        return this.deviceIdentifier;
    }


    private void lookupDeviceProperties() {
        getDeviceIdentifier();

        inboundContext.getLogger().info("Identified device: " + getDeviceIdentifier());

        getDeviceTimeZoneFromCore();

        getEncryptionKeyFromCore();
    }

    /**
     * TODO -> migrate to a security set and accessor / add HSM support
     */
    private void getEncryptionKeyFromCore() {
        TypedProperties protocolProperties = inboundContext.getInboundDiscoveryContext().getInboundDAO().getDeviceProtocolProperties(getDeviceIdentifier());

        ZoneId timeZone;
        if (protocolProperties.hasLocalValueFor(PROPERTY_DEVICE_TIME_ZONE)){
            String configuredTimeZoneId = protocolProperties.getTypedProperty(PROPERTY_DEVICE_TIME_ZONE, TimeZone.getDefault()).getID();
            timeZone = ZoneId.of(configuredTimeZoneId);
            inboundContext.getLogger().info("Using configured time zone of the device: " + timeZone);
        } else {
            timeZone = ZoneId.systemDefault();
            inboundContext.getLogger().info("Using system default time zone : " + timeZone);
        }

        inboundContext.setTimeZone(timeZone);
    }

    private void getDeviceTimeZoneFromCore() {
        TypedProperties protocolProperties = inboundContext.getInboundDiscoveryContext().getInboundDAO().getDeviceProtocolProperties(getDeviceIdentifier());

        String defaultEK = "01 01 01 01 01 01 01 01 01 01 01 01 01 01 01 01";
        String encryptionKey = defaultEK;
        if (protocolProperties.hasLocalValueFor(PROPERTY_ENCRYPTION_KEY_MOCKED_PROPERTY)){
            encryptionKey = protocolProperties.getTypedProperty(PROPERTY_ENCRYPTION_KEY_MOCKED_PROPERTY, defaultEK);
        }
        inboundContext.setEncryptionKey(encryptionKey);
        // NO-NO, do not log the actual key, used only for testing and PoC!! FIXME!
        inboundContext.getLogger().info("Using EK: " + encryptionKey);

    }


}