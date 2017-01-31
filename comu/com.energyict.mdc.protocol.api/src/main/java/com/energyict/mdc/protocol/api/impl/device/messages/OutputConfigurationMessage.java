/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

enum OutputConfigurationMessage implements DeviceMessageSpecEnum {

    SetOutputOn(DeviceMessageId.OUTPUT_CONFIGURATION_SET_OUTPUT_ON, "Set output on"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringSpec(DeviceMessageAttributes.OutputOn, propertySpecService, thesaurus));
        }
    },
    SetOutputOff(DeviceMessageId.OUTPUT_CONFIGURATION_SET_OUTPUT_OFF, "Set output off"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringSpec(DeviceMessageAttributes.OutputOff, propertySpecService, thesaurus));
        }
    } ,
    SetOutputToggle(DeviceMessageId.OUTPUT_CONFIGURATION_SET_OUTPUT_TOGGLE, "Set output toggle"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringSpec(DeviceMessageAttributes.OutputToggle, propertySpecService, thesaurus));
        }
    },
    SetOutputPulse(DeviceMessageId.OUTPUT_CONFIGURATION_SET_OUTPUT_PULSE, "Set output pulse"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringSpec(DeviceMessageAttributes.OutputPulse, propertySpecService, thesaurus));
        }
    },
    OutputOff(DeviceMessageId.OUTPUT_CONFIGURATION_OUTPUT_OFF, "Output off"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringSpec(DeviceMessageAttributes.output, propertySpecService, thesaurus));
        }
    } ,
    OutputOn(DeviceMessageId.OUTPUT_CONFIGURATION_OUTPUT_ON, "Output on"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringSpec(DeviceMessageAttributes.output, propertySpecService, thesaurus));
        }
    } ,
    AbsoluteDOSwitchRule(DeviceMessageId.OUTPUT_CONFIGURATION_ABSOLUTE_DO_SWITCH_RULE, "Absolute DO switch rule"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageConstants.id, DeviceMessageAttributes.OutputConfigurationMessageId)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageConstants.startTime, DeviceMessageAttributes.startTime)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageConstants.endTime, DeviceMessageAttributes.endTime)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageConstants.outputBitMap, DeviceMessageAttributes.OutputConfigurationMessageOutputBitMap)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    DeleteDOSwitchRule(DeviceMessageId.OUTPUT_CONFIGURATION_DELETE_DO_SWITCH_RULE, "Delete DO switch rule"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageConstants.id, DeviceMessageAttributes.OutputConfigurationMessageId)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageConstants.delete, DeviceMessageAttributes.OutputConfigurationMessageDelete)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    RelativeDOSwitchRule(DeviceMessageId.OUTPUT_CONFIGURATION_RELATIVE_DO_SWITCH_RULE, "Relative DO switch rule"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageConstants.id, DeviceMessageAttributes.OutputConfigurationMessageId)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageConstants.duration, DeviceMessageAttributes.OutputConfigurationMessageDuration)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageConstants.outputBitMap, DeviceMessageAttributes.OutputConfigurationMessageOutputBitMap)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    WriteOutputState(DeviceMessageId.OUTPUT_CONFIGURATION_WRITE_OUTPUT_STATE, "Write output state"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DeviceMessageAttributes.OutputConfigurationMessageOutputId)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .booleanSpec()
                            .named(DeviceMessageAttributes.OutputConfigurationMessageNewState)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    };

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

    public final List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
        return propertySpecs;
    }

    protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        // Default behavior is not to add anything
    };

    protected PropertySpec stringSpec(DeviceMessageAttributes name, PropertySpecService service, Thesaurus thesaurus) {
        return service.stringSpec().named(name).fromThesaurus(thesaurus).markRequired().finish();
    }

}


