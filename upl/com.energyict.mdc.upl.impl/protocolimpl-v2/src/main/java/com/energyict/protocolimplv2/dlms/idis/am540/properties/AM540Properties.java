package com.energyict.protocolimplv2.dlms.idis.am540.properties;

import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dlms.g3.properties.AS330DConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.am130.properties.IDISSecurityProvider;
import com.energyict.protocolimplv2.dlms.idis.am500.properties.IDISProperties;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;

import java.math.BigDecimal;

import static com.energyict.dlms.common.DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS;

/**
 * @author sva
 * @since 11/08/2015 - 15:04
 */
public class AM540Properties extends IDISProperties {

    @Override
    public SecurityProvider getSecurityProvider() {
        if (securityProvider == null) {
            securityProvider = new IDISSecurityProvider(getProperties(), getSecurityPropertySet().getAuthenticationDeviceAccessLevel());
        }
        return securityProvider;
    }

    @Override
    public int getServerLowerMacAddress() {
        return parseBigDecimalProperty(SERVER_LOWER_MAC_ADDRESS, AM540ConfigurationSupport.DEFAULT_SERVER_LOWER_MAC_ADDRESS);
    }

    @Override
    public int getServerUpperMacAddress() {
        if (useBeaconMirrorDeviceDialect()) {
            return getMirrorLogicalDeviceId();  // The Beacon mirrored device
        } else if (useBeaconGatewayDeviceDialect()) {
            return getGatewayLogicalDeviceId(); // Beacon acts as a gateway
        } else {
            return getNodeAddress();            // Classic G3 gateway
        }
    }

    private int getMirrorLogicalDeviceId() {
        return parseBigDecimalProperty(AS330DConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID);
    }

    private int getGatewayLogicalDeviceId() {
        return parseBigDecimalProperty(AS330DConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID);
    }

    public int getNodeAddress() {
        return parseBigDecimalProperty(MeterProtocol.NODEID);
    }

    public boolean useEquipmentIdentifierAsSerialNumber() {
        return getProperties().getTypedProperty(AM540ConfigurationSupport.USE_EQUIPMENT_IDENTIFIER_AS_SERIAL, AM540ConfigurationSupport.USE_EQUIPMENT_IDENTIFIER_AS_SERIAL_DEFAULT_VALUE);
    }

    @Override
    public int getClientMacAddress() {
        if (useBeaconMirrorDeviceDialect()) {
            return BigDecimal.ONE.intValue();   // When talking to the Beacon mirrored device, we should always use client address 1
        } else {
            return parseBigDecimalProperty(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString(), BigDecimal.ONE);
        }
    }

    public boolean useBeaconMirrorDeviceDialect() {
        String dialectName = getProperties().getStringProperty(DeviceProtocolDialect.DEVICE_PROTOCOL_DIALECT_NAME);
        return dialectName != null && dialectName.equals(DeviceProtocolDialectNameEnum.BEACON_MIRROR_TCP_DLMS_PROTOCOL_DIALECT_NAME.getName());
    }

    public boolean useBeaconGatewayDeviceDialect() {
        String dialectName = getProperties().getStringProperty(DeviceProtocolDialect.DEVICE_PROTOCOL_DIALECT_NAME);
        return dialectName != null && dialectName.equals(DeviceProtocolDialectNameEnum.BEACON_GATEWAY_TCP_DLMS_PROTOCOL_DIALECT_NAME.getName());
    }
}