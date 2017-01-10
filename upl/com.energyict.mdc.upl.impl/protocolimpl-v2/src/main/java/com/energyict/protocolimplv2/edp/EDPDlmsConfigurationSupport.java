package com.energyict.protocolimplv2.edp;

import com.energyict.mdc.upl.properties.*;

import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_FORCED_DELAY;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_MAX_REC_PDU_SIZE;
import static com.energyict.dlms.common.DlmsProtocolProperties.FORCED_DELAY;
import static com.energyict.dlms.common.DlmsProtocolProperties.MAX_REC_PDU_SIZE;
import static com.energyict.dlms.common.DlmsProtocolProperties.TIMEZONE;

/**
 * A collection of general DLMS properties that are relevant for the EDP DLMS meters.
 * These properties are not related to the security or the protocol dialects.
 * The parsing and the usage of the property values is done in implementations of {@link com.energyict.dlms.protocolimplv2.DlmsSessionProperties}
 * <p>
 * Copyrights EnergyICT
 * Date: 22/10/13
 * Time: 15:41
 * Author: khe
 */
public class EDPDlmsConfigurationSupport implements HasDynamicProperties {

    private final PropertySpecService propertySpecService;

    public EDPDlmsConfigurationSupport(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    private PropertySpec timeZonePropertySpec() {
        return propertySpecService
                .timeZoneSpec()
                .named(TIMEZONE, TIMEZONE)
                .describedAs("Description for " + TIMEZONE)
                .finish();
    }

    private PropertySpec serverUpperMacAddressPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS, false, propertySpecService::bigDecimalSpec)
                .setDefaultValue(BigDecimal.ONE)
                .finish();
    }

    private PropertySpec serverLowerMacAddressPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS, false, propertySpecService::bigDecimalSpec)
                .setDefaultValue(BigDecimal.valueOf(16))
                .finish();
    }

    private PropertySpec readCachePropertySpec() {
        return UPLPropertySpecFactory.specBuilder(DlmsProtocolProperties.READCACHE_PROPERTY, false, propertySpecService::booleanSpec).finish();
    }

    private PropertySpec forcedDelayPropertySpec() {
        return this.durationSpec(FORCED_DELAY, false, DEFAULT_FORCED_DELAY);
    }

    private PropertySpec maxRecPduSizePropertySpec() {
        return UPLPropertySpecFactory.specBuilder(MAX_REC_PDU_SIZE, false, propertySpecService::bigDecimalSpec)
                .setDefaultValue(DEFAULT_MAX_REC_PDU_SIZE)
                .finish();
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.forcedDelayPropertySpec(),
                this.maxRecPduSizePropertySpec(),
                this.timeZonePropertySpec(),
                this.serverUpperMacAddressPropertySpec(),
                this.serverLowerMacAddressPropertySpec(),
                this.readCachePropertySpec());
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        // currently nothing to set
    }

    private PropertySpec durationSpec(String name, boolean required, Duration defaultValue) {
        PropertySpecBuilder<Duration> durationPropertySpecBuilder = UPLPropertySpecFactory.specBuilder(name, required, propertySpecService::durationSpec);
        durationPropertySpecBuilder.setDefaultValue(defaultValue);
        return durationPropertySpecBuilder.finish();
    }
}