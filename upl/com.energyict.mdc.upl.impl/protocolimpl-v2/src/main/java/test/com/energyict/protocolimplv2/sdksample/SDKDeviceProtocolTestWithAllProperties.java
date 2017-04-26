/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package test.com.energyict.protocolimplv2.sdksample;


import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.obis.ObisCode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SDKDeviceProtocolTestWithAllProperties extends SDKDeviceProtocol {

    public SDKDeviceProtocolTestWithAllProperties(CollectedDataFactory collectedDataFactory, PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        super(collectedDataFactory, propertySpecService, nlsService, converter);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> optionalProperties = new ArrayList<>();
        optionalProperties.add(
                this.propertySpecService
                        .stringSpec()
                        .named(SDKWithAllPropertiesTranslationKeys.SDKSTRINGPROPERTY)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKSTRINGPROPERTY)
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .stringSpec()
                        .named(SDKWithAllPropertiesTranslationKeys.SDKSTRINGPROPERTYWITHDEFAULT)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKSTRINGPROPERTYWITHDEFAULT)
                        .setDefaultValue("Test")
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .stringSpec()
                        .named(SDKWithAllPropertiesTranslationKeys.SDKSTRINGPROPERTYWITHVALUES)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKSTRINGPROPERTYWITHVALUES)
                        .addValues("value 1", "value 2", "value 3", "value 4")
                        .markExhaustive()
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .stringSpec()
                        .named(SDKWithAllPropertiesTranslationKeys.SDKSTRINGPROPERTYWITHVALUESANDDEFAULT)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKSTRINGPROPERTYWITHVALUESANDDEFAULT)
                        .markExhaustive()
                        .setDefaultValue("value 3")
                        .addValues("value 1", "value 2", "value 4", "value 5")
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .stringSpecOfExactLength(10)
                        .named(SDKWithAllPropertiesTranslationKeys.SDKEXACTSTRINGPROPERTY)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKEXACTSTRINGPROPERTY)
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .stringSpecOfMaximumLength(10)
                        .named(SDKWithAllPropertiesTranslationKeys.SDKMAXLENGTHSTRINGPROPERTY)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKMAXLENGTHSTRINGPROPERTY)
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .hexStringSpec()
                        .named(SDKWithAllPropertiesTranslationKeys.SDKHEXSTRINGPROPERTY)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKHEXSTRINGPROPERTY)
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .integerSpec()
                        .named(SDKWithAllPropertiesTranslationKeys.SDKINTEGERPROPERTY)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKINTEGERPROPERTY)
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .longSpec()
                        .named(SDKWithAllPropertiesTranslationKeys.SDKLONGPROPERTY)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKLONGPROPERTY)
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .hexStringSpecOfExactLength(10)
                        .named(SDKWithAllPropertiesTranslationKeys.SDKHEXSTRINGEXACTPROPERTY)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKHEXSTRINGEXACTPROPERTY)
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .passwordSpec()
                        .named(SDKWithAllPropertiesTranslationKeys.SDKPASSWORDPROPERTY)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKPASSWORDPROPERTY)
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(SDKWithAllPropertiesTranslationKeys.SDKBIGDECIMALPROPERTY)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKBIGDECIMALPROPERTY)
                        .finish());
        optionalProperties.add(
                this.propertySpecService.
                        bigDecimalSpec()
                        .named(SDKWithAllPropertiesTranslationKeys.SDKBIGDECIMALWITHDEFAULT)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKBIGDECIMALWITHDEFAULT)
                        .setDefaultValue(new BigDecimal("666.156"))
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(SDKWithAllPropertiesTranslationKeys.SDKBIGDECIMALWITHVALUES)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKBIGDECIMALWITHVALUES)
                        .setDefaultValue(BigDecimal.ZERO)
                        .addValues(
                                BigDecimal.ONE,
                                new BigDecimal("2"),
                                new BigDecimal("3"))
                        .markExhaustive()
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .boundedBigDecimalSpec(new BigDecimal(2), BigDecimal.TEN)
                        .named(SDKWithAllPropertiesTranslationKeys.SDKBOUNDEDDECIMAL)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKBOUNDEDDECIMAL)
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .positiveBigDecimalSpec()
                        .named(SDKWithAllPropertiesTranslationKeys.SDKPOSITIVEDECIMALPROPERTY)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKPOSITIVEDECIMALPROPERTY)
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .booleanSpec()
                        .named(SDKWithAllPropertiesTranslationKeys.SDKBOOLEANPROPERTY)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKBOOLEANPROPERTY)
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .dateSpec()
                        .named(SDKWithAllPropertiesTranslationKeys.SDKDATEPROPERTY)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKDATEPROPERTY)
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .timeSpec()
                        .named(SDKWithAllPropertiesTranslationKeys.SDKTIMEOFDAYPROPERTY)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKTIMEOFDAYPROPERTY)
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .timeZoneSpec()
                        .named(SDKWithAllPropertiesTranslationKeys.SDKTIMEOFDAYPROPERTY)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKTIMEOFDAYPROPERTY)
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .dateTimeSpec()
                        .named(SDKWithAllPropertiesTranslationKeys.SDKDATETIMEPROPERTY)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKDATETIMEPROPERTY)
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .durationSpec()
                        .named(SDKWithAllPropertiesTranslationKeys.SDKDURATIONPROPERTY)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKDURATIONPROPERTY)
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .temporalAmountSpec()
                        .named(SDKWithAllPropertiesTranslationKeys.SDKDURATIONPROPERTY)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKDURATIONPROPERTY)
                        .finish());

        optionalProperties.add(
                this.propertySpecService
                        .obisCodeSpec()
                        .named(SDKWithAllPropertiesTranslationKeys.SDKOBISCODEPROPERTY)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKOBISCODEPROPERTY)
                        .addValues(
                                ObisCode.fromString("1.0.1.8.0.255"),
                                ObisCode.fromString("1.0.1.8.1.255"),
                                ObisCode.fromString("1.0.1.8.2.255"),
                                ObisCode.fromString("1.0.2.8.0.255"),
                                ObisCode.fromString("1.0.2.8.1.255"),
                                ObisCode.fromString("1.0.2.8.2.255"))
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .encryptedStringSpec()
                        .named(SDKWithAllPropertiesTranslationKeys.SDKENCRYPTEDSTRINGPROPERTY)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKENCRYPTEDSTRINGPROPERTY)
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .referenceSpec(com.energyict.mdc.upl.properties.DeviceMessageFile.class.getName())
                        .named(SDKWithAllPropertiesTranslationKeys.SDKDEVICEMSSAGEFILEPROPERTY)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKDEVICEMSSAGEFILEPROPERTY)
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .referenceSpec(com.energyict.mdc.upl.properties.DeviceGroup.class.getName())
                        .named(SDKWithAllPropertiesTranslationKeys.SDKDEVICEGROUPPROPERTY)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKDEVICEGROUPPROPERTY)
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .referenceSpec(com.energyict.mdc.upl.meterdata.LoadProfile.class.getName())
                        .named(SDKWithAllPropertiesTranslationKeys.SDKLOADPROFILEROPERTY)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKLOADPROFILEROPERTY)
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .referenceSpec(com.energyict.mdc.upl.properties.TariffCalendar.class.getName())
                        .named(SDKWithAllPropertiesTranslationKeys.SDKTARIFFCALENDARROPERTY)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKTARIFFCALENDARROPERTY)
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .referenceSpec(com.energyict.mdc.upl.properties.FirmwareVersion.class.getName())
                        .named(SDKWithAllPropertiesTranslationKeys.SDKFIRMWARE_VERSIONROPERTY)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKFIRMWARE_VERSIONROPERTY)
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .referenceSpec(com.energyict.mdc.upl.properties.NumberLookup.class.getName())
                        .named(SDKWithAllPropertiesTranslationKeys.SDKNUMBER_LOOKUPROPERTY)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKNUMBER_LOOKUPROPERTY)
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .referenceSpec(com.energyict.mdc.upl.security.CertificateWrapper.class.getName())
                        .named(SDKWithAllPropertiesTranslationKeys.SDKCERTIFICATE_WRAPPERROPERTY)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKCERTIFICATE_WRAPPERROPERTY)
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .referenceSpec(com.energyict.mdc.upl.properties.StringLookup.class.getName())
                        .named(SDKWithAllPropertiesTranslationKeys.SDKSTRING_LOOKUPROPERTY)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKSTRING_LOOKUPROPERTY)
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .referenceSpec(com.energyict.mdc.upl.security.CertificateAlias.class.getName())
                        .named(SDKWithAllPropertiesTranslationKeys.SDKCERTIFICATE_ALIASROPERTY)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKCERTIFICATE_ALIASROPERTY)
                        .finish());
        optionalProperties.add(
                this.propertySpecService
                        .referenceSpec(com.energyict.mdc.upl.security.PrivateKeyAlias.class.getName())
                        .named(SDKWithAllPropertiesTranslationKeys.SDKPRIVATE_KEY_ALIASROPERTY)
                        .describedAs(SDKWithAllPropertiesTranslationKeys.SDKPRIVATE_KEY_ALIASROPERTY)
                        .finish());
        return optionalProperties;
    }

    @Override
    public String getProtocolDescription() {
        return "EICT SDK DeviceProtocol with all properties";
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }
}