package com.energyict.protocolimplv2.nta.dsmr23;

import com.energyict.mdc.upl.properties.*;

import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static com.energyict.dlms.common.DlmsProtocolProperties.BULK_REQUEST;
import static com.energyict.dlms.common.DlmsProtocolProperties.CIPHERING_TYPE;
import static com.energyict.dlms.common.DlmsProtocolProperties.CONFORMANCE_BLOCK_VALUE;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_CIPHERING_TYPE;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_DEVICE_ID;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_FORCED_DELAY;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_MANUFACTURER;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_MAX_REC_PDU_SIZE;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEVICE_ID;
import static com.energyict.dlms.common.DlmsProtocolProperties.FIX_MBUS_HEX_SHORT_ID;
import static com.energyict.dlms.common.DlmsProtocolProperties.FORCED_DELAY;
import static com.energyict.dlms.common.DlmsProtocolProperties.MANUFACTURER;
import static com.energyict.dlms.common.DlmsProtocolProperties.MAX_REC_PDU_SIZE;
import static com.energyict.dlms.common.DlmsProtocolProperties.NTA_SIMULATION_TOOL;
import static com.energyict.dlms.common.DlmsProtocolProperties.REQUEST_TIMEZONE;
import static com.energyict.dlms.common.DlmsProtocolProperties.TIMEOUT;
import static com.energyict.dlms.common.DlmsProtocolProperties.TIMEZONE;
import static com.energyict.dlms.common.DlmsProtocolProperties.VALIDATE_INVOKE_ID;

/**
 * A collection of general DLMS properties.
 * These properties are not related to the security or the protocol dialects.
 * The parsing and the usage of the property values is done in implementations of {@link com.energyict.dlms.protocolimplv2.DlmsSessionProperties}
 * <p/>
 * Copyrights EnergyICT
 * Date: 22/10/13
 * Time: 15:41
 * Author: khe
 */
public class DlmsConfigurationSupport implements HasDynamicProperties{

    private static final boolean DEFAULT_VALIDATE_INVOKE_ID = true;

    private final PropertySpecService propertySpecService;

    public DlmsConfigurationSupport(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    public List<PropertySpec> getUPLPropertySpecs() {
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

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        // currently no properties are set ...
    }

    protected PropertySpec serverUpperMacAddressPropertySpec() {
        return this.bigDecimalSpec(DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS, false, BigDecimal.ONE);
    }

    protected PropertySpec serverLowerMacAddressPropertySpec() {
        return this.bigDecimalSpec(DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS, false, BigDecimal.ZERO);
    }

    protected PropertySpec timeZonePropertySpec() {
        return this.propertySpecService.timeZoneSpec().named(TIMEZONE, TIMEZONE).describedAs("Description for " + TIMEOUT).finish();
    }

    protected PropertySpec validateInvokeIdPropertySpec() {
        return this.booleanSpecBuilder(VALIDATE_INVOKE_ID)
                .setDefaultValue(DEFAULT_VALIDATE_INVOKE_ID)
                .finish();
    }

    private PropertySpecBuilder<Boolean> booleanSpecBuilder(String name) {
        return this.propertySpecService
                .booleanSpec()
                .named(name, name).describedAs("Description for " + name);
    }

    protected PropertySpec deviceId() {
        return this.stringWithDefaultSpec(DEVICE_ID, false, DEFAULT_DEVICE_ID);
    }

    protected PropertySpec requestTimeZonePropertySpec() {
        return this.booleanSpecBuilder(REQUEST_TIMEZONE).finish();
    }

    protected PropertySpec forcedDelayPropertySpec() {
        return this.propertySpecService
                .durationSpec()
                .named(FORCED_DELAY, FORCED_DELAY)
                .describedAs("Description for " + FORCED_DELAY)
                .setDefaultValue(DEFAULT_FORCED_DELAY)
                .finish();
    }

    protected PropertySpec conformanceBlockValuePropertySpec() {
        return this.bigDecimalSpec(CONFORMANCE_BLOCK_VALUE, false, BigDecimal.valueOf(ConformanceBlock.DEFAULT_LN_CONFORMANCE_BLOCK));
    }

    protected PropertySpec manufacturerPropertySpec() {
        return this.stringWithDefaultSpec(MANUFACTURER, false, DEFAULT_MANUFACTURER, "WKP", "ISK", "LGZ", "SLB", "ActarisPLCC", "SLB::SL7000");
    }

    protected PropertySpec maxRecPduSizePropertySpec() {
        return this.bigDecimalSpec(MAX_REC_PDU_SIZE, false, DEFAULT_MAX_REC_PDU_SIZE);
    }

    protected PropertySpec bulkRequestPropertySpec() {
        return this.booleanSpecBuilder(BULK_REQUEST).finish();
    }

    protected PropertySpec cipheringTypePropertySpec() {
        return this.bigDecimalSpec(CIPHERING_TYPE, false, DEFAULT_CIPHERING_TYPE);
    }

    protected PropertySpec ntaSimulationToolPropertySpec() {
        return this.booleanSpecBuilder(NTA_SIMULATION_TOOL).finish();
    }

    protected PropertySpec fixMbusHexShortIdPropertySpec() {
        return this.booleanSpecBuilder(FIX_MBUS_HEX_SHORT_ID).finish();
    }

    /**
     * Property that can be used to indicate whether or not the public client has a pre-established association.
     *
     * @return	The property specification.
     */
    protected final PropertySpec publicClientPreEstablishedPropertySpec() {
        return this.booleanSpecBuilder(com.energyict.dlms.protocolimplv2.DlmsSessionProperties.PUBLIC_CLIENT_ASSOCIATION_PRE_ESTABLISHED).finish();
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    protected PropertySpec bigDecimalSpec(String name, boolean required, BigDecimal defaultValue, BigDecimal... validValues) {
        PropertySpecBuilder<BigDecimal> specBuilder = UPLPropertySpecFactory.specBuilder(name, required, getPropertySpecService()::bigDecimalSpec);
        specBuilder.setDefaultValue(defaultValue);
        specBuilder.addValues(validValues);
        if (validValues.length > 0) {
            specBuilder.markExhaustive();
        }
        return specBuilder.finish();
    }

    protected PropertySpec stringWithDefaultSpec(String name, boolean required, String defaultValue, String... validValues) {
        PropertySpecBuilder<String> specBuilder = UPLPropertySpecFactory.specBuilder(name, required, getPropertySpecService()::stringSpec);
        specBuilder.setDefaultValue(defaultValue);
        specBuilder.addValues(validValues);
        if (validValues.length > 0) {
            specBuilder.markExhaustive();
        }
        return specBuilder.finish();
    }

}