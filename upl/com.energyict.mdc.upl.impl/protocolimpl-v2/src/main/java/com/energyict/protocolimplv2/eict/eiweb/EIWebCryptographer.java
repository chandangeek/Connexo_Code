package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.channels.inbound.EIWebConnectionType;
import com.energyict.mdc.ports.InboundComPort;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.inbound.InboundDAO;
import com.energyict.mdc.protocol.inbound.crypto.MD5Seed;
import com.energyict.mdc.protocol.inbound.crypto.ServerCryptographer;
import com.energyict.mdc.protocol.security.SecurityProperty;
import com.energyict.protocolimplv2.MdcManager;
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

    private InboundDAO inboundDAO;
    private InboundComPort comPort;
    private int usageCount = 0;

    public EIWebCryptographer(InboundDAO inboundDAO, InboundComPort comPort) {
        super();
        this.inboundDAO = inboundDAO;
        this.comPort = comPort;
    }

    @Override
    public MD5Seed buildMD5Seed(DeviceIdentifier deviceIdentifier, String source) {
        this.usageCount++;
        TypedProperties connectionTypeProperties = this.inboundDAO.getDeviceConnectionTypeProperties(deviceIdentifier, this.comPort);
        if (connectionTypeProperties == null) {
            throw MdcManager.getComServerExceptionFactory().notConfiguredForInboundCommunication(deviceIdentifier);
        } else {
            List<SecurityProperty> securityProperties = this.inboundDAO.getDeviceProtocolSecurityProperties(deviceIdentifier, this.comPort);
            if (securityProperties != null) {
                String encryptionPassword = this.getEncryptionPassword(securityProperties);
                String macAddress = connectionTypeProperties.getStringProperty(EIWebConnectionType.MAC_ADDRESS_PROPERTY_NAME);
                StringBuilder md5SeedBuilder = new StringBuilder(source);
                md5SeedBuilder.append(macAddress);
                md5SeedBuilder.append(encryptionPassword);
                return new StringBasedMD5Seed(md5SeedBuilder.toString());
            } else {
                throw MdcManager.getComServerExceptionFactory().notConfiguredForInboundCommunication(deviceIdentifier);
            }
        }
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