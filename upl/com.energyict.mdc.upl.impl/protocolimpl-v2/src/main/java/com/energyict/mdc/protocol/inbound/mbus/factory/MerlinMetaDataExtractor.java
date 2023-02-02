package com.energyict.mdc.protocol.inbound.mbus.factory;

import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.protocol.inbound.mbus.InboundContext;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.Telegram;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import java.time.ZoneId;
import java.util.Optional;
import java.util.TimeZone;

public class MerlinMetaDataExtractor {
    public static final String PROPERTY_DEVICE_TIME_ZONE = "upl.property.v2.elster.timezone";
    public static final String ENCRYPTION_KEY = "EncryptionKey";

    private final InboundContext inboundContext;
    private final Telegram encryptedTelegram;
    private final boolean valid;
    private DeviceIdentifierBySerialNumber deviceIdentifier;
    private String serialNumber;

    public MerlinMetaDataExtractor(Telegram encryptedTelegram, InboundContext inboundContext) {
        this.encryptedTelegram = encryptedTelegram;
        this.inboundContext = inboundContext;
        this.valid = lookupDeviceProperties();
    }

    public boolean isValid() {
        return valid;
    }

    public DeviceIdentifier getDeviceIdentifier() {
        if (this.deviceIdentifier == null) {
            this.serialNumber = encryptedTelegram.getSerialNr();
            this.deviceIdentifier = new DeviceIdentifierBySerialNumber(serialNumber);
        }
        return this.deviceIdentifier;
    }


    private boolean lookupDeviceProperties() {
        getDeviceIdentifier();

        inboundContext.getLogger().info("Identified device: " + getDeviceIdentifier());
        inboundContext.getLogger().setSerialNumber(serialNumber);

        try {
            getDeviceTimeZoneFromCore();
        } catch (Exception ex) {
            inboundContext.getLogger().error("Device not found");
            return false;
        }

        try {
            getEncryptionKeyFromCore();
        } catch (Exception ex) {
            inboundContext.getLogger().error("Cannot extract encryption key", ex);
            return false;
        }

        return true;
    }

    private void getEncryptionKeyFromCore() {
        String encryptionKey = "01010101010101010101010101010101";

        Optional<DeviceProtocolSecurityPropertySet> securityProtocols = inboundContext.getInboundDiscoveryContext().getDeviceProtocolSecurityPropertySet(getDeviceIdentifier());

        if (securityProtocols.isPresent()) {
            encryptionKey = (String) securityProtocols.get().getSecurityProperties().getProperty(ENCRYPTION_KEY);
        } else {
            inboundContext.getLogger().warn("Security protocol properties are not available!");
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