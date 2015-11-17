package com.energyict.protocolimplv2.nta.dsmr50.elster.am540;

import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.g3.properties.AS330DConfigurationSupport;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;
import com.energyict.protocolimplv2.nta.dsmr50.Dsmr50ConfigurationSupport;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;

import java.math.BigDecimal;

import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_UPPER_SERVER_MAC_ADDRESS;

/**
 * Extension of the standard DLMS properties, adding DSMR50 stuff
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 17/12/2014 - 17:27
 */
public class Dsmr50Properties extends DlmsProperties {

    public static final String AARQ_TIMEOUT_PROPERTY = "AARQTimeout";
    public static final String AARQ_RETRIES_PROPERTY = "AARQRetries";
    public static final String READCACHE_PROPERTY = "ReadCache";
    public static final String CumulativeCaptureTimeChannel = "CumulativeCaptureTimeChannel";

    private static final int PUBLIC_CLIENT_MAC_ADDRESS = 16;

    /**
     * Property indicating to read the cache out (useful because there's no config change state)
     */
    public boolean isReadCache() {
        return getProperties().getTypedProperty(READCACHE_PROPERTY, false);
    }

    public boolean isCumulativeCaptureTimeChannel() {
        return getProperties().getTypedProperty(CumulativeCaptureTimeChannel, false);
    }

    public boolean isIgnoreDstStatusCode() {
        return getProperties().getTypedProperty(Dsmr50ConfigurationSupport.PROPERTY_IGNORE_DST_STATUS_CODE, false);
    }

    public long getAARQTimeout() {
        return getProperties().getTypedProperty(AARQ_TIMEOUT_PROPERTY, BigDecimal.ZERO).longValue();
    }

    public int getAARQRetries() {
        return getProperties().getTypedProperty(AARQ_RETRIES_PROPERTY, BigDecimal.valueOf(2)).intValue();
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
        final int logicalDeviceId = parseBigDecimalProperty(AS330DConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID);
        if (logicalDeviceId == -1) {
            throw MdcManager.getComServerExceptionFactory().createInvalidPropertyFormatException(AS330DConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID, "-1", "Should be a number greater than 0");
        }
        return logicalDeviceId;
    }

    private int getGatewayLogicalDeviceId() {
        final int logicalDeviceId = parseBigDecimalProperty(AS330DConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID);
        if (logicalDeviceId == -1) {
            throw MdcManager.getComServerExceptionFactory().createInvalidPropertyFormatException(AS330DConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID, "-1", "Should be a number greater than 0");
        }
        return logicalDeviceId;
    }

    public int getNodeAddress() {
        Object nodeAddressObject = getProperties().getTypedProperty(MeterProtocol.NODEID);
        if (nodeAddressObject == null) {
            return DEFAULT_UPPER_SERVER_MAC_ADDRESS.intValue();
        } else if (nodeAddressObject instanceof BigDecimal) {
            return ((BigDecimal) nodeAddressObject).intValue();
        } else {
            try {
                return Integer.parseInt((String) nodeAddressObject);
            } catch (NumberFormatException e) {
                return DEFAULT_UPPER_SERVER_MAC_ADDRESS.intValue();
            }
        }
    }

    @Override
    public int getClientMacAddress() {
        if (useBeaconMirrorDeviceDialect() && !usesPublicClient()) {
            return BigDecimal.ONE.intValue();   // When talking to the Beacon mirrored device, we should always use client address 1 (except for the public client, which is always 16)
        } else {
            return parseBigDecimalProperty(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString(), BigDecimal.ONE);
        }
    }

    public boolean usesPublicClient() {
        return parseBigDecimalProperty(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString(), BigDecimal.ONE) == PUBLIC_CLIENT_MAC_ADDRESS;
    }

    public boolean useBeaconMirrorDeviceDialect() {
        String dialectName = getProperties().getStringProperty(DeviceProtocolDialect.DEVICE_PROTOCOL_DIALECT_NAME);
        return dialectName != null && dialectName.equals(DeviceProtocolDialectNameEnum.BEACON_MIRROR_TCP_DLMS_PROTOCOL_DIALECT_NAME.getName());
    }

    public boolean useBeaconGatewayDeviceDialect() {
        String dialectName = getProperties().getStringProperty(DeviceProtocolDialect.DEVICE_PROTOCOL_DIALECT_NAME);
        return dialectName != null && dialectName.equals(DeviceProtocolDialectNameEnum.BEACON_GATEWAY_TCP_DLMS_PROTOCOL_DIALECT_NAME.getName());
    }

    /**
     * In case of non-polling for TCP IP communication, frames that have a wrong WPDU source or destination will be fully read & ignored.
     * After that, the connection layer will attempt to read out the next full frame, so the normal protocol sequence can continue.
     */
    @Override
    public boolean isUsePolling() {
        return false;
    }

    public boolean getCheckNumberOfBlocksDuringFirmwareResume() {
        return getProperties().getTypedProperty(Dsmr50ConfigurationSupport.CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME, Dsmr50ConfigurationSupport.DEFAULT_CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME);
    }

    public boolean useEquipmentIdentifierAsSerialNumber() {
        return getProperties().getTypedProperty(Dsmr50ConfigurationSupport.USE_EQUIPMENT_IDENTIFIER_AS_SERIAL, Dsmr50ConfigurationSupport.USE_EQUIPMENT_IDENTIFIER_AS_SERIAL_DEFAULT_VALUE);
    }
}