package com.energyict.protocolimplv2.dlms.idis.am540.properties;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
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
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
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
        return this.bigDecimalSpec(AM540ConfigurationSupport.FRAME_COUNTER_RECOVERY_RETRIES, false, BigDecimal.valueOf(100));
    }

    private PropertySpec frameCounterRecoveryStep() {
        return this.bigDecimalSpec(AM540ConfigurationSupport.FRAME_COUNTER_RECOVERY_STEP, false, BigDecimal.ONE);
    }

    private PropertySpec validateCachedFrameCounter() {
        return UPLPropertySpecFactory.specBuilder(AM540ConfigurationSupport.VALIDATE_CACHED_FRAMECOUNTER, false, getPropertySpecService()::booleanSpec).finish();
    }

    private PropertySpec useCachedFrameCounter() {
        return UPLPropertySpecFactory.specBuilder(AM540ConfigurationSupport.USE_CACHED_FRAME_COUNTER, false, getPropertySpecService()::booleanSpec).finish();
    }

    private PropertySpec requestAuthenticatedFrameCounter() {
        return UPLPropertySpecFactory.specBuilder(AM540ConfigurationSupport.REQUEST_AUTHENTICATED_FRAME_COUNTER, false, getPropertySpecService()::booleanSpec).finish();
    }

    private PropertySpec lastSeenDatePropertySpec() {
        return UPLPropertySpecFactory.specBuilder(G3Properties.PROP_LASTSEENDATE, false, getPropertySpecService()::bigDecimalSpec).finish();
    }

    private PropertySpec pollingDelayPropertySpec() {
        return this.durationSpec(POLLING_DELAY, false, Duration.ofSeconds(0));
    }

    private PropertySpec pskPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(G3Properties.PSK, false, this.getPropertySpecService()::hexStringSpec).finish();
    }

    private PropertySpec nodeAddressPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName(), false, this.getPropertySpecService()::bigDecimalSpec).finish();
    }

    private PropertySpec serverLowerMacAddressPropertySpec() {
        return this.bigDecimalSpec(DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS, false, DEFAULT_SERVER_LOWER_MAC_ADDRESS);
    }

    private PropertySpec mirrorLogicalDeviceIdPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(AS330DConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID, false, this.getPropertySpecService()::bigDecimalSpec).finish();
    }

    private PropertySpec actualLogicalDeviceIdPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(AS330DConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID, false, this.getPropertySpecService()::bigDecimalSpec).finish();
    }

    private PropertySpec useEquipmentIdentifierAsSerialNumberPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(USE_EQUIPMENT_IDENTIFIER_AS_SERIAL, false, this.getPropertySpecService()::booleanSpec)
                .setDefaultValue(USE_EQUIPMENT_IDENTIFIER_AS_SERIAL_DEFAULT_VALUE)
                .finish();
    }

    private PropertySpec aarqTimeoutPropertySpec() {
        return this.durationSpec(AARQ_TIMEOUT_PROPERTY, false, DEFAULT_NOT_USED_AARQ_TIMEOUT);
    }

    private PropertySpec aarqRetriesPropertySpec() {
        return this.bigDecimalSpec(AARQ_RETRIES_PROPERTY, false, BigDecimal.valueOf(2));
    }

    private PropertySpec initialFrameCounter() {
        return UPLPropertySpecFactory.specBuilder(INITIAL_FRAME_COUNTER, false, this.getPropertySpecService()::positiveBigDecimalSpec).finish();
    }

    private PropertySpec useMeterInTransparentMode() {
        return UPLPropertySpecFactory.specBuilder(USE_METER_IN_TRANSPARENT_MODE, false, this.getPropertySpecService()::booleanSpec)
                .setDefaultValue(false)
                .finish();
    }

    private PropertySpec transparentConnectTime() {
        return this.bigDecimalSpec(TRANSP_CONNECT_TIME, false, BigDecimal.valueOf(10));
    }

    private PropertySpec transparentPassword() {
        return UPLPropertySpecFactory.specBuilder(PASSWORD, false, this.getPropertySpecService()::stringSpec)
                .setDefaultValue(DEFAULT_TRANSPARENT_PASSWORD)
                .finish();
    }

    private PropertySpec transparentSecurityLevel() {
        return UPLPropertySpecFactory.specBuilder(METER_SECURITY_LEVEL, false, this.getPropertySpecService()::stringSpec)
                .setDefaultValue(DEFAULT_TRANSPARENT_SECURITY_LEVEL)
                .finish();
    }

    private PropertySpec bigDecimalSpec(String name, boolean required, BigDecimal defaultValue, BigDecimal... validValues) {
        PropertySpecBuilder<BigDecimal> specBuilder = UPLPropertySpecFactory.specBuilder(name, required, getPropertySpecService()::bigDecimalSpec);
        specBuilder.setDefaultValue(defaultValue);
        specBuilder.addValues(validValues);
        if (validValues.length > 0) {
            specBuilder.markExhaustive();
        }
        return specBuilder.finish();
    }

    private PropertySpec durationSpec(String name, boolean required, Duration defaultValue) {
        PropertySpecBuilder<Duration> durationPropertySpecBuilder = UPLPropertySpecFactory.specBuilder(name, required, this.getPropertySpecService()::durationSpec);
        durationPropertySpecBuilder.setDefaultValue(defaultValue);
        return durationPropertySpecBuilder.finish();
    }
}