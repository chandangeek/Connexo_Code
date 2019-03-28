/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheck;

import java.util.logging.Level;

public class MicroCheckTranslations {
    static final String NAME_PREFIX = "transition.micro.check.name.";
    static final String MESSAGE_PREFIX = "transition.micro.check.message.";
    static final String DESCRIPTION_PREFIX = "transition.micro.check.description.";

    public enum Name implements TranslationKey {
        AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE(ActiveConnectionAvailable.class, "At least one active connection available"),
        ALL_DATA_VALID(AllDataValid.class, "All data valid"),
        ALL_DATA_VALIDATED(AllDataValidated.class, "All data validated"),
        ALL_ISSUES_AND_ALARMS_ARE_CLOSED(AllIssuesAreClosed.class, "All issues and alarms closed"),
        ALL_DATA_COLLECTED(AllLoadProfileDataCollected.class, "All load profile data collected"),
        MANDATORY_COMMUNICATION_ATTRIBUTES_AVAILABLE("MANDATORY_COMMUNICATION_ATTRIBUTES_AVAILABLE", "Mandatory communication attributes available"),
        DEFAULT_CONNECTION_AVAILABLE(DefaultConnectionTaskAvailable.class, "Default connection available"),
        LINKED_WITH_USAGE_POINT(DeviceIsLinkedWithUsagePoint.class, "Connected to usage point"),
        METROLOGY_CONFIGURATION_IN_CORRECT_STATE_IF_ANY(MetrologyConfigurationInCorrectStateIfAny.class, "Device linked to a usage point with active metrology configuration isn''t moved from operational stage"),
        NO_ACTIVE_SERVICE_CALLS(NoActiveServiceCalls.class, "No active service calls"),
        NO_LINKED_MULTI_ELEMENT_SLAVES(NoLinkedOperationalMultiElementSlaves.class, "No operational slave devices"),
        AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE(ScheduledCommunicationTaskAvailable.class, "At least one scheduled communication task"),
        AT_LEAST_ONE_SHARED_COMMUNICATION_SCHEDULE_AVAILABLE(SharedScheduledCommunicationTaskAvailable.class, "At least one shared communication schedule"),
        SLAVE_DEVICE_HAS_GATEWAY(SlaveDeviceHasGateway.class, "Check for master device"),
        AT_LEAST_ONE_ZONE_LINKED(ZonesLinkedToDevice.class, "Device is linked to a zone");

        private final String key;
        private final String defaultFormat;

        Name(Class<? extends ExecutableMicroCheck> clazz, String defaultFormat) {
            this(clazz.getSimpleName(), defaultFormat);
        }

        Name(String key, String defaultFormat) {
            this.key = NAME_PREFIX + key;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getDefaultFormat() {
            return defaultFormat;
        }
    }

    public enum Message implements MessageSeed {
        AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE(10001, ActiveConnectionAvailable.class, "There should be at least one active connection on the device"),
        ALL_DATA_VALID(10002, AllDataValid.class, "All the collected data on the device must be valid"),
        ALL_DATA_VALIDATED(10003, AllDataValidated.class, "All the collected data on the device is validated"),
        ALL_ISSUES_AND_ALARMS_ARE_CLOSED(10004, AllIssuesAreClosed.class, "All issues and alarms must have been closed or resolved on the device"),
        ALL_LOAD_PROFILE_DATA_COLLECTED(10005, AllLoadProfileDataCollected.class, "All the data on the device must have been collected"),
        CONNECTION_PROPERTIES_ARE_ALL_VALID(10006, ConnectionPropertiesAreValid.class, "All mandatory connection method properties should be valid and specified"),
        DEFAULT_CONNECTION_AVAILABLE(10007, DefaultConnectionTaskAvailable.class, "There should at least be a default connection task"),
        LINKED_WITH_USAGE_POINT(10008, DeviceIsLinkedWithUsagePoint.class, "A device must be linked to a usage point"),
        GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID(10009, GeneralProtocolPropertiesAreValid.class, "All mandatory general protocol properties should be valid and specified"),
        METROLOGY_CONFIGURATION_IN_CORRECT_STATE_IF_ANY(10010, MetrologyConfigurationInCorrectStateIfAny.class, "This device is linked to a usage point and can''t be moved from its operational life cycle stage. A metrology configuration is active on the usage point at the moment of the life cycle transition."),
        NO_ACTIVE_SERVICE_CALLS(10011, NoActiveServiceCalls.class, "There should not be any active service calls on the device"),
        NO_LINKED_MULTI_ELEMENT_SLAVES(10012, NoLinkedOperationalMultiElementSlaves.class, "There can't be any operational multi-element slaves linked with this device"),
        PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID(10013, ProtocolDialectPropertiesAreValid.class, "All mandatory protocol dialect properties should be valid and specified"),
        AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE(10014, ScheduledCommunicationTaskAvailable.class, "At least one communication task has been scheduled"),
        SECURITY_PROPERTIES_ARE_ALL_VALID(10015, SecurityPropertiesAreValid.class, "All mandatory security properties should be valid and specified"),
        AT_LEAST_ONE_SHARED_COMMUNICATION_SCHEDULE_AVAILABLE(10016, SharedScheduledCommunicationTaskAvailable.class, "At least one shared communication schedule should be available on the device"),
        SLAVE_DEVICE_HAS_GATEWAY(10017, SlaveDeviceHasGateway.class, "A slave device must have a gateway device"),
        AT_LEAST_ONE_ZONE_LINKED(10018, ZonesLinkedToDevice.class, "Device {0} has no zone linked.");

        private final int number;
        private final String key;
        private final String defaultFormat;

        Message(int number, Class<? extends ExecutableMicroCheck> clazz, String defaultFormat) {
            this(number, clazz.getSimpleName(), defaultFormat);
        }

        Message(int number, String key, String defaultFormat) {
            this.number = number;
            this.key = MESSAGE_PREFIX + key;
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

        @Override
        public Level getLevel() {
            return Level.SEVERE;
        }

        @Override
        public String getModule() {
            return DeviceLifeCycleService.COMPONENT_NAME;
        }

        @Override
        public int getNumber() {
            return number;
        }
    }

    public enum Description implements TranslationKey {
        AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE(ActiveConnectionAvailable.class, "Check if at least one connection is available on the device with the status: 'Active'"),
        ALL_DATA_VALID(AllDataValid.class, "Check if all the collected data is valid"),
        ALL_DATA_VALIDATED(AllDataValidated.class, "Check if all the collected data is validated."),
        ALL_ISSUES_AND_ALARMS_ARE_CLOSED(AllIssuesAreClosed.class, "Check if all the issues and alarms on this device are closed."),
        ALL_DATA_COLLECTED(AllLoadProfileDataCollected.class, "Check if all load profile data of this device has been collected."),
        MANDATORY_COMMUNICATION_ATTRIBUTES_AVAILABLE("MANDATORY_COMMUNICATION_ATTRIBUTES_AVAILABLE", "Check if the mandatory communication attributes are available on the device: protocol dialect attributes, security setting attributes, connection attributes, general attributes."),
        DEFAULT_CONNECTION_AVAILABLE(DefaultConnectionTaskAvailable.class, "Check if a default connection is available on the device."),
        LINKED_WITH_USAGE_POINT(DeviceIsLinkedWithUsagePoint.class, "Check if this device is connected to a usage point."),
        METROLOGY_CONFIGURATION_IN_CORRECT_STATE_IF_ANY(MetrologyConfigurationInCorrectStateIfAny.class, "Check if the device is linked to a usage point that has an active metrology configuration."),
        NO_ACTIVE_SERVICE_CALLS(NoActiveServiceCalls.class, "Check that no service calls are active for the device."),
        NO_LINKED_MULTI_ELEMENT_SLAVES(NoLinkedOperationalMultiElementSlaves.class, "Check if there are no multi element slaves in an operational stage"),
        AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE(ScheduledCommunicationTaskAvailable.class, "Check if at least one communication task has been scheduled."),
        AT_LEAST_ONE_SHARED_COMMUNICATION_SCHEDULE_AVAILABLE(SharedScheduledCommunicationTaskAvailable.class, "Check if at least one shared communication schedule has been added to the device"),
        SLAVE_DEVICE_HAS_GATEWAY(SlaveDeviceHasGateway.class, "If this device is a slave, check if the device has been linked to a master device."),
        AT_LEAST_ONE_ZONE_LINKED(ZonesLinkedToDevice.class, "Check if the device has at least one zone linked");

        private final String key;
        private final String defaultFormat;

        Description(Class<? extends ExecutableMicroCheck> clazz, String defaultFormat) {
            this(clazz.getSimpleName(), defaultFormat);
        }

        Description(String key, String defaultFormat) {
            this.key = DESCRIPTION_PREFIX + key;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getDefaultFormat() {
            return defaultFormat;
        }
    }
}
