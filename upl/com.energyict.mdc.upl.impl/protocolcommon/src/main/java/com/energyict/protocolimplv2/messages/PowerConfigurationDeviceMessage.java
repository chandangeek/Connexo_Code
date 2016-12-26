package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.util.Collections;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum PowerConfigurationDeviceMessage implements DeviceMessageSpecSupplier {

    IEC1107LimitPowerQuality(26001, "Limit power quality", DeviceMessageConstants.powerQualityThresholdAttributeName, DeviceMessageConstants.powerQualityThresholdAttributeDefaultTranslation),
    SetReferenceVoltage(26002, "Set reference voltage", DeviceMessageConstants.ReferenceVoltageAttributeName, DeviceMessageConstants.ReferenceVoltageAttributeDefaultTranslation),
    SetVoltageSagTimeThreshold(26003, "Set voltage sag time threshold", DeviceMessageConstants.VoltageSagTimeThresholdAttributeName, DeviceMessageConstants.VoltageSagTimeThresholdAttributeDefaultTranslation),
    SetVoltageSwellTimeThreshold(26004, "Set voltage swell time threshold", DeviceMessageConstants.VoltageSwellTimeThresholdAttributeName, DeviceMessageConstants.VoltageSwellTimeThresholdAttributeDefaultTranslation),
    SetVoltageSagThreshold(26005, "Set voltage sag threshold", DeviceMessageConstants.VoltageSagThresholdAttributeName, DeviceMessageConstants.VoltageSagThresholdAttributeDefaultTranslation),
    SetVoltageSwellThreshold(26006, "Set voltage swell threshold", DeviceMessageConstants.VoltageSwellThresholdAttributeName, DeviceMessageConstants.VoltageSwellThresholdAttributeDefaultTranslation),
    SetLongPowerFailureTimeThreshold(26007, "Set long power failure time threshold", DeviceMessageConstants.LongPowerFailureTimeThresholdAttributeName, DeviceMessageConstants.LongPowerFailureTimeThresholdAttributeDefaultTranslation),
    SetLongPowerFailureThreshold(26008, "Set long power failure threshold", DeviceMessageConstants.LongPowerFailureThresholdAttributeName, DeviceMessageConstants.LongPowerFailureThresholdAttributeDefaultTranslation);

    private final long id;
    private final String defaultNameTranslation;
    private final String propertyName;
    private final String propertyDefaultTranslation;

    PowerConfigurationDeviceMessage(long id, String defaultNameTranslation, String propertyName, String propertyDefaultTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
        this.propertyName = propertyName;
        this.propertyDefaultTranslation = propertyDefaultTranslation;
    }

    @Override
    public long id() {
        return this.id;
    }

    private String getNameResourceKey() {
        return PowerConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    private List<PropertySpec> getPropertySpecs(PropertySpecService service) {
        return Collections.singletonList(this.bigDecimalSpec(service, this.propertyName, this.propertyDefaultTranslation));
    }

    private PropertySpec bigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .bigDecimalSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.SMS_CONFIGURATION,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService, converter);
    }

}