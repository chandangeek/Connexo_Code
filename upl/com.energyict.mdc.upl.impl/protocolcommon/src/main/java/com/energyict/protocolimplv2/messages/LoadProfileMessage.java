package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.meterdata.LoadProfile;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.enums.LoadProfileMode;
import com.energyict.protocolimplv2.messages.enums.LoadProfileOptInOut;
import com.energyict.protocolimplv2.messages.enums.SetDisplayMode;
import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.capturePeriodAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.capturePeriodAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.consumerProducerModeAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.consumerProducerModeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.fromDateAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.fromDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.fromDateAttributeNameDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.loadProfileAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.loadProfileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.loadProfileOptInOutModeAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.loadProfileOptInOutModeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.setDisplayOnOffModeAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.setDisplayOnOffModeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.toDateAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.toDateAttributeName;

/**
 * Provides a summary of all DeviceMessages related to LoadProfiles and their configuration
 * <p/>
 * Copyrights EnergyICT
 * Date: 2/05/13
 * Time: 10:44
 */
public enum LoadProfileMessage implements DeviceMessageSpecSupplier {

    PARTIAL_LOAD_PROFILE_REQUEST(13001, "Partial load profile request") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.loadProfileSpec(service, loadProfileAttributeName, loadProfileAttributeDefaultTranslation),
                    this.dateTimeSpec(service, fromDateAttributeName, fromDateAttributeNameDefaultTranslation),
                    this.dateTimeSpec(service, toDateAttributeName, toDateAttributeDefaultTranslation)
            );
        }
    },
    ResetActiveImportLP(13002, "Reset active import load profile") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    ResetActiveExportLP(13002, "Reset active export load profile") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    ResetDailyProfile(13004, "Reset daily load profile") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    ResetMonthlyProfile(13005, "Reset monthly load profile") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    WRITE_CAPTURE_PERIOD_LP1(13006, "Write capture period of load profile 1") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.durationSpec(service, capturePeriodAttributeName, capturePeriodAttributeDefaultTranslation));
        }
    },
    WRITE_CAPTURE_PERIOD_LP2(13007, "Write capture period of load profile 2") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.durationSpec(service, capturePeriodAttributeName, capturePeriodAttributeDefaultTranslation));
        }
    },
    WriteConsumerProducerMode(13008, "Write consumer producer mode") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, consumerProducerModeAttributeName, consumerProducerModeAttributeDefaultTranslation, LoadProfileMode.getAllDescriptions()));
        }
    },
    LOAD_PROFILE_REGISTER_REQUEST(13009, "Load profile register request") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.loadProfileSpec(service, loadProfileAttributeName, loadProfileAttributeDefaultTranslation),
                    this.dateTimeSpec(service, fromDateAttributeName, fromDateAttributeDefaultTranslation)
            );
        }
    },
    READ_PROFILE_DATA(13010, "Read profile data") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.dateTimeSpec(service, fromDateAttributeName, fromDateAttributeDefaultTranslation),
                    this.dateTimeSpec(service, toDateAttributeName, toDateAttributeDefaultTranslation)
            );
        }
    },
    LOAD_PROFILE_OPT_IN_OUT(13011, "Load profile Opt In/Opt Out") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, loadProfileOptInOutModeAttributeName, loadProfileOptInOutModeAttributeDefaultTranslation, LoadProfileOptInOut.getScriptNames()));
        }
    },
    SET_DISPLAY_ON_OFF(13012, "Set display on/off") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, setDisplayOnOffModeAttributeName, setDisplayOnOffModeAttributeDefaultTranslation, SetDisplayMode.getModeNames()));
        }
    };

    private final long id;
    private final String defaultNameTranslation;

    LoadProfileMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    protected PropertySpec stringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, String... exhaustiveValues) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .addValues(exhaustiveValues)
                .markExhaustive()
                .markRequired()
                .finish();
    }

    protected PropertySpec durationSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .durationSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    protected PropertySpec dateTimeSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .dateTimeSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    protected PropertySpec loadProfileSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .referenceSpec(LoadProfile.class.getName())
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    private String getNameResourceKey() {
        return LoadProfileMessage.class.getSimpleName() + "." + this.toString();
    }

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.LOAD_PROFILES,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService);
    }

}