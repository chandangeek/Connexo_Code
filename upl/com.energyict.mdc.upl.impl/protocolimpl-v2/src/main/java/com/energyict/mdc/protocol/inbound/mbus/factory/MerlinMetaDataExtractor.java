package com.energyict.mdc.protocol.inbound.mbus.factory;

import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.protocol.inbound.mbus.InboundContext;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.Telegram;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.SecurityPropertySpecTranslationKeys;
import com.energyict.protocolimplv2.security.DeviceSecurityProperty;

import java.time.ZoneId;
import java.util.Optional;
import java.util.TimeZone;

public class MerlinMetaDataExtractor {
    public static final String PROPERTY_DEVICE_TIME_ZONE = "deviceTimeZone";
    public static final String PROPERTY_ENCRYPTION_KEY_MOCKED_PROPERTY = "callHomeId"; // used for tests only, to migrate to a proper HSM / security accessor
    public static final String ENCRYPTION_KEY = "EncryptionKey";

    private final InboundContext inboundContext;
    private final Telegram encryptedTelegram;
    private DeviceIdentifierBySerialNumber deviceIdentifier;
    private String serialNumber;

    public MerlinMetaDataExtractor(Telegram encryptedTelegram, InboundContext inboundContext) {
        this.encryptedTelegram = encryptedTelegram;
        this.inboundContext = inboundContext;
        lookupDeviceProperties();
    }

    public DeviceIdentifier getDeviceIdentifier() {
        if (this.deviceIdentifier == null) {
            this.serialNumber = encryptedTelegram.getSerialNr();
            this.deviceIdentifier = new DeviceIdentifierBySerialNumber(serialNumber);
        }
        return this.deviceIdentifier;
    }


    private void lookupDeviceProperties() {
        getDeviceIdentifier();

        inboundContext.getLogger().info("Identified device: " + getDeviceIdentifier());
        inboundContext.getLogger().setSerialNumber(serialNumber);

        getDeviceTimeZoneFromCore();

        getEncryptionKeyFromCore();
    }

    private void getEncryptionKeyFromCore() {
        String encryptionKey = "";

        Optional<DeviceProtocolSecurityPropertySet> securityProtocols = inboundContext.getInboundDiscoveryContext().getDeviceProtocolSecurityPropertySet(getDeviceIdentifier());

        if (securityProtocols.isPresent()) {
            encryptionKey = (String) securityProtocols.get().getSecurityProperties().getProperty(ENCRYPTION_KEY);
        }

        inboundContext.setEncryptionKey(encryptionKey);
        // NO-NO, do not log the actual key, used only for testing and PoC!! FIXME!
        inboundContext.getLogger().info("Using EK: " + encryptionKey);
    }

    private void getDeviceTimeZoneFromCore() {
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


}