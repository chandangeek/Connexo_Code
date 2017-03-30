/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms.idis.am130.properties;


import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceProtocolProperty;
import com.energyict.mdc.protocol.api.legacy.dynamic.ConfigurationSupport;

import com.energyict.dlms.CipheringType;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.dlms.idis.IDIS;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.CIPHERING_TYPE;
import static com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.FORCED_DELAY;
import static com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.GBT_WINDOW_SIZE;
import static com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.MAX_REC_PDU_SIZE;
import static com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.READCACHE_PROPERTY;
import static com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.TIMEZONE;
import static com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.USE_GBT;
import static com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.VALIDATE_INVOKE_ID;

public class AM130ConfigurationSupport implements ConfigurationSupport {

    public static final String LIMIT_MAX_NR_OF_DAYS_PROPERTY = "LimitMaxNrOfDays";

    public static final boolean DEFAULT_VALIDATE_INVOKE_ID = true;
    public static final BigDecimal DEFAULT_GBT_WINDOW_SIZE = BigDecimal.valueOf(5);
    public static final boolean USE_GBT_DEFAULT_VALUE = true;
    public static final CipheringType DEFAULT_CIPHERING_TYPE = CipheringType.GENERAL_GLOBAL;
    private final PropertySpecService propertySpecService;

    @Inject
    public AM130ConfigurationSupport(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
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
                this.useGeneralBlockTransferPropertySpec(),
                this.generalBlockTransferWindowSizePropertySpec(),
                this.cipheringTypePropertySpec(),
                this.callingAPTitlePropertySpec(),
                this.serverUpperMacAddressPropertySpec(),
                this.callHomeIdPropertySpec()
        );
    }

    protected PropertySpec serverUpperMacAddressPropertySpec() {
        return propertySpecService.positiveBigDecimalSpec()
                .named(DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS, DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS)
                .describedAs(DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS)
                .setDefaultValue(BigDecimal.ONE)
                .finish();
    }

    protected PropertySpec useGeneralBlockTransferPropertySpec() {
        return propertySpecService.booleanSpec().named(USE_GBT, USE_GBT).describedAs(USE_GBT).markRequired().setDefaultValue(USE_GBT_DEFAULT_VALUE).finish();
    }

    protected PropertySpec generalBlockTransferWindowSizePropertySpec() {
        return propertySpecService.positiveBigDecimalSpec().named(GBT_WINDOW_SIZE, GBT_WINDOW_SIZE).describedAs(GBT_WINDOW_SIZE).setDefaultValue(DEFAULT_GBT_WINDOW_SIZE).finish();
    }

    protected PropertySpec cipheringTypePropertySpec() {
        return propertySpecService.stringSpec().named(CIPHERING_TYPE, CIPHERING_TYPE).describedAs(CIPHERING_TYPE).setDefaultValue(DEFAULT_CIPHERING_TYPE.getDescription())
                .addValues(CipheringType.GLOBAL.getDescription(),
                        CipheringType.DEDICATED.getDescription(),
                        CipheringType.GENERAL_GLOBAL.getDescription(),
                        CipheringType.GENERAL_DEDICATED.getDescription()).finish();
    }

    public PropertySpec callingAPTitlePropertySpec() {
        return propertySpecService.stringSpec().named(IDIS.CALLING_AP_TITLE, IDIS.CALLING_AP_TITLE).describedAs(IDIS.CALLING_AP_TITLE).setDefaultValue(IDIS.CALLING_AP_TITLE_DEFAULT).finish();
    }

    protected PropertySpec limitMaxNrOfDaysPropertySpec() {
        return propertySpecService.positiveBigDecimalSpec()
                .named(LIMIT_MAX_NR_OF_DAYS_PROPERTY, LIMIT_MAX_NR_OF_DAYS_PROPERTY)
                .describedAs(LIMIT_MAX_NR_OF_DAYS_PROPERTY)
                .setDefaultValue(BigDecimal.ZERO)
                .finish();
    }

    protected PropertySpec timeZonePropertySpec() {
        return propertySpecService.timezoneSpec()
                .named(TIMEZONE, TIMEZONE)
                .describedAs(TIMEZONE)
                .addValues(Stream.of(TimeZone.getAvailableIDs()).map(TimeZone::getTimeZone).collect(Collectors.toList()))
                .finish();
    }

    protected PropertySpec validateInvokeIdPropertySpec() {
        return propertySpecService.booleanSpec().named(USE_GBT, VALIDATE_INVOKE_ID).describedAs(VALIDATE_INVOKE_ID).markRequired().setDefaultValue(DEFAULT_VALIDATE_INVOKE_ID).finish();
    }

    protected PropertySpec readCachePropertySpec() {
        return propertySpecService.booleanSpec().named(READCACHE_PROPERTY, READCACHE_PROPERTY).describedAs(READCACHE_PROPERTY).markRequired().setDefaultValue(false).finish();
    }

    protected PropertySpec forcedDelayPropertySpec() {
        return propertySpecService.timeDurationSpec().named(FORCED_DELAY, FORCED_DELAY).describedAs(FORCED_DELAY).setDefaultValue(new TimeDuration(0)).finish();
    }

    protected PropertySpec maxRecPduSizePropertySpec() {
        return propertySpecService.positiveBigDecimalSpec().named(MAX_REC_PDU_SIZE, MAX_REC_PDU_SIZE).describedAs(MAX_REC_PDU_SIZE).finish();
    }

    protected PropertySpec callHomeIdPropertySpec() {
        return propertySpecService.stringSpec()
                .named(DeviceProtocolProperty.CALL_HOME_ID.javaFieldName(), DeviceProtocolProperty.CALL_HOME_ID.javaFieldName())
                .describedAs(DeviceProtocolProperty.CALL_HOME_ID.javaFieldName())
                .finish();
    }

}