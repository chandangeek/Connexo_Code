package com.energyict.protocolimplv2.dlms.a1860.properties;

import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.upl.MeterProtocol;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static com.energyict.protocolimpl.nls.PropertyTranslationKeys.DLMS_APPLY_TRANSFORMER_RATIOS;
import static com.energyict.protocolimpl.nls.PropertyTranslationKeys.DLMS_READ_SERIAL_NUMBER;
import static com.energyict.protocolimpl.nls.PropertyTranslationKeys.DLMS_SEND_PREFIX_DESCRIPTION;
import static com.energyict.protocolimpl.nls.PropertyTranslationKeys.IEC1107_SECURITYLEVEL;
import static com.energyict.protocolimplv2.elster.ctr.MTU155.MTU155Properties.TIMEZONE_PROPERTY_NAME;

public class A1860ConfigurationSupport  implements HasDynamicProperties {

    private final static String READ_SERIAL_NUMBER = "ReadSerialNumber";
    private final static String SEND_PREFIX = "SendPrefix";
    private final static String SECURITY_LEVEL = "SecurityLevel";

    static final String READCACHE_PROPERTY = "ReadCache";
    static final String APPLY_TRANSFORMER_RATIOS = "ApplyTransformerRatios";
    static final BigDecimal DEFAULT_SERVER_LOWER_MAC_ADDRESS = BigDecimal.valueOf(16);

    private final PropertySpecService propertySpecService;

    public A1860ConfigurationSupport(PropertySpecService propertySpecService){
        this.propertySpecService = propertySpecService;
    }


    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.timeZonePropertySpec(),
                this.readCachePropertySpec(),
                this.nodeAddressPropertySpec(),
                this.deviceIdPropertySpec(),
                this.callHomeIdPropertySpec(),
                this.serverLowerMacAddressPropertySpec(),
                this.serverUpperMacAddressPropertySpec(),
                this.readSerialNumber(),
                this.sendPrefix(),
                this.applyTransformerRatios(),
                this.securityLevel()
        );
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        // currently doesn't hold any properties
    }

    private PropertySpec timeZonePropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(TIMEZONE_PROPERTY_NAME, true, PropertyTranslationKeys.V2_ELSTER_TIMEZONE, propertySpecService::timeZoneSpec)
                .finish();
    }

    private PropertySpec serverLowerMacAddressPropertySpec() {
        return bigDecimalSpec(DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS, false, PropertyTranslationKeys.V2_ELSTER_SERVER_LOWER_MAC_ADDRESS, DEFAULT_SERVER_LOWER_MAC_ADDRESS);
    }

    private PropertySpec deviceIdPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(MeterProtocol.Property.ADDRESS.getName(),
                        false,
                        PropertyTranslationKeys.V2_ELSTER_DEVICE_ID,
                        propertySpecService::stringSpec)
                .finish();
    }

    private PropertySpec callHomeIdPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, false, PropertyTranslationKeys.V2_ELSTER_CALL_HOME_ID, propertySpecService::stringSpec)
                .finish();
    }

    private PropertySpec nodeAddressPropertySpec() {
        return bigDecimalSpec(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName(), false, PropertyTranslationKeys.V2_DLMS_NODEID, BigDecimal.ZERO);
    }

    private PropertySpec readCachePropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(READCACHE_PROPERTY, false, PropertyTranslationKeys.V2_DLMS_READCACHE, propertySpecService::booleanSpec)
                .setDefaultValue(false)
                .finish();
    }

    private PropertySpec serverUpperMacAddressPropertySpec() {
        return bigDecimalSpec(DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS, false, PropertyTranslationKeys.V2_ELSTER_SERVER_UPPER_MAC_ADDRESS, BigDecimal.ONE);
    }

    private PropertySpec applyTransformerRatios(){
        return UPLPropertySpecFactory
                .specBuilder(APPLY_TRANSFORMER_RATIOS, false, DLMS_APPLY_TRANSFORMER_RATIOS, propertySpecService::booleanSpec)
                .setDefaultValue(false)
                .finish();

    }

    private PropertySpec sendPrefix(){
        return UPLPropertySpecFactory
                .specBuilder(SEND_PREFIX, false, DLMS_SEND_PREFIX_DESCRIPTION, propertySpecService::booleanSpec)
                .setDefaultValue(false)
                .finish();
    }

    private PropertySpec readSerialNumber(){
        return UPLPropertySpecFactory
                .specBuilder(READ_SERIAL_NUMBER, false, DLMS_READ_SERIAL_NUMBER, propertySpecService::booleanSpec)
                .setDefaultValue(false)
                .finish();
    }

    private PropertySpec securityLevel() {
        return UPLPropertySpecFactory
                .specBuilder(SECURITY_LEVEL,
                        true,
                        IEC1107_SECURITYLEVEL,
                        propertySpecService::stringSpec)
                .finish();
    }

    @SuppressWarnings("SameParameterValue")
    private PropertySpec bigDecimalSpec(String name, boolean required, TranslationKey translationKey, BigDecimal defaultValue, BigDecimal... validValues) {
        PropertySpecBuilder<BigDecimal> specBuilder = UPLPropertySpecFactory.specBuilder(name, required, translationKey, propertySpecService::bigDecimalSpec);
        specBuilder.setDefaultValue(defaultValue);
        specBuilder.addValues(validValues);
        if (validValues.length > 0) {
            specBuilder.markExhaustive();
        }
        return specBuilder.finish();
    }

}

