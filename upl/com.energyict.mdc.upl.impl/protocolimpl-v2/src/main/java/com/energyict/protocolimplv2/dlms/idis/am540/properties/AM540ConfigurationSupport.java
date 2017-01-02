package com.energyict.protocolimplv2.dlms.idis.am540.properties;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.dlms.g3.G3Properties;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.dlms.g3.properties.AS330DConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.am130.properties.AM130ConfigurationSupport;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * @author sva
 * @since 11/08/2015 - 15:15
 */
public class AM540ConfigurationSupport extends AM130ConfigurationSupport {

    public static final String AARQ_TIMEOUT_PROPERTY = "AARQTimeout";
    public static final String AARQ_RETRIES_PROPERTY = "AARQRetries";
    public static final String USE_EQUIPMENT_IDENTIFIER_AS_SERIAL = "UseEquipmentIdentifierAsSerialNumber";
    public static final String POLLING_DELAY = "PollingDelay";
    public static final String INITIAL_FRAME_COUNTER = "InitialFrameCounter";
    public static final String USE_METER_IN_TRANSPARENT_MODE = "UseMeterInTransparentMode";
    public static final String TRANSP_CONNECT_TIME = "TransparentConnectTime";
    public static final String PASSWORD = "Password";
    public static final String METER_SECURITY_LEVEL = "SecurityLevel";
    public static final String REQUEST_AUTHENTICATED_FRAME_COUNTER = "RequestAuthenticatedFrameCounter";
    public static final String USE_CACHED_FRAME_COUNTER = "UseCachedFrameCounter";
    public static final String VALIDATE_CACHED_FRAMECOUNTER = "ValidateCachedFrameCounterAndFallback";
    public static final String FRAME_COUNTER_RECOVERY_RETRIES = "FrameCounterRecoveryRetries";
    public static final String FRAME_COUNTER_RECOVERY_STEP = "FrameCounterRecoveryStep";
    public static final String DEFAULT_TRANSPARENT_PASSWORD = "00000000";
    public static final String DEFAULT_TRANSPARENT_SECURITY_LEVEL = "1:0";

    public static final boolean USE_EQUIPMENT_IDENTIFIER_AS_SERIAL_DEFAULT_VALUE = false;
    public static final BigDecimal DEFAULT_SERVER_LOWER_MAC_ADDRESS = BigDecimal.valueOf(17);
    public static final Duration DEFAULT_NOT_USED_AARQ_TIMEOUT = Duration.ofSeconds(0);

    public AM540ConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                this.forcedDelayPropertySpec(),
                this.maxRecPduSizePropertySpec(),
                this.timeZonePropertySpec(),
                this.validateInvokeIdPropertySpec(),
                this.limitMaxNrOfDaysPropertySpec(),
                this.readCachePropertySpec(),
                this.callingAPTitlePropertySpec(),
                this.callHomeIdPropertySpec(),
                this.serverLowerMacAddressPropertySpec(),
                this.mirrorLogicalDeviceIdPropertySpec(),
                this.actualLogicalDeviceIdPropertySpec(),
                this.nodeAddressPropertySpec(),
                this.pskPropertySpec(),
                this.useEquipmentIdentifierAsSerialNumberPropertySpec(),
                this.aarqTimeoutPropertySpec(),
                this.lastSeenDatePropertySpec(),
                this.aarqRetriesPropertySpec(),
                this.pollingDelayPropertySpec(),
                this.initialFrameCounter(),
                this.useMeterInTransparentMode(),
                this.transparentConnectTime(),
                this.transparentSecurityLevel(),
                this.transparentPassword(),
                this.requestAuthenticatedFrameCounter(),
                this.useCachedFrameCounter(),
                this.validateCachedFrameCounter(),
                this.frameCounterRecoveryRetries(),
                this.frameCounterRecoveryStep()
        );
    }

    private PropertySpec frameCounterRecoveryRetries() {
        return UPLPropertySpecFactory.bigDecimal(AM540ConfigurationSupport.FRAME_COUNTER_RECOVERY_RETRIES, false, BigDecimal.valueOf(100));
    }

    private PropertySpec frameCounterRecoveryStep() {
        return UPLPropertySpecFactory.bigDecimal(AM540ConfigurationSupport.FRAME_COUNTER_RECOVERY_STEP, false, BigDecimal.ONE);
    }

    private PropertySpec validateCachedFrameCounter() {
        return UPLPropertySpecFactory.booleanValue(AM540ConfigurationSupport.VALIDATE_CACHED_FRAMECOUNTER, false);
    }

    private PropertySpec useCachedFrameCounter() {
        return UPLPropertySpecFactory.booleanValue(AM540ConfigurationSupport.USE_CACHED_FRAME_COUNTER, false);
    }

    private PropertySpec requestAuthenticatedFrameCounter() {
        return UPLPropertySpecFactory.booleanValue(AM540ConfigurationSupport.REQUEST_AUTHENTICATED_FRAME_COUNTER, false);
    }

    private PropertySpec lastSeenDatePropertySpec() {
        return UPLPropertySpecFactory.bigDecimal(G3Properties.PROP_LASTSEENDATE, false);
    }

    private PropertySpec pollingDelayPropertySpec() {
        return UPLPropertySpecFactory.duration(POLLING_DELAY, false, Duration.ofSeconds(0));
    }

    private PropertySpec pskPropertySpec() {
        return UPLPropertySpecFactory.hexString(G3Properties.PSK, false);
    }

    private PropertySpec nodeAddressPropertySpec() {
        return UPLPropertySpecFactory.bigDecimal(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName(), false);
    }

    private PropertySpec serverLowerMacAddressPropertySpec() {
        return UPLPropertySpecFactory.bigDecimal(DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS, false, DEFAULT_SERVER_LOWER_MAC_ADDRESS);
    }

    private PropertySpec mirrorLogicalDeviceIdPropertySpec() {
        return UPLPropertySpecFactory.bigDecimal(AS330DConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID, false);
    }

    private PropertySpec actualLogicalDeviceIdPropertySpec() {
        return UPLPropertySpecFactory.bigDecimal(AS330DConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID, false);
    }

    private PropertySpec useEquipmentIdentifierAsSerialNumberPropertySpec() {
        return UPLPropertySpecFactory.booleanValue(USE_EQUIPMENT_IDENTIFIER_AS_SERIAL, false, USE_EQUIPMENT_IDENTIFIER_AS_SERIAL_DEFAULT_VALUE);
    }

    private PropertySpec aarqTimeoutPropertySpec() {
        return UPLPropertySpecFactory.duration(AARQ_TIMEOUT_PROPERTY, false, DEFAULT_NOT_USED_AARQ_TIMEOUT);
    }

    private PropertySpec aarqRetriesPropertySpec() {
        return UPLPropertySpecFactory.bigDecimal(AARQ_RETRIES_PROPERTY, false, BigDecimal.valueOf(2));
    }

    private PropertySpec initialFrameCounter() {
        return UPLPropertySpecFactory.positiveBigDecimal(INITIAL_FRAME_COUNTER, false);
    }

    private PropertySpec useMeterInTransparentMode() {
        return UPLPropertySpecFactory.booleanValue(USE_METER_IN_TRANSPARENT_MODE, false, false);
    }

    private PropertySpec transparentConnectTime() {
        return UPLPropertySpecFactory.bigDecimal(TRANSP_CONNECT_TIME, false, BigDecimal.valueOf(10));
    }

    private PropertySpec transparentPassword() {
        return UPLPropertySpecFactory.string(PASSWORD, false, DEFAULT_TRANSPARENT_PASSWORD);
    }

    private PropertySpec transparentSecurityLevel() {
        return UPLPropertySpecFactory.string(METER_SECURITY_LEVEL, false, DEFAULT_TRANSPARENT_SECURITY_LEVEL);
    }

}