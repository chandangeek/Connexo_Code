package com.energyict.protocolimplv2.dlms.idis.aec.properties;

import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.DescriptionTranslationKey;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540ConfigurationSupport;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class AECConfigurationSupport extends AM540ConfigurationSupport {

    public AECConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.forcedDelayPropertySpec(),
                this.maxRecPduSizePropertySpec(),
                this.bulkRequestPropertySpec(),
                this.timeZonePropertySpec(),
                this.validateInvokeIdPropertySpec(),
                this.limitMaxNrOfDaysPropertySpec(),
                this.readCachePropertySpec(),
                this.callingAPTitlePropertySpec(),
                this.useGeneralBlockTransferPropertySpec(),
                this.generalBlockTransferWindowSizePropertySpec(),
                this.useUndefinedForClockStatus(),
                this.useUndefinedForTimeDeviation(),
                this.serverUpperMacAddressPropertySpec(),
                this.serverLowerMacAddressPropertySpec(),
                this.overwriteServerLowerMacAddressPropertySpec(),
                this.addressModePropertySpec()
        );
    }

    protected PropertySpec overwriteServerLowerMacAddressPropertySpec() {
        return this.booleanSpecBuilder(AECDlmsProperties.OVERWRITE_SERVER_LOWER_MAC_ADDRESS, true, com.energyict.protocolimpl.nls.PropertyTranslationKeys.DLMS_OVERWRITE_SERVER_LOWER_MAC_ADDRESS);
    }

    protected PropertySpec booleanSpecBuilder(String name, boolean defaultValue, TranslationKey translationKey) {
        return getPropertySpecService()
                .booleanSpec()
                .named(name, translationKey).describedAs(new DescriptionTranslationKey(translationKey))
                .setDefaultValue(defaultValue)
                .finish();
    }

    private PropertySpec addressModePropertySpec() {
        return this.bigDecimalSpec(DlmsProtocolProperties.ADDRESSING_MODE, true, com.energyict.protocolimpl.nls.PropertyTranslationKeys.DLMS_ADDRESSING_MODE, new BigDecimal(4), new BigDecimal(2), new BigDecimal(4));
    }

    /**
     * Returns the "UseUndefinedAsClockStatus" property spec.
     *
     * @return The property specification.
     */
    protected PropertySpec useUndefinedForClockStatus() {
        return UPLPropertySpecFactory.specBuilder(AM540ConfigurationSupport.USE_UNDEFINED_AS_CLOCK_STATUS, false, PropertyTranslationKeys.V2_USE_UNDEFINED_AS_CLOCK_STATUS, getPropertySpecService()::booleanSpec)
                .setDefaultValue(false)
                .finish();
    }

    /**
     * Returns the "UseUndefinedAsTimeDeviation" property spec.
     *
     * @return The property specification.
     */
    protected PropertySpec useUndefinedForTimeDeviation() {
        return UPLPropertySpecFactory.specBuilder(AM540ConfigurationSupport.USE_UNDEFINED_AS_TIME_DEVIATION, false, PropertyTranslationKeys.V2_USE_UNDEFINED_AS_TIME_DEVIATION, getPropertySpecService()::booleanSpec)
                .setDefaultValue(false)
                .finish();
    }
}
