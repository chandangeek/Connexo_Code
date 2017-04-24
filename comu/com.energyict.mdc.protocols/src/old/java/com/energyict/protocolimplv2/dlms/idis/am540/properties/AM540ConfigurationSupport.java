/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms.idis.am540.properties;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;

import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.dlms.g3.G3Properties;
import com.energyict.protocolimplv2.dlms.g3.properties.AS330DConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.am130.properties.AM130ConfigurationSupport;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
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

    public static final boolean USE_EQUIPMENT_IDENTIFIER_AS_SERIAL_DEFAULT_VALUE = false;
    public static final BigDecimal DEFAULT_SERVER_LOWER_MAC_ADDRESS = BigDecimal.valueOf(17);
    public static final int NOT_USED_AARQ_TIMEOUT = 0;

    public AM540ConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
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
        return getPropertySpecService().positiveBigDecimalSpec().named(AM540ConfigurationSupport.FRAME_COUNTER_RECOVERY_RETRIES, AM540ConfigurationSupport.FRAME_COUNTER_RECOVERY_RETRIES)
                .describedAs(AM540ConfigurationSupport.FRAME_COUNTER_RECOVERY_RETRIES).setDefaultValue(BigDecimal.valueOf(100)).finish();
    }

    private PropertySpec frameCounterRecoveryStep() {
        return getPropertySpecService().positiveBigDecimalSpec().named(AM540ConfigurationSupport.FRAME_COUNTER_RECOVERY_STEP, AM540ConfigurationSupport.FRAME_COUNTER_RECOVERY_STEP)
                .describedAs(AM540ConfigurationSupport.FRAME_COUNTER_RECOVERY_STEP).setDefaultValue(BigDecimal.ONE).finish();
    }

    private PropertySpec validateCachedFrameCounter() {
        return getPropertySpecService().booleanSpec().named(AM540ConfigurationSupport.VALIDATE_CACHED_FRAMECOUNTER, AM540ConfigurationSupport.VALIDATE_CACHED_FRAMECOUNTER)
                .describedAs(AM540ConfigurationSupport.VALIDATE_CACHED_FRAMECOUNTER).finish();
    }

    private PropertySpec useCachedFrameCounter() {
        return getPropertySpecService().booleanSpec().named(AM540ConfigurationSupport.USE_CACHED_FRAME_COUNTER, AM540ConfigurationSupport.USE_CACHED_FRAME_COUNTER)
                .describedAs(AM540ConfigurationSupport.USE_CACHED_FRAME_COUNTER).finish();
    }

    private PropertySpec requestAuthenticatedFrameCounter() {
        return getPropertySpecService().booleanSpec().named(AM540ConfigurationSupport.REQUEST_AUTHENTICATED_FRAME_COUNTER, AM540ConfigurationSupport.REQUEST_AUTHENTICATED_FRAME_COUNTER)
                .describedAs(AM540ConfigurationSupport.REQUEST_AUTHENTICATED_FRAME_COUNTER).finish();
    }

    public PropertySpec lastSeenDatePropertySpec() {
        return getPropertySpecService().bigDecimalSpec().named(G3Properties.PROP_LASTSEENDATE, G3Properties.PROP_LASTSEENDATE)
                .describedAs(G3Properties.PROP_LASTSEENDATE).finish();
    }

    private PropertySpec pollingDelayPropertySpec() {
        return getPropertySpecService().timeDurationSpec().named(POLLING_DELAY, POLLING_DELAY)
                .describedAs(POLLING_DELAY).setDefaultValue(new TimeDuration(0)).finish();
    }

    private PropertySpec pskPropertySpec() {
        return getPropertySpecService().hexStringSpec().named(G3Properties.PSK, G3Properties.PSK)
                .describedAs(G3Properties.PSK).finish();
    }

    private PropertySpec nodeAddressPropertySpec() {
        return getPropertySpecService().positiveBigDecimalSpec().named(MeterProtocol.NODEID, MeterProtocol.NODEID)
                .describedAs(MeterProtocol.NODEID)
                .setDefaultValue(BigDecimal.ONE).finish();
    }

    private PropertySpec serverLowerMacAddressPropertySpec() {
        return getPropertySpecService().positiveBigDecimalSpec().named(DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS, DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS)
                .describedAs(DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS).setDefaultValue(DEFAULT_SERVER_LOWER_MAC_ADDRESS).finish();
    }

    public PropertySpec mirrorLogicalDeviceIdPropertySpec() {
        return getPropertySpecService().bigDecimalSpec().named(AS330DConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID, AS330DConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID)
                .describedAs(AS330DConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID).finish();
    }

    public PropertySpec actualLogicalDeviceIdPropertySpec() {
        return getPropertySpecService().bigDecimalSpec().named(AS330DConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID, AS330DConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID)
                .describedAs(AS330DConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID).finish();
    }

    private PropertySpec useEquipmentIdentifierAsSerialNumberPropertySpec() {
        return getPropertySpecService().booleanSpec().named(USE_EQUIPMENT_IDENTIFIER_AS_SERIAL, USE_EQUIPMENT_IDENTIFIER_AS_SERIAL).describedAs(USE_EQUIPMENT_IDENTIFIER_AS_SERIAL)
                .setDefaultValue(USE_EQUIPMENT_IDENTIFIER_AS_SERIAL_DEFAULT_VALUE).finish();
    }

    private PropertySpec aarqTimeoutPropertySpec() {
        return getPropertySpecService().timeDurationSpec().named(AARQ_TIMEOUT_PROPERTY, AARQ_TIMEOUT_PROPERTY).describedAs(AARQ_TIMEOUT_PROPERTY)
                .setDefaultValue(new TimeDuration(NOT_USED_AARQ_TIMEOUT)).finish();
    }

    private PropertySpec aarqRetriesPropertySpec() {
        return getPropertySpecService().positiveBigDecimalSpec().named(AARQ_RETRIES_PROPERTY, AARQ_RETRIES_PROPERTY)
                .describedAs(AARQ_RETRIES_PROPERTY).setDefaultValue(BigDecimal.valueOf(2)).finish();
    }

    private PropertySpec initialFrameCounter() {
        return getPropertySpecService().positiveBigDecimalSpec().named(INITIAL_FRAME_COUNTER, INITIAL_FRAME_COUNTER)
                .describedAs(INITIAL_FRAME_COUNTER).finish();
    }

    private PropertySpec useMeterInTransparentMode() {
        return getPropertySpecService().booleanSpec().named(USE_METER_IN_TRANSPARENT_MODE, USE_METER_IN_TRANSPARENT_MODE).describedAs(USE_METER_IN_TRANSPARENT_MODE)
                .setDefaultValue(false).finish();
    }

    private PropertySpec transparentConnectTime() {
        return getPropertySpecService().positiveBigDecimalSpec().named(TRANSP_CONNECT_TIME, TRANSP_CONNECT_TIME)
                .describedAs(TRANSP_CONNECT_TIME).setDefaultValue(BigDecimal.valueOf(10)).finish();
    }

    private PropertySpec transparentPassword() {
        return getPropertySpecService().stringSpec().named(PASSWORD, PASSWORD).describedAs(PASSWORD).setDefaultValue("00000000").finish();
    }

    private PropertySpec transparentSecurityLevel() {
        return getPropertySpecService().stringSpec().named(METER_SECURITY_LEVEL, METER_SECURITY_LEVEL).describedAs(METER_SECURITY_LEVEL).setDefaultValue("1:0").finish();

    }

}