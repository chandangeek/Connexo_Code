package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.TimeDurationValueFactory;
import com.energyict.mdc.protocol.api.device.messages.LoadProfileMode;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.PropertySpec;

import java.util.ArrayList;
import java.util.List;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.capturePeriodAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.consumerProducerModeAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.fromDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.loadProfileAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.toDateAttributeName;

/**
 * Provides a summary of all DeviceMessages related to LoadProfiles and their configuration
 * <p/>
 * Copyrights EnergyICT
 * Date: 2/05/13
 * Time: 10:44
 */
public enum LoadProfileMessage implements DeviceMessageSpecEnum {

    PARTIAL_LOAD_PROFILE_REQUEST(DeviceMessageId.LOAD_PROFILE_PARTIAL_REQUEST, "Partial load profile request") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(loadProfileAttributeName, true, FactoryIds.LOADPROFILE));
            propertySpecs.add(propertySpecService.basicPropertySpec(fromDateAttributeName, true, new DateAndTimeFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(toDateAttributeName, true, new DateAndTimeFactory()));
        }
    },
    ResetActiveImportLP(DeviceMessageId.LOAD_PROFILE_RESET_ACTIVE_IMPORT, "Reset active import load profile"),
    ResetActiveExportLP(DeviceMessageId.LOAD_PROFILE_RESET_ACTIVE_EXPORT, "Reset active export load profile"),
    ResetDailyProfile(DeviceMessageId.LOAD_PROFILE_RESET_DAILY, "Reset daily laod provile"),
    ResetMonthlyProfile(DeviceMessageId.LOAD_PROFILE_RESET_MONTHLY, "Reset monthly load profile"),
    WRITE_CAPTURE_PERIOD_LP1(DeviceMessageId.LOAD_PROFILE_WRITE_CAPTURE_PERIOD_LP1, "Write capture period of load profile 1") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(capturePeriodAttributeName, true, new TimeDurationValueFactory()));
        }
    },
    WRITE_CAPTURE_PERIOD_LP2(DeviceMessageId.LOAD_PROFILE_WRITE_CAPTURE_PERIOD_LP2, "Write capture period of load profile 2") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(capturePeriodAttributeName, true, new TimeDurationValueFactory()));
        }
    },
    WriteConsumerProducerMode(DeviceMessageId.LOAD_PROFILE_WRITE_CONSUMER_PRODUCER_MODE, "Write consumer producer mode") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpecWithValues(consumerProducerModeAttributeName, true, LoadProfileMode.getAllDescriptions()));
        }
    },
    LOAD_PROFILE_REGISTER_REQUEST(DeviceMessageId.LOAD_PROFILE_REGISTER_REQUEST, "Load profile register request") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(loadProfileAttributeName, true, FactoryIds.LOADPROFILE));
            propertySpecs.add(propertySpecService.basicPropertySpec(fromDateAttributeName, true, new DateAndTimeFactory()));
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    LoadProfileMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return LoadProfileMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultTranslation;
    }

    @Override
    public DeviceMessageId getId() {
        return this.id;
    }

    public final List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, propertySpecService);
        return propertySpecs;
    }

    protected void addPropertySpecs (List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
        // Default behavior is not to add anything
    };

    public final PropertySpec getPropertySpec(String name, PropertySpecService propertySpecService) {
        for (PropertySpec securityProperty : getPropertySpecs(propertySpecService)) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

}