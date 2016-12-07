package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum LoggingConfigurationDeviceMessage implements DeviceMessageSpecSupplier {

    DownloadFile(0, "Download file") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.fileInfo, DeviceMessageConstants.fileInfoDefaultTranslation));
        }
    },
    PushConfiguration(1, "Push the configuration files") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    PushLogsNow(2, "Push the log files now") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    SetServerLogLevel(3, "Set server log level") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.boundedBigDecimalSpec(
                            service,
                            DeviceMessageConstants.logLevel, DeviceMessageConstants.logLevelDefaultTranslation,
                            BigDecimal.ZERO, BigDecimal.valueOf(7)));
        }
    },
    SetWebPortalLogLevel(4, "Set web portal log level") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.boundedBigDecimalSpec(
                            service,
                            DeviceMessageConstants.logLevel, DeviceMessageConstants.logLevelDefaultTranslation,
                            BigDecimal.ZERO, BigDecimal.valueOf(7)));
        }
    };

    private final long id;
    private final String defaultNameTranslation;

    LoggingConfigurationDeviceMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    private String getNameResourceKey() {
        return LoggingConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

    protected PropertySpec boundedBigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, BigDecimal lowerLimit, BigDecimal upperLimit) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .boundedBigDecimalSpec(lowerLimit, upperLimit)
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    protected PropertySpec stringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                this.id,
                new EnumBasedDeviceMessageSpecPrimaryKey(this, name()),
                new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.LOGGING_CONFIGURATION,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService);
    }

}