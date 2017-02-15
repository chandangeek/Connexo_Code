package com.energyict.protocolimplv2.dlms.idis.am130.properties;

import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.dlms.CipheringType;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.dlms.idis.IDIS;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.Dsmr50Properties;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_FORCED_DELAY;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_MAX_REC_PDU_SIZE;
import static com.energyict.dlms.common.DlmsProtocolProperties.FORCED_DELAY;
import static com.energyict.dlms.common.DlmsProtocolProperties.GBT_WINDOW_SIZE;
import static com.energyict.dlms.common.DlmsProtocolProperties.MAX_REC_PDU_SIZE;
import static com.energyict.dlms.common.DlmsProtocolProperties.TIMEZONE;
import static com.energyict.dlms.common.DlmsProtocolProperties.USE_GBT;
import static com.energyict.dlms.common.DlmsProtocolProperties.VALIDATE_INVOKE_ID;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 19/12/2014 - 15:53
 */
public class AM130ConfigurationSupport implements HasDynamicProperties {

    public static final String LIMIT_MAX_NR_OF_DAYS_PROPERTY = "LimitMaxNrOfDays";

    public static final boolean DEFAULT_VALIDATE_INVOKE_ID = true;
    public static final BigDecimal DEFAULT_GBT_WINDOW_SIZE = BigDecimal.valueOf(5);
    public static final boolean USE_GBT_DEFAULT_VALUE = true;
    public static final CipheringType DEFAULT_CIPHERING_TYPE = CipheringType.GENERAL_GLOBAL;

    private final PropertySpecService propertySpecService;

    public AM130ConfigurationSupport(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
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
                this.callHomeIdPropertySpec());
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        // currently I don't hold any properties
    }

    protected PropertySpec serverUpperMacAddressPropertySpec() {
        return this.bigDecimalSpec(DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS, BigDecimal.ONE, PropertyTranslationKeys.V2_DLMS_SERVER_UPPER_MAC_ADDRESS);
    }

    protected PropertySpec useGeneralBlockTransferPropertySpec() {
        return this.booleanSpec(USE_GBT, USE_GBT_DEFAULT_VALUE, PropertyTranslationKeys.V2_DLMS_USE_GBT);
    }

    protected PropertySpec generalBlockTransferWindowSizePropertySpec() {
        return this.bigDecimalSpec(GBT_WINDOW_SIZE, DEFAULT_GBT_WINDOW_SIZE, PropertyTranslationKeys.V2_DLMS_GBT_WINDOW_SIZE);
    }

    protected PropertySpec cipheringTypePropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(DlmsProtocolProperties.CIPHERING_TYPE, false, PropertyTranslationKeys.V2_DLMS_CIPHERING_TYPE, this.propertySpecService::stringSpec)
                .setDefaultValue(DEFAULT_CIPHERING_TYPE.getDescription())
                .addValues(
                    CipheringType.GLOBAL.getDescription(),
                    CipheringType.DEDICATED.getDescription(),
                    CipheringType.GENERAL_GLOBAL.getDescription(),
                    CipheringType.GENERAL_DEDICATED.getDescription())
                .markExhaustive()
                .finish();
    }

    public PropertySpec callingAPTitlePropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(IDIS.CALLING_AP_TITLE, false, PropertyTranslationKeys.V2_DLMS_IDIS_CALLING_AP_TITLE, this.propertySpecService::stringSpec)
                .setDefaultValue(IDIS.CALLING_AP_TITLE_DEFAULT)
                .finish();
    }

    protected PropertySpec limitMaxNrOfDaysPropertySpec() {
        return this.bigDecimalSpec(LIMIT_MAX_NR_OF_DAYS_PROPERTY, BigDecimal.ZERO, PropertyTranslationKeys.V2_DLMS_LIMIT_MAX_NR_OF_DAYS);
    }

    protected PropertySpec timeZonePropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(TIMEZONE, false, PropertyTranslationKeys.V2_DLMS_TIMEZONE, this.propertySpecService::timeZoneSpec)
                .finish();
    }

    protected PropertySpec validateInvokeIdPropertySpec() {
        return this.booleanSpec(VALIDATE_INVOKE_ID, DEFAULT_VALIDATE_INVOKE_ID, PropertyTranslationKeys.V2_DLMS_VALIDATE_INVOKE_ID);
    }

    protected PropertySpec readCachePropertySpec() {
        return this.booleanSpec(Dsmr50Properties.READCACHE_PROPERTY, false, PropertyTranslationKeys.V2_DLMS_READCACHE);
    }

    protected PropertySpec forcedDelayPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(FORCED_DELAY, false, PropertyTranslationKeys.V2_DLMS_FORCED_DELAY, this.propertySpecService::durationSpec)
                .setDefaultValue(DEFAULT_FORCED_DELAY)
                .finish();
    }

    protected PropertySpec maxRecPduSizePropertySpec() {
        return this.bigDecimalSpec(MAX_REC_PDU_SIZE, DEFAULT_MAX_REC_PDU_SIZE, PropertyTranslationKeys.V2_DLMS_MAX_REC_PDU_SIZE);
    }

    protected PropertySpec callHomeIdPropertySpec() {
        return this.stringSpec(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, PropertyTranslationKeys.V2_DLMS_CALL_HOME_ID);
    }

    private PropertySpec stringSpec (String name, TranslationKey translationKey) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, translationKey, this.propertySpecService::stringSpec)
                .finish();
    }

    private PropertySpec booleanSpec (String name, boolean defaultValue, TranslationKey translationKey) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, translationKey, this.propertySpecService::booleanSpec)
                .setDefaultValue(defaultValue)
                .finish();
    }

    private PropertySpec bigDecimalSpec (String name, BigDecimal defaultValue, TranslationKey translationKey) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, translationKey, this.propertySpecService::bigDecimalSpec)
                .setDefaultValue(defaultValue)
                .finish();
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }
}