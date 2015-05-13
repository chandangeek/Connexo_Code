package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 12/8/14
 * Time: 12:11 PM
 */
public enum FirewallConfigurationMessage implements DeviceMessageSpecEnum {

    ActivateFirewall(DeviceMessageId.FIREWALL_ACTIVATE_FIREWALL, "Activate the firewall"),
    DeactivateFirewall(DeviceMessageId.FIREWALL_DEACTIVATE_FIREWALL, "Deactivate the firewall"),
    ConfigureFWWAN(DeviceMessageId.FIREWALL_CONFIGURE_FW_WAN, "Configure the WAN firewall") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.EnableDLMS, true, new BooleanFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.EnableHTTP, true, new BooleanFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.EnableSSH, true, new BooleanFactory()));
        }
    },
    ConfigureFWLAN(DeviceMessageId.FIREWALL_CONFIGURE_FW_LAN, "Configure the LAN firewall") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.EnableDLMS, true, new BooleanFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.EnableHTTP, true, new BooleanFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.EnableSSH, true, new BooleanFactory()));
        }
    },
    ConfigureFWGPRS(DeviceMessageId.FIREWALL_CONFIGURE_FW_GPRS, "Configure the GPRS firewall") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.EnableDLMS, true, new BooleanFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.EnableHTTP, true, new BooleanFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.EnableSSH, true, new BooleanFactory()));
        }
    },
    SetFWDefaultState(DeviceMessageId.FIREWALL_SET_FW_DEFAULT_STATE, "Set default") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.defaultEnabled, true, new BooleanFactory()));
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    FirewallConfigurationMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    public String getKey() {
        return FirewallConfigurationMessage.class.getSimpleName() + "." + this.toString();
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

    protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
        // Default behavior is not to add anything
    }

    ;

    @Override
    public final PropertySpec getPropertySpec(String name, PropertySpecService propertySpecService) {
        for (PropertySpec securityProperty : getPropertySpecs(propertySpecService)) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

}


