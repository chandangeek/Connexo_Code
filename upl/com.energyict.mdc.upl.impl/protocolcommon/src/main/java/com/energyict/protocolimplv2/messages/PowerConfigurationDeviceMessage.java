package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum PowerConfigurationDeviceMessage implements DeviceMessageSpecSupplier {

    IEC1107LimitPowerQuality(26001, "Limit power quality") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(bigDecimalSpec(service, DeviceMessageConstants.powerQualityThresholdAttributeName, DeviceMessageConstants.powerQualityThresholdAttributeDefaultTranslation));
        }
        },
    SetReferenceVoltage(26002, "Set reference voltage") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(bigDecimalSpec(service, DeviceMessageConstants.ReferenceVoltageAttributeName, DeviceMessageConstants.ReferenceVoltageAttributeDefaultTranslation));
        }
        },
    SetVoltageSagTimeThreshold(26003, "Set voltage sag time threshold") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(bigDecimalSpec(service, DeviceMessageConstants.VoltageSagTimeThresholdAttributeName, DeviceMessageConstants.VoltageSagTimeThresholdAttributeDefaultTranslation));
        }},
    SetVoltageSwellTimeThreshold(26004, "Set voltage swell time threshold") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(bigDecimalSpec(service, DeviceMessageConstants.VoltageSwellTimeThresholdAttributeName, DeviceMessageConstants.VoltageSwellTimeThresholdAttributeDefaultTranslation));
        }},
    SetVoltageSagThreshold(26005, "Set voltage sag threshold") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(bigDecimalSpec(service, DeviceMessageConstants.VoltageSagThresholdAttributeName, DeviceMessageConstants.VoltageSagThresholdAttributeDefaultTranslation));
        }},
    SetVoltageSwellThreshold(26006, "Set voltage swell threshold") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(bigDecimalSpec(service, DeviceMessageConstants.VoltageSwellThresholdAttributeName, DeviceMessageConstants.VoltageSwellThresholdAttributeDefaultTranslation));
        }},
    SetLongPowerFailureTimeThreshold(26007, "Set long power failure time threshold") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(bigDecimalSpec(service, DeviceMessageConstants.LongPowerFailureTimeThresholdAttributeName, DeviceMessageConstants.LongPowerFailureTimeThresholdAttributeDefaultTranslation));
        }},
    SetLongPowerFailureThreshold(26008, "Set long power failure threshold") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(bigDecimalSpec(service, DeviceMessageConstants.LongPowerFailureThresholdAttributeName, DeviceMessageConstants.LongPowerFailureThresholdAttributeDefaultTranslation));
        }},
    SetPowerQualityMeasurePeriod(26009, "Set power quality measure period") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(bigDecimalSpec(service, DeviceMessageConstants.SetPowerQualityMeasurePeriodAttributeName, DeviceMessageConstants.SetPowerQualityMeasurePeriodDefaultTranslation));
        }},
    SetVoltageAndCurrentParameters(26010, "Write voltage and current ratios") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    bigDecimalSpec(service, DeviceMessageConstants.VoltageRatioDenominatorAttributeName, DeviceMessageConstants.VoltageRatioDenominatorDefaultTranslation),
                    bigDecimalSpec(service, DeviceMessageConstants.VoltageRatioNumeratorAttributeName, DeviceMessageConstants.VoltageRatioNumeratorDefaultTranslation),
                    bigDecimalSpec(service, DeviceMessageConstants.CurrentRatioDenominatorAttributeName, DeviceMessageConstants.CurrentRatioDenominatorDefaultTranslation),
                    bigDecimalSpec(service, DeviceMessageConstants.CurrentRatioNumeratorAttributeName, DeviceMessageConstants.CurrentRatioNumeratorDefaultTranslation));
        }},
    SetVoltageRatioNumerator(26011, "Write voltage ratio") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(bigDecimalSpec(service, DeviceMessageConstants.VoltageRatioNumeratorAttributeName, DeviceMessageConstants.VoltageRatioNumeratorDefaultTranslation));
        }},
    SetCurrentRatioNumerator(26012, "Write current ratio") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(bigDecimalSpec(service, DeviceMessageConstants.CurrentRatioNumeratorAttributeName, DeviceMessageConstants.CurrentRatioNumeratorDefaultTranslation));
        }};

    private final long id;
    private final String defaultNameTranslation;

    PowerConfigurationDeviceMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    @Override
    public long id() {
        return this.id;
    }

    private String getNameResourceKey() {
        return PowerConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.POWER_CONFIGURATION,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService, converter);
    }
}