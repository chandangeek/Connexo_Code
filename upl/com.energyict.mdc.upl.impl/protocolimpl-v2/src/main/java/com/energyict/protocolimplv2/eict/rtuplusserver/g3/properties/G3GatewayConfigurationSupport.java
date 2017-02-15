package com.energyict.protocolimplv2.eict.rtuplusserver.g3.properties;

import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.DescriptionTranslationKey;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static com.energyict.dlms.common.DlmsProtocolProperties.*;

/**
 * A collection of general DLMS properties that are relevant for the G3 gateway protocol.
 * These properties are not related to the security or the protocol dialects.
 * The parsing and the usage of the property values is done in implementations of {@link com.energyict.dlms.protocolimplv2.DlmsSessionProperties}
 * <p>
 * Copyrights EnergyICT
 * Date: 22/10/13
 * Time: 15:41
 * Author: khe
 */
public class G3GatewayConfigurationSupport {

    private final PropertySpecService propertySpecService;

    public G3GatewayConfigurationSupport(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                this.forcedDelayPropertySpec(),
                this.maxRecPduSizePropertySpec(),
                this.timeZonePropertySpec(),
                this.aarqTimeoutPropertySpec(),
                this.readCachePropertySpec(),
                this.serverUpperMacAddressPropertySpec(),
                this.validateInvokeIdPropertySpec()
        );
    }

    private PropertySpec serverUpperMacAddressPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS, false, PropertyTranslationKeys.V2_EICT_SERVER_UPPER_MAC_ADDRESS, this.propertySpecService::bigDecimalSpec)
                .setDefaultValue(BigDecimal.ONE)
                .finish();
    }

    private PropertySpec timeZonePropertySpec() {
        return this.propertySpecService
                .timeZoneSpec()
                .named(TIMEZONE, TIMEZONE)
                .describedAs("Description for " + TIMEZONE)
                .finish();
    }

    private PropertySpec validateInvokeIdPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(VALIDATE_INVOKE_ID, true, PropertyTranslationKeys.V2_EICT_VALIDATE_INVOKE_ID, propertySpecService::booleanSpec).finish();
    }

    private PropertySpec aarqTimeoutPropertySpec() {
        return this.durationPropertySpecBuilder(G3GatewayProperties.AARQ_TIMEOUT, PropertyTranslationKeys.V2_EICT_AARQ_TIMEOUT)
                .setDefaultValue(G3GatewayProperties.AARQ_TIMEOUT_DEFAULT)
                .finish();
    }

    private PropertySpec readCachePropertySpec() {
        return UPLPropertySpecFactory.specBuilder(DlmsProtocolProperties.READCACHE_PROPERTY, false, this.propertySpecService::booleanSpec).finish();
    }

    private PropertySpec forcedDelayPropertySpec() {
        return this.durationPropertySpecBuilder(FORCED_DELAY, PropertyTranslationKeys.V2_EICT_FORCED_DELAY)
                .setDefaultValue(DEFAULT_FORCED_DELAY)
                .finish();
    }

    private PropertySpec maxRecPduSizePropertySpec() {
        return UPLPropertySpecFactory.specBuilder(MAX_REC_PDU_SIZE, false, this.propertySpecService::bigDecimalSpec)
                .setDefaultValue(DEFAULT_MAX_REC_PDU_SIZE)
                .finish();
    }

    private PropertySpecBuilder<Duration> durationPropertySpecBuilder(String name, TranslationKey translationKey) {
        return this.propertySpecService
                .durationSpec()
                .named(name, translationKey)
                .describedAs(new DescriptionTranslationKey(translationKey));
    }

}