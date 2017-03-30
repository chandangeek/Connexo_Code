/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.nta.dsmr23;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.legacy.dynamic.ConfigurationSupport;

import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.BULK_REQUEST;
import static com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.CIPHERING_TYPE;
import static com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.DEFAULT_CIPHERING_TYPE;
import static com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.DEFAULT_DEVICE_ID;
import static com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.DEFAULT_FORCED_DELAY;
import static com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.DEFAULT_MANUFACTURER;
import static com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.DEFAULT_MAX_REC_PDU_SIZE;
import static com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.DEVICE_ID;
import static com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.FIX_MBUS_HEX_SHORT_ID;
import static com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.FORCED_DELAY;
import static com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.MANUFACTURER;
import static com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.NTA_SIMULATION_TOOL;
import static com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.REQUEST_TIMEZONE;
import static com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.TIMEZONE;
import static com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.VALIDATE_INVOKE_ID;


public class DlmsConfigurationSupport implements ConfigurationSupport {

    private static final boolean DEFAULT_VALIDATE_INVOKE_ID = true;
    private final PropertySpecService propertySpecService;

    @Inject
    public DlmsConfigurationSupport(PropertySpecService propertySpecService) {
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
                this.bulkRequestPropertySpec(),
                this.cipheringTypePropertySpec(),
                this.ntaSimulationToolPropertySpec(),
                this.fixMbusHexShortIdPropertySpec(),
                this.manufacturerPropertySpec(),
                this.conformanceBlockValuePropertySpec(),
                this.requestTimeZonePropertySpec(),
                this.timeZonePropertySpec(),
                this.validateInvokeIdPropertySpec(),
                this.serverUpperMacAddressPropertySpec(),
                this.serverLowerMacAddressPropertySpec(),
                this.deviceId());
    }

    protected PropertySpec serverUpperMacAddressPropertySpec() {
        return propertySpecService.positiveBigDecimalSpec()
                .named(DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS, DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS)
                .describedAs(DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS)
                .setDefaultValue(BigDecimal.ONE)
                .finish();
    }

    protected PropertySpec serverLowerMacAddressPropertySpec() {
        return propertySpecService.positiveBigDecimalSpec()
                .named(DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS, DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS)
                .describedAs(DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS)
                .setDefaultValue(BigDecimal.ZERO)
                .finish();
    }

    protected PropertySpec timeZonePropertySpec() {
        return propertySpecService.timezoneSpec().named(TIMEZONE, TIMEZONE).describedAs(TIMEZONE).finish();
    }

    protected PropertySpec validateInvokeIdPropertySpec() {
        return propertySpecService.booleanSpec().named(VALIDATE_INVOKE_ID, VALIDATE_INVOKE_ID).describedAs(VALIDATE_INVOKE_ID).finish();
    }

    protected PropertySpec deviceId() {
        return propertySpecService.stringSpec().named(DEVICE_ID, DEVICE_ID).describedAs(DEVICE_ID).setDefaultValue(DEFAULT_DEVICE_ID).finish();
    }

    protected PropertySpec requestTimeZonePropertySpec() {
        return propertySpecService.booleanSpec().named(REQUEST_TIMEZONE, REQUEST_TIMEZONE).describedAs(REQUEST_TIMEZONE).finish();
    }

    protected PropertySpec forcedDelayPropertySpec() {
        return propertySpecService.timeDurationSpec().named(FORCED_DELAY, FORCED_DELAY).describedAs(FORCED_DELAY).setDefaultValue(new TimeDuration(DEFAULT_FORCED_DELAY)).finish();
    }

    protected PropertySpec conformanceBlockValuePropertySpec() {
        return propertySpecService.positiveBigDecimalSpec()
                .named(DlmsProtocolProperties.CONFORMANCE_BLOCK_VALUE, DlmsProtocolProperties.CONFORMANCE_BLOCK_VALUE)
                .describedAs(DlmsProtocolProperties.CONFORMANCE_BLOCK_VALUE)
                .setDefaultValue(BigDecimal.valueOf(ConformanceBlock.DEFAULT_LN_CONFORMANCE_BLOCK))
                .finish();
    }

    protected PropertySpec manufacturerPropertySpec() {
        return propertySpecService.stringSpec()
                .named(MANUFACTURER, MANUFACTURER)
                .describedAs(MANUFACTURER)
                .addValues("WKP", "ISK", "LGZ", "SLB", "ActarisPLCC", "SLB::SL7000")
                .setDefaultValue(DEFAULT_MANUFACTURER)
                .finish();
    }

    protected PropertySpec maxRecPduSizePropertySpec() {
        return propertySpecService.positiveBigDecimalSpec()
                .named(DlmsProtocolProperties.MAX_REC_PDU_SIZE, DlmsProtocolProperties.MAX_REC_PDU_SIZE)
                .describedAs(DlmsProtocolProperties.MAX_REC_PDU_SIZE)
                .setDefaultValue(new BigDecimal(DEFAULT_MAX_REC_PDU_SIZE))
                .finish();
    }

    protected PropertySpec bulkRequestPropertySpec() {
        return propertySpecService.booleanSpec().named(BULK_REQUEST, BULK_REQUEST).describedAs(BULK_REQUEST).finish();
    }

    protected PropertySpec cipheringTypePropertySpec() {
        return propertySpecService.positiveBigDecimalSpec()
                .named(CIPHERING_TYPE, CIPHERING_TYPE)
                .describedAs(CIPHERING_TYPE)
                .setDefaultValue(new BigDecimal(DEFAULT_CIPHERING_TYPE))
                .finish();  }

    protected PropertySpec ntaSimulationToolPropertySpec() {
        return propertySpecService.booleanSpec().named(NTA_SIMULATION_TOOL, NTA_SIMULATION_TOOL).describedAs(NTA_SIMULATION_TOOL).finish();
    }

    protected PropertySpec fixMbusHexShortIdPropertySpec() {
        return propertySpecService.booleanSpec().named(FIX_MBUS_HEX_SHORT_ID, FIX_MBUS_HEX_SHORT_ID).describedAs(FIX_MBUS_HEX_SHORT_ID).finish();
    }
}
