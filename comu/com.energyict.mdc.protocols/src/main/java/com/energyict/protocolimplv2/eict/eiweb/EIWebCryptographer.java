package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.protocol.api.crypto.Cryptographer;
import com.energyict.mdc.protocol.api.crypto.MD5Seed;
import com.energyict.mdc.protocol.api.inbound.InboundDiscoveryContext;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;
import com.energyict.protocols.impl.channels.inbound.EIWebConnectionProperties;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import java.util.List;

/**
 * Provides an implementation for the {@link Cryptographer}
 * that currently only supports the {@link EIWebBulk}
 * protocol as all of the code was extracted from it.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (14:42)
 */
public class EIWebCryptographer implements Cryptographer {

    /**
     * The name of the password property of the {@link EIWebBulk}
     * protocol that is however coded in the protocol module and can
     * therefore not be referenced here.
     */
    private static final String EIWEB_PROTOCOL_PASSWORD_PROPERTY_NAME = SecurityPropertySpecName.PASSWORD.getKey();

    private InboundDiscoveryContext inboundDiscoveryContext;
    private int usageCount = 0;

    public EIWebCryptographer (InboundDiscoveryContext inboundDiscoveryContext) {
        super();
        this.inboundDiscoveryContext = inboundDiscoveryContext;
    }

    @Override
    public MD5Seed buildMD5Seed (DeviceIdentifier deviceIdentifier, String source) {
        this.usageCount++;
        TypedProperties connectionTypeProperties = this.inboundDiscoveryContext.getDeviceConnectionTypeProperties(deviceIdentifier);
        if (connectionTypeProperties == null) {
            throw new CommunicationException(MessageSeeds.NOT_CONFIGURED_FOR_INBOUND_COMMUNICATION,  deviceIdentifier);
        }
        else {
            List<SecurityProperty> securityProperties = this.inboundDiscoveryContext.getDeviceProtocolSecurityProperties(deviceIdentifier);
            if (securityProperties != null) {
                String encryptionPassword = this.getEncryptionPassword(securityProperties);
                String macAddress = connectionTypeProperties.getStringProperty(EIWebConnectionProperties.Fields.MAC_ADDRESS.propertySpecName());
                return new StringBasedMD5Seed(source + macAddress + encryptionPassword);
            }
            else {
                throw new CommunicationException(MessageSeeds.NOT_CONFIGURED_FOR_INBOUND_COMMUNICATION,  deviceIdentifier);
            }
        }
    }

    private String getEncryptionPassword (List<SecurityProperty> securityProperties) {
        for (SecurityProperty securityProperty : securityProperties) {
            if (EIWEB_PROTOCOL_PASSWORD_PROPERTY_NAME.equals(securityProperty.getName())) {
                return (String) securityProperty.getValue();
            }
        }
        return null;
    }

    @Override
    public boolean wasUsed () {
        return this.usageCount > 0;
    }

}