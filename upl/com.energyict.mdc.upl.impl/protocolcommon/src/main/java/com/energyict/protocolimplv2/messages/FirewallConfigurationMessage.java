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
 * Provides a summary of all messages related to general Device Actions
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 11:59
 */
public enum FirewallConfigurationMessage implements DeviceMessageSpecSupplier {

    ActivateFirewall(34001, "Activate firewall") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList() ;
        }
    },
    DeactivateFirewall(34002, "Deactivate firewall") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList() ;
        }
    },
    ConfigureFWWAN(34003, "Configure WAN firewall") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.booleanSpec(service, DeviceMessageConstants.EnableDLMS, DeviceMessageConstants.EnableDLMSDefaultTranslation),
                    this.booleanSpec(service, DeviceMessageConstants.EnableHTTP, DeviceMessageConstants.EnableHTTPDefaultTranslation),
                    this.booleanSpec(service, DeviceMessageConstants.EnableSSH, DeviceMessageConstants.EnableSSHDefaultTranslation)
            );
        }
    },
    ConfigureFWLAN(34004, "Configure LAN firewall") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.booleanSpec(service, DeviceMessageConstants.EnableDLMS, DeviceMessageConstants.EnableDLMSDefaultTranslation),
                    this.booleanSpec(service, DeviceMessageConstants.EnableHTTP, DeviceMessageConstants.EnableHTTPDefaultTranslation),
                    this.booleanSpec(service, DeviceMessageConstants.EnableSSH, DeviceMessageConstants.EnableSSHDefaultTranslation)
            );
        }
    },
    ConfigureFWGPRS(34005, "Configure GPRS firewall") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.booleanSpec(service, DeviceMessageConstants.EnableDLMS, DeviceMessageConstants.EnableDLMSDefaultTranslation),
                    this.booleanSpec(service, DeviceMessageConstants.EnableHTTP, DeviceMessageConstants.EnableHTTPDefaultTranslation),
                    this.booleanSpec(service, DeviceMessageConstants.EnableSSH, DeviceMessageConstants.EnableSSHDefaultTranslation)
            );
        }
    },
    SetFWDefaultState(34006, "Set default state for firewall") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.booleanSpec(service, DeviceMessageConstants.defaultEnabled, DeviceMessageConstants.defaultEnabledDefaultTranslation));
        }
    };

    private final long id;
    private final String defaultNameTranslation;

    FirewallConfigurationMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

    protected PropertySpec booleanSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .booleanSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    private String getNameResourceKey() {
        return FirewallConfigurationMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.FIREWALL_CONFIGURATION,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService);
    }

}