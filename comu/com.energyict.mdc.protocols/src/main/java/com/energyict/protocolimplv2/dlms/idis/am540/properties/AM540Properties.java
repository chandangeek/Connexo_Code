package com.energyict.protocolimplv2.dlms.idis.am540.properties;


import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.exceptions.DeviceConfigurationException;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.protocols.mdc.protocoltasks.MirrorTcpDeviceProtocolDialect;
import com.energyict.protocols.naming.SecurityPropertySpecName;

import com.energyict.dlms.CipheringType;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.protocolimplv2.DeviceProtocolDialectName;
import com.energyict.protocolimplv2.dlms.g3.properties.AS330DConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.am130.properties.IDISSecurityProvider;
import com.energyict.protocolimplv2.dlms.idis.am500.properties.IDISProperties;

import java.math.BigDecimal;

/**
 * @author sva
 * @since 11/08/2015 - 15:04
 */
public class AM540Properties extends IDISProperties {

    private static final int PUBLIC_CLIENT_MAC_ADDRESS = 16;

    public AM540Properties(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
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

    @Override
    public boolean isBulkRequest() {
        return true;
    }

    /**
     * The AM540 protocol will also run embedded in the Beacon3100, so by default: avoid polling on the inputstream
     */
    @Override
    public TimeDuration getPollingDelay() {
        return getProperties().getTypedProperty(AM540ConfigurationSupport.POLLING_DELAY, new TimeDuration(0));
    }

    @Override
    public int getServerLowerMacAddress() {
        return parseBigDecimalProperty(SERVER_LOWER_MAC_ADDRESS, AM540ConfigurationSupport.DEFAULT_SERVER_LOWER_MAC_ADDRESS);
    }

    @Override
    public int getServerUpperMacAddress() {
//        if (useBeaconMirrorDeviceDialect()) {
//            return getMirrorLogicalDeviceId();  // The Beacon mirrored device
//        } else if (useBeaconGatewayDeviceDialect()) {
        //todo we might need to fix this ...
            return getGatewayLogicalDeviceId(); // Beacon acts as a gateway
//        } else {
//            return getNodeAddress();            // Classic G3 gateway
//        }
    }

    private int getMirrorLogicalDeviceId() {
        final int logicalDeviceId = parseBigDecimalProperty(AS330DConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID);
        if (logicalDeviceId == -1) {
            throw DeviceConfigurationException.invalidPropertyFormat(AS330DConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID, "-1", "Should be a number greater than 0");
        }
        return logicalDeviceId;
    }

    private int getGatewayLogicalDeviceId() {
        final int logicalDeviceId = parseBigDecimalProperty(AS330DConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID);
        if (logicalDeviceId == -1) {
            throw DeviceConfigurationException.invalidPropertyFormat(AS330DConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID, "-1", "Should be a number greater than 0");
        }
        return logicalDeviceId;
    }

    public int getNodeAddress() {
        return parseBigDecimalProperty(MeterProtocol.NODEID);
    }

    /**
     * False by default, to return the serial number of the connected e-meter
     */
    public boolean useEquipmentIdentifierAsSerialNumber() {
        return getProperties().getTypedProperty(AM540ConfigurationSupport.USE_EQUIPMENT_IDENTIFIER_AS_SERIAL, AM540ConfigurationSupport.USE_EQUIPMENT_IDENTIFIER_AS_SERIAL_DEFAULT_VALUE);
    }

    @Override
    public int getClientMacAddress() {
        return parseBigDecimalProperty(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.getKey(), BigDecimal.ONE);
    }

    public boolean usesPublicClient() {
        return parseBigDecimalProperty(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.getKey(), BigDecimal.ONE) == PUBLIC_CLIENT_MAC_ADDRESS;
    }

    public boolean useBeaconMirrorDeviceDialect() {
        String dialectName = getProperties().getStringProperty(DeviceProtocolDialect.DEVICE_PROTOCOL_DIALECT_NAME);
        if (dialectName == null) {
            return false;
        }
        MirrorTcpDeviceProtocolDialect dialect = new MirrorTcpDeviceProtocolDialect(getThesaurus(), getPropertySpecService());
        // for compatibility with ProtocolTester - here the protocol dialect is the "display name"
        return dialect.getDisplayName().equals(dialectName) || dialect.getDeviceProtocolDialectName().equals(dialectName);
    }

    public boolean useBeaconGatewayDeviceDialect() {
        String dialectName = getProperties().getStringProperty(DeviceProtocolDialect.DEVICE_PROTOCOL_DIALECT_NAME);
        return dialectName != null && dialectName.equals(DeviceProtocolDialectName.BEACON_GATEWAY_TCP_DLMS_PROTOCOL.getName());
    }

    public boolean useSerialDialect() {
        String dialectName = getProperties().getStringProperty(DeviceProtocolDialect.DEVICE_PROTOCOL_DIALECT_NAME);
        return dialectName != null && dialectName.equals(DeviceProtocolDialectName.SERIAL_DLMS_PROTOCOL.getName());
    }

    public int getAARQRetries() {
        return getProperties().getTypedProperty(AM540ConfigurationSupport.AARQ_RETRIES_PROPERTY, BigDecimal.valueOf(2)).intValue();
    }

    public long getAARQTimeout() {
        return getProperties().getTypedProperty(AM540ConfigurationSupport.AARQ_TIMEOUT_PROPERTY, new TimeDuration(AM540ConfigurationSupport.NOT_USED_AARQ_TIMEOUT)).getMilliSeconds();
    }

    /**
     * A timeout (lack of response from the AM540) should be handled differently according to the context:
     * - in case of G3 gateway mode, you can still read out the next physical slave devices if one slave device does not reply.
     * - in case of Beacon DC mode (reading out 'mirror' logical devices), a timeout is fatal, the next physical slaves cannot be read out.
     * - in case of a serial connection we should fail on a timeout
     */
    @Override
    public boolean timeoutMeansBrokenConnection() {
        return useBeaconMirrorDeviceDialect() || useSerialDialect();
    }

    public boolean useMeterInTransparentMode() {
        return getProperties().getTypedProperty(AM540ConfigurationSupport.USE_METER_IN_TRANSPARENT_MODE, false);
    }

    public int getTransparentConnectTime() {
        return getProperties().getTypedProperty(AM540ConfigurationSupport.TRANSP_CONNECT_TIME, BigDecimal.valueOf(10)).intValue();
    }

    public String getTransparentPassword() {
        return getProperties().getTypedProperty(AM540ConfigurationSupport.PASSWORD, "00000000");
    }

    public String getTransparentSecurityLevel() {
        return getProperties().getTypedProperty(AM540ConfigurationSupport.METER_SECURITY_LEVEL, "1:0");
    }

    public boolean getRequestAuthenticatedFrameCounter() {
        return getProperties().getTypedProperty(AM540ConfigurationSupport.REQUEST_AUTHENTICATED_FRAME_COUNTER, false);
    }

    public boolean useCachedFrameCounter() {
        return getProperties().getTypedProperty(AM540ConfigurationSupport.USE_CACHED_FRAME_COUNTER, false);
    }
    public boolean validateCachedFrameCounter() {
        return getProperties().getTypedProperty(AM540ConfigurationSupport.VALIDATE_CACHED_FRAMECOUNTER, true);
    }

    public int getFrameCounterRecoveryRetries(){
        return getProperties().getTypedProperty(AM540ConfigurationSupport.FRAME_COUNTER_RECOVERY_RETRIES, BigDecimal.valueOf(100)).intValue();
    }

    public int getFrameCounterRecoveryStep(){
        return getProperties().getTypedProperty(AM540ConfigurationSupport.FRAME_COUNTER_RECOVERY_STEP, BigDecimal.ONE).intValue();
    }

    public BigDecimal getInitialFrameCounter(){
        return getProperties().getTypedProperty(AM540ConfigurationSupport.INITIAL_FRAME_COUNTER);
    }

}