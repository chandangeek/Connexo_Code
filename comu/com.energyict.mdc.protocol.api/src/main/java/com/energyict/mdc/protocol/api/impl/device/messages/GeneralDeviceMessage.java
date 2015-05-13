package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.dynamic.HexStringFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.PropertySpec;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.IEC1107ClassIdAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.OffsetAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.RawDataAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.configUserFileAttributeName;

/**
 * Provides a summary of all messages that have no unique goal.
 * For example, this can be a message that writes a general value to a certain DLMS object, chosen by the user (obiscode).
 * <p/>
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/04/13
 * Time: 8:38
 */
public enum GeneralDeviceMessage implements DeviceMessageSpecEnum {

    WRITE_RAW_IEC1107_CLASS(DeviceMessageId.GENERAL_WRITE_RAW_IEC1107_CLASS, "Write raw IEC1107 class") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.boundedDecimalPropertySpec(IEC1107ClassIdAttributeName, true, BigDecimal.ZERO, Constants.UPPER_LIMIT));
            propertySpecs.add(propertySpecService.boundedDecimalPropertySpec(OffsetAttributeName, true, BigDecimal.ZERO, Constants.UPPER_LIMIT));
            propertySpecs.add(propertySpecService.basicPropertySpec(RawDataAttributeName, true, new HexStringFactory()));
        }
    },
    WRITE_FULL_CONFIGURATION(DeviceMessageId.GENERAL_WRITE_FULL_CONFIGURATION, "Write full configuration") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(configUserFileAttributeName, true, FactoryIds.USERFILE));
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    GeneralDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return GeneralDeviceMessage.class.getSimpleName() + "." + this.toString();
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

    private static class Constants {
        private static final BigDecimal UPPER_LIMIT = BigDecimal.valueOf(9999);
    }

}