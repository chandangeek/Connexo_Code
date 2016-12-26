package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides a summary of all messages that have no unique goal.
 * For example, this can be a message that writes a general value to a certain DLMS object, chosen by the user (obiscode)
 * <p/>
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/04/13
 * Time: 8:38
 */
public enum GeneralDeviceMessage implements DeviceMessageSpecSupplier {

    WRITE_RAW_IEC1107_CLASS(11001, "Write raw IEC1107 class") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.boundedBigDecimalSpec(service, DeviceMessageConstants.IEC1107ClassIdAttributeName, DeviceMessageConstants.IEC1107ClassIdAttributeDefaultTranslation, BigDecimal.ZERO, BigDecimal.valueOf(9999)),
                    this.boundedBigDecimalSpec(service, DeviceMessageConstants.OffsetAttributeName, DeviceMessageConstants.OffsetAttributeDefaultTranslation, BigDecimal.ZERO, BigDecimal.valueOf(9999)),
                    this.hexStringSpec(service, DeviceMessageConstants.RawDataAttributeName, DeviceMessageConstants.RawDataAttributeDefaultTranslation)
            );
        }
    },
    WRITE_FULL_CONFIGURATION(11002, "Write full configuration") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.deviceMessageFileSpec(service, DeviceMessageConstants.configUserFileAttributeName, DeviceMessageConstants.configUserFileAttributeDefaultTranslation));
        }
    },
    SEND_XML_MESSAGE(11003, "Send XML message") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.xmlMessageAttributeName, DeviceMessageConstants.xmlMessageAttributeDefaultTranslation));
        }
    };

    private final long id;
    private final String defaultNameTranslation;

    GeneralDeviceMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    @Override
    public long id() {
        return this.id;
    }

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

    protected PropertySpec boundedBigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, BigDecimal lowerLimit, BigDecimal upperLimit) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .boundedBigDecimalSpec(lowerLimit, upperLimit)
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    protected PropertySpec stringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    protected PropertySpec hexStringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .hexStringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    protected PropertySpec deviceMessageFileSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .referenceSpec(DeviceMessageFile.class.getName())
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    private String getNameResourceKey() {
        return GeneralDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.GENERAL,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService, converter);
    }

}