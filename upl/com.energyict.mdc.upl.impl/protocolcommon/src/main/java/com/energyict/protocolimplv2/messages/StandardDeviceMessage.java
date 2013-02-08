package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessagePropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpecSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Defines a set of all standard Device messages
 * <p/>
 * Copyrights EnergyICT
 * Date: 18/12/12
 * Time: 16:37
 */
public enum StandardDeviceMessage implements DeviceMessageSpecSet {

    CONNECT_WITHOUT_PARAMETERS(StandardDeviceMessageCategory.CONTACTOR),
    CONNECT_WITH_OUTPUTS(StandardDeviceMessageCategory.CONTACTOR, DeviceMessagePropertySpec.CONTACTOR_OUTPUT),
    CONNECT_ON_DATE(StandardDeviceMessageCategory.CONTACTOR, DeviceMessagePropertySpec.CONTACTOR_DATE),
    CONNECT_ON_DATE_WITH_OUTPUTS(StandardDeviceMessageCategory.CONTACTOR,
            DeviceMessagePropertySpec.CONTACTOR_OUTPUT, DeviceMessagePropertySpec.CONTACTOR_DATE),

    CONTACTOR_ARM_WITHOUT_PARAMETERS(StandardDeviceMessageCategory.CONTACTOR),
    CONTACTOR_ARM_WITH_OUTPUTS(StandardDeviceMessageCategory.CONTACTOR, DeviceMessagePropertySpec.CONTACTOR_OUTPUT),
    CONTACTOR_ARM_ON_DATE(StandardDeviceMessageCategory.CONTACTOR, DeviceMessagePropertySpec.CONTACTOR_DATE),
    CONTACTOR_ON_DATE_WITH_OUTPUTS(StandardDeviceMessageCategory.CONTACTOR,
            DeviceMessagePropertySpec.CONTACTOR_OUTPUT, DeviceMessagePropertySpec.CONTACTOR_DATE),

    DISCONNECT_WITHOUT_PARAMETERS(StandardDeviceMessageCategory.CONTACTOR),
    DISCONNECT_WITH_OUTPUTS(StandardDeviceMessageCategory.CONTACTOR, DeviceMessagePropertySpec.CONTACTOR_OUTPUT),
    DISCONNECT_ON_DATE(StandardDeviceMessageCategory.CONTACTOR, DeviceMessagePropertySpec.CONTACTOR_DATE),
    DISCONNECT_ON_DATE_WITH_OUTPUTS(StandardDeviceMessageCategory.CONTACTOR,
            DeviceMessagePropertySpec.CONTACTOR_OUTPUT, DeviceMessagePropertySpec.CONTACTOR_DATE),

    CHANGE_CONTACTOR_MODE(StandardDeviceMessageCategory.CONTACTOR, DeviceMessagePropertySpec.CONTACTOR_MODE),

    RESET(StandardDeviceMessageCategory.RESET) {
        @Override
        public StandardDeviceMessage addPredefinedPropertySpecs(DeviceMessagePropertySpec... predefinedPropertySpecs) {
            return doAddPredefinedPropertySpecs(predefinedPropertySpecs);
        }
    },

    ACTIVITY_CALENDER_SEND(StandardDeviceMessageCategory.ACTIVITY_CALENDAR, DeviceMessagePropertySpec.ACTIVITY_CALENDAR_CODE_TABLE),
    SPECIAL_DAYS_SEND(StandardDeviceMessageCategory.ACTIVITY_CALENDAR, DeviceMessagePropertySpec.ACTIVITY_CALENDAR_CODE_TABLE),
    ACTIVITY_CALENDER_SEND_WITH_DATE(StandardDeviceMessageCategory.ACTIVITY_CALENDAR,
            DeviceMessagePropertySpec.ACTIVITY_CALENDAR_CODE_TABLE, DeviceMessagePropertySpec.ACTIVITY_CALENDAR_ACTIVATION_DATE),
    ACTIVITY_CALENDAR_ACTIVATE(StandardDeviceMessageCategory.ACTIVITY_CALENDAR),
    ACTIVITY_CALENDAR_ACTIVATE_WITH_DATE(
            StandardDeviceMessageCategory.ACTIVITY_CALENDAR, DeviceMessagePropertySpec.ACTIVITY_CALENDAR_ACTIVATION_DATE),

    ;

    private final DeviceMessageCategory deviceMessageCategory;
    private List<DeviceMessagePropertySpec> deviceMessagePropertySpecs;

    private StandardDeviceMessage(DeviceMessageCategory deviceMessageCategory, DeviceMessagePropertySpec... deviceMessagePropertySpecs) {
        this.deviceMessageCategory = deviceMessageCategory;
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public List<DeviceMessagePropertySpec> getDeviceMessagePropertySpecs() {
        return deviceMessagePropertySpecs;
    }

    @Override
    public DeviceMessageCategory getDeviceMessageCategory(){
        return this.deviceMessageCategory;
    }

    /**
     * Add one or more DeviceMessagePropertySpecs which have predefined values, which are understood
     * by your DeviceProtocol.
     * <p/>
     * Only certain messages allow to add predefined values. If this message does not support it,
     * it will throw an {@link IllegalStateException}, be warned.
     *
     * @param predefinedPropertySpecs the propertySpecs to add to this message
     * @return the initial StandardDeviceMessage with the added properties
     */
    public StandardDeviceMessage addPredefinedPropertySpecs(DeviceMessagePropertySpec... predefinedPropertySpecs) {
        throw new IllegalStateException("Adding predefined property specs is only allowed for certain messages, but not for " + this);
    }

    /**
     * Checks if the given predefinedPropertySpecs indeed contain predefined values and add them to the
     * corresponding propertySpecList
     *
     * @param predefinedPropertySpecs the specs to verify
     * @return this enumeration with the added specs
     */
    protected StandardDeviceMessage doAddPredefinedPropertySpecs(DeviceMessagePropertySpec[] predefinedPropertySpecs) {
        if (this.deviceMessagePropertySpecs == null) {
            this.deviceMessagePropertySpecs = new ArrayList<>();
        }
        checkForPredefinedValues(predefinedPropertySpecs);
        Collections.addAll(this.deviceMessagePropertySpecs, predefinedPropertySpecs);
        return this;
    }

    /**
     * Checks whether the deviceMessagePropertySpecs have predefined values.
     *
     * @param predefinedPropertySpecs the specs to check if they have predefined values
     */
    private void checkForPredefinedValues(DeviceMessagePropertySpec[] predefinedPropertySpecs) {
        for (DeviceMessagePropertySpec predefinedPropertySpec : predefinedPropertySpecs) {
            if (predefinedPropertySpec.getPropertySpec().getPossibleValues() == null) {
                throw new IllegalArgumentException("Predefined values should have been defined for the propertySpec " + predefinedPropertySpec);
            }
        }
    }
}