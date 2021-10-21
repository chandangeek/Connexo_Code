package com.energyict.protocolimplv2.dlms.idis.AS3000G.properties;

import com.energyict.dlms.CipheringType;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocolimplv2.DeviceProtocolDialectTranslationKeys;
import com.energyict.protocolimplv2.dlms.g3.properties.AS330DConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.am130.properties.IDISSecurityProvider;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540Properties;

import java.math.BigDecimal;
import java.time.Duration;

import static com.energyict.dlms.common.DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS;
import static com.energyict.dlms.common.DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS;
import static com.energyict.mdc.upl.DeviceProtocolDialect.Property.DEVICE_PROTOCOL_DIALECT;


public class AS3000GProperties extends AM540Properties {

    private static final int PUBLIC_CLIENT_MAC_ADDRESS = 16;

    public AS3000GProperties(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if (securityProvider == null) {
            securityProvider = new IDISSecurityProvider(getProperties(), getSecurityPropertySet().getAuthenticationDeviceAccessLevel(), DLMSConnectionException.REASON_CONTINUE_INVALID_FRAMECOUNTER);
        }
        return securityProvider;
    }

    @Override
    public ConformanceBlock getConformanceBlock() {
        ConformanceBlock conformanceBlock = super.getConformanceBlock();

        conformanceBlock.setGeneralBlockTransfer(useGeneralBlockTransfer());
        conformanceBlock.setGeneralProtection(getCipheringType().equals(CipheringType.GENERAL_DEDICATED) || getCipheringType().equals(CipheringType.GENERAL_GLOBAL));

        conformanceBlock.setGeneralProtection(true);
        conformanceBlock.setAccess(true);
        conformanceBlock.setDataNotification(true);
        conformanceBlock.setAction(true);
        conformanceBlock.setPriorityManagementSupported(false);
        conformanceBlock.setEventNotification(false);

        return conformanceBlock;
    }

    /**
     * The AM540 protocol will also run embedded in the Beacon3100, so by default: avoid polling on the inputstream
     */
    @Override
    public Duration getPollingDelay() {
        return getProperties().getTypedProperty(AS3000GConfigurationSupport.POLLING_DELAY, Duration.ofSeconds(0));
    }

    @Override
    public int getServerLowerMacAddress() {
        return parseBigDecimalProperty(SERVER_LOWER_MAC_ADDRESS, AS3000GConfigurationSupport.DEFAULT_SERVER_LOWER_MAC_ADDRESS);
    }

    @Override
    public int getServerUpperMacAddress() {
        return parseBigDecimalProperty(SERVER_UPPER_MAC_ADDRESS, AS3000GConfigurationSupport.DEFAULT_UPPER_SERVER_MAC_ADDRESS);
    }

    protected int getMirrorLogicalDeviceId() {
        final int logicalDeviceId = parseBigDecimalProperty(AS330DConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID);
        if (logicalDeviceId == -1) {
            throw DeviceConfigurationException.invalidPropertyFormat(AS330DConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID, "-1", "Should be a number greater than 0");
        }
        return logicalDeviceId;
    }

    protected int getGatewayLogicalDeviceId() {
        final int logicalDeviceId = parseBigDecimalProperty(AS330DConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID);
        if (logicalDeviceId == -1) {
            throw DeviceConfigurationException.invalidPropertyFormat(AS330DConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID, "-1", "Should be a number greater than 0");
        }
        return logicalDeviceId;
    }

    public int getNodeAddress() {
        return parseBigDecimalProperty(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName());
    }

    /**
     * False by default, to return the serial number of the connected e-meter
     */
    @Override
    public boolean useEquipmentIdentifierAsSerialNumber() {
        return getProperties().getTypedProperty(AS3000GConfigurationSupport.USE_EQUIPMENT_IDENTIFIER_AS_SERIAL, AS3000GConfigurationSupport.USE_EQUIPMENT_IDENTIFIER_AS_SERIAL_DEFAULT_VALUE);
    }

    public boolean usesPublicClient() {
        return getClientMacAddress() == PUBLIC_CLIENT_MAC_ADDRESS;
    }

    public boolean useSerialDialect() {
        String dialectName = getProperties().getStringProperty(DEVICE_PROTOCOL_DIALECT.getName());
        return dialectName != null && dialectName.equals(DeviceProtocolDialectTranslationKeys.SERIAL_DLMS_PROTOCOL_DIALECT_NAME.getName());
    }

    public int getAARQRetries() {
        return getProperties().getTypedProperty(AS3000GConfigurationSupport.AARQ_RETRIES_PROPERTY, BigDecimal.valueOf(2)).intValue();
    }

    public long getAARQTimeout() {
        return getProperties().getTypedProperty(AS3000GConfigurationSupport.AARQ_TIMEOUT_PROPERTY, AS3000GConfigurationSupport.DEFAULT_NOT_USED_AARQ_TIMEOUT).toMillis();
    }

    @Override
    public boolean timeoutMeansBrokenConnection() {
        return true;
    }

    public boolean getRequestAuthenticatedFrameCounter() {
        return getProperties().getTypedProperty(AS3000GConfigurationSupport.REQUEST_AUTHENTICATED_FRAME_COUNTER, false);
    }

    public boolean useCachedFrameCounter() {
        return getProperties().getTypedProperty(AS3000GConfigurationSupport.USE_CACHED_FRAME_COUNTER, false);
    }

    public boolean validateCachedFrameCounter() {
        return getProperties().getTypedProperty(AS3000GConfigurationSupport.VALIDATE_CACHED_FRAMECOUNTER, true);
    }

    public int getFrameCounterRecoveryRetries() {
        return getProperties().getTypedProperty(AS3000GConfigurationSupport.FRAME_COUNTER_RECOVERY_RETRIES, BigDecimal.valueOf(100)).intValue();
    }

    public int getFrameCounterRecoveryStep() {
        return getProperties().getTypedProperty(AS3000GConfigurationSupport.FRAME_COUNTER_RECOVERY_STEP, BigDecimal.ONE).intValue();
    }

    public long getInitialFrameCounter() {
        return getProperties().getTypedProperty(AS3000GConfigurationSupport.INITIAL_FRAME_COUNTER, BigDecimal.valueOf(100)).longValue();
    }
}