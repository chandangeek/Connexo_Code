/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.mdc.channels.inbound.EIWebConnectionType;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import com.energyict.protocol.exception.CommunicationException;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.security.SecurityPropertySpecTranslationKeys;

/**
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (14:42)
 */
public class EIWebCryptographer {

    /**
     * The name of the password property of the {@link EIWebBulk}
     * protocol that is however coded in the protocol module and can
     * therefore not be referenced here.
     */
    private static final String EIWEB_PROTOCOL_PASSWORD_PROPERTY_NAME = SecurityPropertySpecTranslationKeys.PASSWORD.toString();

    private final InboundDiscoveryContext inboundDiscoveryContext;
    private int usageCount = 0;

    public EIWebCryptographer(InboundDiscoveryContext inboundDiscoveryContext) {
        this.inboundDiscoveryContext = inboundDiscoveryContext;
    }

    public StringBasedMD5Seed buildMD5Seed(DeviceIdentifier deviceIdentifier, String source) {
        this.usageCount++;
        TypedProperties connectionTypeProperties =
                this.inboundDiscoveryContext
                        .getConnectionTypeProperties(deviceIdentifier)
                        .map(TypedProperties::copyOf)
                        .orElseThrow(() -> CommunicationException.notConfiguredForInboundCommunication(deviceIdentifier));
        TypedProperties securityProperties = this.inboundDiscoveryContext
                .getDeviceProtocolSecurityPropertySet(deviceIdentifier)
                .map(DeviceProtocolSecurityPropertySet::getSecurityProperties)
                .map(TypedProperties::copyOf)
                .orElseThrow(() -> CommunicationException.notConfiguredForInboundCommunication(deviceIdentifier));

        String encryptionPassword = securityProperties.getStringProperty(EIWEB_PROTOCOL_PASSWORD_PROPERTY_NAME);
        String macAddress = connectionTypeProperties.getStringProperty(EIWebConnectionType.MAC_ADDRESS_PROPERTY_NAME);
        String md5SeedBuilder = source + macAddress + encryptionPassword;
        return new StringBasedMD5Seed(md5SeedBuilder);
    }

    public boolean wasUsed() {
        return this.usageCount > 0;
    }
}
