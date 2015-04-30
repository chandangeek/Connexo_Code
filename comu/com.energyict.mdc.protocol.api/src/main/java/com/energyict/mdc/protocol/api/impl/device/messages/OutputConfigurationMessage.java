package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 12/8/14
 * Time: 12:07 PM
 */
public enum OutputConfigurationMessage implements DeviceMessageSpecEnum {

//    SET_RELAY_OPERATING_MODE(DeviceMessageId.PUBLIC_LIGHTING_SET_RELAY_OPERATING_MODE, "Public lighting set relay operating mode"){
//        @Override
//        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
//            super.addPropertySpecs(propertySpecs, propertySpecService);
//            propertySpecs.add(propertySpecService.bigDecimalPropertySpecWithValues(relayNumberAttributeName, true, BigDecimal.ONE, BigDecimals.TWO));
//            propertySpecs.add(propertySpecService.bigDecimalPropertySpecWithValues(relayOperatingModeAttributeName, true, BigDecimal.ZERO, BigDecimal.ONE, BigDecimals.TWO, BigDecimals.THREE));
//        }
//    },

    SetOutputOn(DeviceMessageId.OUTPUT_CONFIGURATION_SET_OUTPUT_ON, "Set output on"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpec(DeviceMessageConstants.OutputOn, true, ""));
        }
    },
    SetOutputOff(DeviceMessageId.OUTPUT_CONFIGURATION_SET_OUTPUT_OFF, "Set output off"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpec(DeviceMessageConstants.OutputOff, true, ""));
        }
    } ,
    SetOutputToggle(DeviceMessageId.OUTPUT_CONFIGURATION_SET_OUTPUT_TOGGLE, "Set output toggle"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpec(DeviceMessageConstants.OutputToggle, true, ""));
        }
    },
    SetOutputPulse(DeviceMessageId.OUTPUT_CONFIGURATION_SET_OUTPUT_PULSE, "Set output pulse"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpec(DeviceMessageConstants.OutputPulse, true, ""));
        }
    },
    OutputOff(DeviceMessageId.OUTPUT_CONFIGURATION_OUTPUT_OFF, "Output off"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpec(DeviceMessageConstants.output, true, ""));
        }
    } ,
    OutputOn(DeviceMessageId.OUTPUT_CONFIGURATION_OUTPUT_ON, "Output on"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpec(DeviceMessageConstants.output, true, ""));
        }
    } ,
    AbsoluteDOSwitchRule(DeviceMessageId.OUTPUT_CONFIGURATION_ABSOLUTE_DO_SWITCH_RULE, "Absolute DO switch rule"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpec(DeviceMessageConstants.id, true, ""));
            propertySpecs.add(propertySpecService.stringPropertySpec(DeviceMessageConstants.startTime, true, ""));
            propertySpecs.add(propertySpecService.stringPropertySpec(DeviceMessageConstants.endTime, true, ""));
            propertySpecs.add(propertySpecService.stringPropertySpec(DeviceMessageConstants.outputBitMap, true, ""));
        }
    },
    DeleteDOSwitchRule(DeviceMessageId.OUTPUT_CONFIGURATION_DELETE_DO_SWITCH_RULE, "Delete DO switch rule"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpec(DeviceMessageConstants.id, true, ""));
            propertySpecs.add(propertySpecService.stringPropertySpec(DeviceMessageConstants.delete, true, ""));
        }
    },
    RelativeDOSwitchRule(DeviceMessageId.OUTPUT_CONFIGURATION_RELATIVE_DO_SWITCH_RULE, "Relative DO switch rule"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpec(DeviceMessageConstants.id, true, ""));
            propertySpecs.add(propertySpecService.stringPropertySpec(DeviceMessageConstants.duration, true, ""));
            propertySpecs.add(propertySpecService.stringPropertySpec(DeviceMessageConstants.outputBitMap, true, ""));
            propertySpecs.add(propertySpecService.stringPropertySpec(DeviceMessageConstants.outputBitMap, true, ""));
        }
    },
    WriteOutputState(DeviceMessageId.OUTPUT_CONFIGURATION_WRITE_OUTPUT_STATE, "Write output state"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.outputId, true, BigDecimal.ZERO));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.newState, true, new BooleanFactory()));
        }
    }

    ;

    private DeviceMessageId id;
    private String defaultTranslation;

    OutputConfigurationMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    public String getKey() {
        return OutputConfigurationMessage.class.getSimpleName() + "." + this.toString();
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


