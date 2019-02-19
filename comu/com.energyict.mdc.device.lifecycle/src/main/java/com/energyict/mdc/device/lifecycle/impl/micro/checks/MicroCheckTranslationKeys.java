/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.nls.TranslationKey;

public enum MicroCheckTranslationKeys implements TranslationKey {

    MICRO_CHECK_NAME_AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE(Keys.NAME_PREFIX + ActiveConnectionAvailable.class.getSimpleName(), "At least one active connection available"),
    MICRO_CHECK_NAME_ALL_DATA_VALID(Keys.NAME_PREFIX + AllDataValid.class.getSimpleName(), "All data valid"),
    MICRO_CHECK_NAME_ALL_DATA_VALIDATED(Keys.NAME_PREFIX + AllDataValidated.class.getSimpleName(), "All data validated"),
    MICRO_CHECK_NAME_ALL_ISSUES_AND_ALARMS_ARE_CLOSED(Keys.NAME_PREFIX + AllIssuesAreClosed.class.getSimpleName(), "All issues and alarms closed"),
    MICRO_CHECK_NAME_ALL_DATA_COLLECTED(Keys.NAME_PREFIX + AllLoadProfileDataCollected.class.getSimpleName(), "All load profile data collected"),
    MICRO_CHECK_NAME_MANDATORY_COMMUNICATION_ATTRIBUTES_AVAILABLE(Keys.NAME_PREFIX + "MANDATORY_COMMUNICATION_ATTRIBUTES_AVAILABLE", "Mandatory communication attributes available"),
    MICRO_CHECK_NAME_DEFAULT_CONNECTION_AVAILABLE(Keys.NAME_PREFIX + DefaultConnectionTaskAvailable.class.getSimpleName(), "Default connection available"),
    MICRO_CHECK_NAME_LINKED_WITH_USAGE_POINT(Keys.NAME_PREFIX + DeviceIsLinkedWithUsagePoint.class.getSimpleName(), "Connected to usage point"),
    MICRO_CHECK_NAME_METROLOGY_CONFIGURATION_IN_CORRECT_STATE_IF_ANY(Keys.NAME_PREFIX + MetrologyConfigurationInCorrectStateIfAny.class.getSimpleName(), "This device is linked to an usage point and cannot be moved from its operational life cycle stage. A metrology configuration is active on the usage point at the moment of the life cycle transition."),
    MICRO_CHECK_NAME_NO_ACTIVE_SERVICE_CALLS(Keys.NAME_PREFIX + NoActiveServiceCalls.class.getSimpleName(), "No active service calls"),
    MICRO_CHECK_NAME_NO_LINKED_MULTI_ELEMENT_SLAVES(Keys.NAME_PREFIX + NoLinkedOperationalMultiElementSlaves.class.getSimpleName(), "No operational slave devices"),
    MICRO_CHECK_NAME_AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE(Keys.NAME_PREFIX + ScheduledCommunicationTaskAvailable.class.getSimpleName(), "At least one scheduled communication task"),
    MICRO_CHECK_NAME_AT_LEAST_ONE_SHARED_COMMUNICATION_SCHEDULE_AVAILABLE(Keys.NAME_PREFIX + SharedScheduledCommunicationTaskAvailable.class.getSimpleName(), "At least one shared communication schedule"),
    MICRO_CHECK_NAME_SLAVE_DEVICE_HAS_GATEWAY(Keys.NAME_PREFIX + SlaveDeviceHasGateway.class.getSimpleName(), "Check for master device"),

    MICRO_CHECK_MESSAGE_AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE(Keys.MESSAGE_PREFIX + ActiveConnectionAvailable.class.getSimpleName(), "There should at least one active connection on the device"),
    MICRO_CHECK_MESSAGE_ALL_DATA_VALID(Keys.MESSAGE_PREFIX + AllDataValid.class.getSimpleName(), "All the collected data on the device must be valid"),
    MICRO_CHECK_MESSAGE_ALL_DATA_VALIDATED(Keys.MESSAGE_PREFIX + AllDataValidated.class.getSimpleName(), "All the collected data on the device is validated"),
    MICRO_CHECK_MESSAGE_ALL_ISSUES_AND_ALARMS_ARE_CLOSED(Keys.MESSAGE_PREFIX + AllIssuesAreClosed.class.getSimpleName(), "All issues and alarms must have been closed or resolved on the device"),
    MICRO_CHECK_MESSAGE_ALL_LOAD_PROFILE_DATA_COLLECTED(Keys.MESSAGE_PREFIX + AllLoadProfileDataCollected.class.getSimpleName(), "All the data on the device must have been collected"),
    MICRO_CHECK_MESSAGE_CONNECTION_PROPERTIES_ARE_ALL_VALID(Keys.MESSAGE_PREFIX + ConnectionPropertiesAreValid.class.getSimpleName(), "All mandatory connection method properties should be valid and specified"),
    MICRO_CHECK_MESSAGE_DEFAULT_CONNECTION_AVAILABLE(Keys.MESSAGE_PREFIX + DefaultConnectionTaskAvailable.class.getSimpleName(), "There should at least be a default connection task"),
    MICRO_CHECK_MESSAGE_LINKED_WITH_USAGE_POINT(Keys.MESSAGE_PREFIX + DeviceIsLinkedWithUsagePoint.class.getSimpleName(), "A device must be linked to a usage point"),
    MICRO_CHECK_MESSAGE_GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID(Keys.MESSAGE_PREFIX + GeneralProtocolPropertiesAreValid.class.getSimpleName(), "All mandatory general protocol properties should be valid and specified"),
    MICRO_CHECK_MESSAGE_METROLOGY_CONFIGURATION_IN_CORRECT_STATE_IF_ANY(Keys.MESSAGE_PREFIX + MetrologyConfigurationInCorrectStateIfAny.class.getSimpleName(), "This device is linked to an usage point and cannot be moved from its operational life cycle stage. A metrology configuration is active on the usage point at the moment of the life cycle transition."),
    MICRO_CHECK_MESSAGE_NO_ACTIVE_SERVICE_CALLS(Keys.MESSAGE_PREFIX + NoActiveServiceCalls.class.getSimpleName(), "There should not be any active service calls on the device"),
    MICRO_CHECK_MESSAGE_NO_LINKED_MULTI_ELEMENT_SLAVES(Keys.MESSAGE_PREFIX + NoLinkedOperationalMultiElementSlaves.class.getSimpleName(), "There can't be any operational multi-element slaves linked with this device"),
    MICRO_CHECK_MESSAGE_PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID(Keys.MESSAGE_PREFIX + ProtocolDialectPropertiesAreValid.class.getSimpleName(), "All mandatory protocol dialect properties should be valid and specified"),
    MICRO_CHECK_MESSAGE_AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE(Keys.MESSAGE_PREFIX + ScheduledCommunicationTaskAvailable.class.getSimpleName(), "At least one communication task has been scheduled"),
    MICRO_CHECK_MESSAGE_SECURITY_PROPERTIES_ARE_ALL_VALID(Keys.MESSAGE_PREFIX + SecurityPropertiesAreValid.class.getSimpleName(), "All mandatory security properties should be valid and specified"),
    MICRO_CHECK_MESSAGE_AT_LEAST_ONE_SHARED_COMMUNICATION_SCHEDULE_AVAILABLE(Keys.MESSAGE_PREFIX + SharedScheduledCommunicationTaskAvailable.class.getSimpleName(), "At least one shared communication schedule should be available on the device"),
    MICRO_CHECK_MESSAGE_SLAVE_DEVICE_HAS_GATEWAY(Keys.MESSAGE_PREFIX + SlaveDeviceHasGateway.class.getSimpleName(), "A slave device must have a gateway device"),

    MICRO_CHECK_DESCRIPTION_AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE(Keys.DESCRIPTION_PREFIX + ActiveConnectionAvailable.class.getSimpleName(), "Check if at least one connection is available on the device with the status: 'Active'"),
    MICRO_CHECK_DESCRIPTION_ALL_DATA_VALID(Keys.DESCRIPTION_PREFIX + AllDataValid.class.getSimpleName(), "Check if all the collected data is valid"),
    MICRO_CHECK_DESCRIPTION_ALL_DATA_VALIDATED(Keys.DESCRIPTION_PREFIX + AllDataValidated.class.getSimpleName(), "Check if all the collected data is validated."),
    MICRO_CHECK_DESCRIPTION_ALL_ISSUES_AND_ALARMS_ARE_CLOSED(Keys.DESCRIPTION_PREFIX + AllIssuesAreClosed.class.getSimpleName(), "Check if all the issues and alarms on this device are closed."),
    MICRO_CHECK_DESCRIPTION_ALL_DATA_COLLECTED(Keys.DESCRIPTION_PREFIX + AllLoadProfileDataCollected.class.getSimpleName(), "Check if all load profile data of this device has been collected."),
    MICRO_CHECK_DESCRIPTION_MANDATORY_COMMUNICATION_ATTRIBUTES_AVAILABLE(Keys.DESCRIPTION_PREFIX + "MANDATORY_COMMUNICATION_ATTRIBUTES_AVAILABLE", "Check if the mandatory communication attributes are available on the device: protocol dialect attributes, security setting attributes, connection attributes, general attributes."),
    MICRO_CHECK_DESCRIPTION_DEFAULT_CONNECTION_AVAILABLE(Keys.DESCRIPTION_PREFIX + DefaultConnectionTaskAvailable.class.getSimpleName(), "Check if a default connection is available on the device."),
    MICRO_CHECK_DESCRIPTION_LINKED_WITH_USAGE_POINT(Keys.DESCRIPTION_PREFIX + DeviceIsLinkedWithUsagePoint.class.getSimpleName(), "Check if this device is connected to a usage point."),
    MICRO_CHECK_DESCRIPTION_METROLOGY_CONFIGURATION_IN_CORRECT_STATE_IF_ANY(Keys.DESCRIPTION_PREFIX + MetrologyConfigurationInCorrectStateIfAny.class.getSimpleName(), "Check if the device is linked to a usage point that has an active metrology configuration"),
    MICRO_CHECK_DESCRIPTION_NO_ACTIVE_SERVICE_CALLS(Keys.DESCRIPTION_PREFIX + NoActiveServiceCalls.class.getSimpleName(), "Check that no service calls are active for the device."),
    MICRO_CHECK_DESCRIPTION_NO_LINKED_MULTI_ELEMENT_SLAVES(Keys.DESCRIPTION_PREFIX + NoLinkedOperationalMultiElementSlaves.class.getSimpleName(), "Check if there are no multi element slaves in an operational stage"),
    MICRO_CHECK_DESCRIPTION_AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE(Keys.DESCRIPTION_PREFIX + ScheduledCommunicationTaskAvailable.class.getSimpleName(), "Check if at least one communication task has been scheduled."),
    MICRO_CHECK_DESCRIPTION_AT_LEAST_ONE_SHARED_COMMUNICATION_SCHEDULE_AVAILABLE(Keys.DESCRIPTION_PREFIX + SharedScheduledCommunicationTaskAvailable.class.getSimpleName(), "Check if at least one shared communication schedule has been added to the device"),
    MICRO_CHECK_DESCRIPTION_SLAVE_DEVICE_HAS_GATEWAY(Keys.DESCRIPTION_PREFIX + SlaveDeviceHasGateway.class.getSimpleName(), "If this device is a slave, check if the device has been linked to a master device."),

    ;

    private final String key;
    private final String defaultFormat;

    MicroCheckTranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    public static class Keys {

        static String NAME_PREFIX = "transition.micro.check.name.";
        static String MESSAGE_PREFIX = "transition.micro.check.message.";
        static String DESCRIPTION_PREFIX = "transition.micro.check.description.";

        private Keys() {
        }
    }
}