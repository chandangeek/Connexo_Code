package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.mdc.channels.inbound.EIWebConnectionType;
import com.energyict.mdc.protocol.inbound.crypto.MD5Seed;
import com.energyict.mdc.protocol.inbound.crypto.ServerCryptographer;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.security.SecurityProperty;

import com.energyict.protocol.exceptions.CommunicationException;
import com.energyict.protocolimpl.properties.TypedProperties;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;

import java.util.List;

/**
 * Provides an implementation for the {@link ServerCryptographer}
 * that currently only supports the {@link EIWebBulk}
 * protocol as all of the code was extracted from it.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (14:42)
 */
public class EIWebCryptographer implements ServerCryptographer {

    /**
     * The name of the password property of the {@link EIWebBulk}
     * protocol that is however coded in the protocol module and can
     * therefore not be referenced here.
     */
    private static final String EIWEB_PROTOCOL_PASSWORD_PROPERTY_NAME = SecurityPropertySpecName.PASSWORD.toString();

    private final InboundDiscoveryContext inboundDiscoveryContext;
    private int usageCount = 0;

    public EIWebCryptographer(InboundDiscoveryContext inboundDiscoveryContext) {
        this.inboundDiscoveryContext = inboundDiscoveryContext;
    }

    @Override
    public MD5Seed buildMD5Seed(DeviceIdentifier deviceIdentifier, String source) {
        this.usageCount++;
        TypedProperties connectionTypeProperties =
                this.inboundDiscoveryContext
                        .getConnectionTypeProperties(deviceIdentifier)
                        .map(TypedProperties::copyOf)
                        .orElseThrow(() -> CommunicationException.notConfiguredForInboundCommunication(deviceIdentifier));
        List<SecurityProperty> securityProperties =
                this.inboundDiscoveryContext
                        .getProtocolSecurityProperties(deviceIdentifier)
                        .orElseThrow(() -> CommunicationException.notConfiguredForInboundCommunication(deviceIdentifier));
        String encryptionPassword = this.getEncryptionPassword(securityProperties);
        String macAddress = connectionTypeProperties.getStringProperty(EIWebConnectionType.MAC_ADDRESS_PROPERTY_NAME);
        String md5SeedBuilder = source + macAddress + encryptionPassword;
        return new StringBasedMD5Seed(md5SeedBuilder);
    }

    private String getEncryptionPassword(List<SecurityProperty> securityProperties) {
        for (SecurityProperty securityProperty : securityProperties) {
            if (EIWEB_PROTOCOL_PASSWORD_PROPERTY_NAME.equals(securityProperty.getName())) {
                return (String) securityProperty.getValue();
            }
        }
        return null;
    }

    @Override
    public boolean wasUsed() {
        return this.usageCount > 0;
    }

}