package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum EIWebConfigurationDeviceMessage implements DeviceMessageSpecEnum {

    SetEIWebPassword(DeviceMessageId.EIWEB_SET_PASSWORD, "Set EIWeb password") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetEIWebPasswordAttributeName;
        }
    },
    SetEIWebPage(DeviceMessageId.EIWEB_SET_PAGE, "Set EIWeb web page") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetEIWebPageAttributeName;
        }
    },
    SetEIWebFallbackPage(DeviceMessageId.EIWEB_SET_FALLBACK_PAGE, "Set EIWeb fallback page") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetEIWebFallbackPageAttributeName;
        }
    },
    SetEIWebSendEvery(DeviceMessageId.EIWEB_SET_SEND_EVERY, "Set EIWeb send every") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetEIWebSendEveryAttributeName;
        }
    },
    SetEIWebCurrentInterval(DeviceMessageId.EIWEB_SET_CURRENT_INTERVAL, "Set EIWeb current interval") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetEIWebCurrentIntervalAttributeName;
        }
    },
    SetEIWebDatabaseID(DeviceMessageId.EIWEB_SET_DATABASE_ID, "Set EIWeb database ID") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetEIWebDatabaseIDAttributeName;
        }
    },
    SetEIWebOptions(DeviceMessageId.EIWEB_SET_OPTIONS, "Set EIWeb web options") {
        @Override
        protected String propertyName() {
            return DeviceMessageConstants.SetEIWebOptionsAttributeName;
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    EIWebConfigurationDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return EIWebConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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
        propertySpecs.add(propertySpecService.bigDecimalPropertySpecWithValues(DeviceMessageConstants.id, true, BigDecimal.ONE, BigDecimals.TWO));
        propertySpecs.add(this.stringProperty(this.propertyName(), propertySpecService));
        return propertySpecs;
    }

    private PropertySpec stringProperty(String name, PropertySpecService propertySpecService) {
        return propertySpecService.basicPropertySpec(name, true, new StringFactory());
    }

    protected abstract String propertyName();

    public final PropertySpec getPropertySpec(String name, PropertySpecService propertySpecService) {
        for (PropertySpec securityProperty : getPropertySpecs(propertySpecService)) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

}