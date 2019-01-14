/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    NO_SUCH_DEVICE(14, "noSuchDevice", "No device with name {0}"),
    DEVICE_DOES_NOT_MATCH_CONFIG(15, "deviceDoesNotMatchConfig", "Device does not match device configuration"),
    NO_SUCH_PARTIAL_CONNECTION_TASK(16, "NoSuchPartialConnectionTask", "No such connection method on device config"),
    NO_SUCH_CONNECTION_METHOD(17, "NoSuchConnectionTask", "Device {0} has no connection method {1}"),
    NO_SUCH_REGISTER(18, "NoSuchRegister", "No register with id {0}"),
    DEVICE_VALIDATION_BULK_MSG(20, "DeviceValidationBulkMessage", "This bulk operation for {0} schedule on {1} device is invalid"),
    NO_SUCH_READING(21, "NoSuchReading", "Register {0} has no reading with id {1}"),
    INVALID_DATE(22, "InvalidDate", "Date should be less or equal to {0}"),
    NO_SUCH_LOAD_PROFILE_ON_DEVICE(23, "NoSuchLoadProfile", "Device {0} has no load profile {1}"),
    NO_SUCH_CHANNEL_ON_LOAD_PROFILE(30, "NoSuchChannel", "Load profile {0} has no channel {1}"),
    NO_CHANNELS_ON_REGISTER(72, "NoChannelsOnRegister", "Register {0} has no channels"),
    NO_SUCH_READING_ON_REGISTER(73, "NoSuchReadingOnRegister", "Register {0} has no reading with timestamp {1}"),
    NO_SUCH_LOG_BOOK_ON_DEVICE(24, "NoSuchLogBook", "Device {0} has no log book {1}"),
    CONNECTION_TYPE_STRATEGY_NOT_APPLICABLE(25, "connectionTypeStrategy.notApplicable", "Not applicable"),
    UPDATE_URGENCY_NOT_ALLOWED(26, "urgencyUpdateNotAllowed", "Urgency update not allowed"),
    UPDATE_DIALECT_PROPERTIES_NOT_ALLOWED(27, "updateDialectPropertiesNotAllowed", "Protocol dialect update not allowed"),
    UPDATE_CONNECTION_METHOD_NOT_ALLOWED(28, "updateConnectionMethodNotAllowed", "Connection method update not allowed"),
    RUN_COMTASK__NOT_ALLOWED(29, "runComTaskNotAllowed", "Running of this communication task is not allowed"),
    NULL_DATE(61, "NullDate", "Date must be filled in"),
    DEACTIVATE_VALIDATION_RULE_SET_NOT_POSSIBLE(62, "DeactivateValidationRuleSetNotPossible", "Deactivate of validation rule set {0} is currently not possible."),
    NO_SUCH_COM_SESSION_ON_CONNECTION_METHOD(88, "noSuchComSession", "No such communication session exists for this connection method"),
    NO_SUCH_COM_TASK(91, "NoSucComTaskOnDevice", "No such communication task exists for device ''{0}''"),
    COM_TASK_IS_NOT_ENABLED_FOR_THIS_DEVICE(92, "NoEnablementForDevice", "Communication task ''{0}'' is not enabled for device ''{1}''"),
    NO_SUCH_COM_TASK_EXEC_SESSION(93, "NoSuchComTaskExecSession", "The communication task logging could not be found"),
    INCOMPLETE(96, "Incomplete", "Incomplete"),
    NO_SUCH_SECURITY_PROPERTY_SET_ON_DEVICE(97, "NoSuchSecurityPropertySetOnDevice", "No security settings with id {0} exist for device ''{1}''"),
    NO_SUCH_SECURITY_PROPERTY_SET(98, "NoSuchSecurityPropertySet", "No security settings with id {0} exist"),
    NO_SUCH_USER(211, "NoSuchUser", "No such user"),
    NO_SUCH_MESSAGE_SPEC(212, "NoSuchMessageSpec", "No such device message specification"),
    NO_SUCH_MESSAGE(213, "NoSuchMessage", "No such device message exists on the device"),
    UPDATE_SECURITY_PROPERTY_SET_NOT_ALLOWED(217, "UpdateSecurityPropertySetNotAllowed", "Update security property set not allowed"),
    NO_SUCH_LABEL_CATEGORY(219, "NoSuchLabelCategory", "No such label category"),
    NO_SUCH_CHANNEL_ON_DEVICE(221, "NoSuchChannelOnDevice", "Device {0} has no channel {1}"),
    NO_SUCH_REGISTER_ON_DEVICE(222, "NoSuchRegisterOnDevice", "Device {0} has no register {1}"),
    RUN_CONNECTIONTASK_IMPOSSIBLE(224, "runConTaskImpossible", "Running of this connection task is impossible"),
    NO_SUCH_COMMUNICATION(225, "NoSuchComTaskExecution", "No such communication with id ''{0}'' on device ''{1}''"),
    NO_SUCH_KPI(226, "NoSuchKpi", "No data collection with id ''{0}'' could be found"),
    NO_SUCH_DEVICE_GROUP(227, "NoSuchDeviceGroup", "No end device group with id ''{0}''"),
    NO_UPDATE_ALLOWED(228, "NoUpdateAllowed", "You are not allowed to change this field"),
    IMPOSSIBLE_TO_SET_MASTER_DEVICE(229, "ImpossibleToSetMasterDevice", "Device {0} is directly addressable. It is not possible to set master device"),
    NO_SUCH_COM_TASK_EXEC(231, "NoSuchComTaskExec", "No such communication task execution exists"),
    CAN_NOT_PERFORM_ACTION_ON_SYSTEM_COMTASK(232, "CanNotPerformActionOnSystemComTask", "You can not perform an action on a system communication task"),
    ESTIMATOR_REQUIRED(233, "EstimatorRequired", "Please select an estimator"),
    ESTIMATOR_NOT_FOUND(234, "EstimatorNotValid", "Provided estimator is not valid"),
    METER_ACTIVATION_NOT_FOUND(235, "MeterActivationNotFound", "No meter activation is found"),
    NO_SUCH_DEVICE_LIFE_CYCLE_ACTION(236, "NoSuchDeviceLifeCycleAction", "No device life cycle action with id = {0}"),
    THIS_FIELD_IS_REQUIRED(237, "ThisFieldIsRequired", "This field is required."),
    CIM_DATE_SHOULD_BE_AFTER_X(238, "CIMDateShouldBeAfterX", "This date should be later than ''{0}''"),
    NO_APPSERVER(239, "NoAppServer", "There is no active application server that can handle this request"),
    NO_SUCH_MESSAGE_QUEUE(240, "NoSuchMessageQueue", "Unable to queue command: no message queue was found"),
    BAD_ACTION(241, "BadAction", "Expected action to be either ''add'' or ''remove''."),
    NO_SUCH_COM_SCHEDULE(242, "NoSuchSchedule", "No communication schedule with id {0}"),
    CANT_ADD_READINGS_FOR_STATE(243, "cantAddReadingForState", "The state of the device at {0,date,long} {0,time,long} does not allow adding readings."),
    INVALID_ESTIMATOR_PROPERTY_VALUE(244, "invalidEstimatorPropertyValue", "Invalid property value"),
    CONCURRENT_RUN_TITLE(245, "ConcurrentRunTitle", "Failed to run ''{0}''"),
    CONCURRENT_RUN_BODY(246, "ConcurrentRunMessage", "{0} has changed since the page was last updated."),
    NO_SUCH_CUSTOMPROPERTYSET(247, "noSuchCPS", "No custom property set with ID {0}"),
    DEVICE_SEARCH_DOMAIN_NOT_REGISTERED(248, "DeviceSearchDomainNotRegistered", "Device search domain is not registered"),
    AT_LEAST_ONE_SEARCH_CRITERIA(249, "AtLeastOneCriteria", "At least one search criterion has to be provided"),
    NO_SUCH_REQUIRED_PROPERTY(250, "NoSuchProperty", "No custom property required value"),
    SEARCHABLE_PROPERTY_INVALID_VALUE(251, "SearchablePropertyInvalidValue", "Invalid value"),
    OVERLAP_CUSTOMPROPERTYSET(252, "overlapCPSvalue", "Custom property set conflicting with another sets in timeline"),
    GAP_CUSTOMPROPRTTYSET(253, "gapCPSvalue", "Custom property set gap with another sets in timeline"),
    CUSTOMPROPERTYSET_TIMESLICED_INSERT(254, "edit.historical.values.insert", "Insert"),
    INTERVAL_INVALID(255, "wrongInterval", "Invalid interval [{0},{1})"),
    INTERVAL_EMPTY(256, "emptyInterval", "Empty intervals are not allowed"),
    INTERVAL_START_AFTER_END(257, "intervalStattAfterEnd", "Start time after end time"),
    INTERVAL_END_BEFORE_START(258, "intervalEndBeforeStart", "End time before start time"),
    NO_SUCH_CUSTOMPROPERTYSET_FOR_REGISTER(259, "noSuchCPSforRegister", "No custom property set with ID {0} for Register with ID {1}"),
    NO_SUCH_CUSTOMPROPERTYSET_FOR_CHANNEL(260, "noSuchCPSforChannel", "No custom property set with ID {0} for Channel with ID {1}"),
    FLAG_DEVICE_CONCURRENT_TITLE(261, "FlagDeviceConcurrentTitle", "Failed to flag ''{0}''"),
    REMOVE_FLAG_DEVICE_CONCURRENT_TITLE(262, "RemoveFlagDeviceConcurrentTitle", "Failed to remove ''{0}'' from the list of flagged devices"),
    FLAG_DEVICE_CONCURRENT_BODY(263, "FlagDeviceConcurrentMessage", "{0} has changed since the page was last updated."),
    VERSION_MISSING(264, "VersionInfoMissing", "Versioning information is missing in the request"),
    CONFLICT_ON_DEVICE(265, "ConflictOnDevice", "The device you attempted to edit was changed by someone else."),
    NO_SUCH_DEVICE_CONFIG(266, "NoSuchDeviceConfig", "Device type does not contain a device configuration with that id"),
    NO_SUCH_DEVICE_TYPE(267, "NoSuchDeviceType", "Device type does not exist with that id"),
    NO_SUCH_DEVICE_ID(268, "NoSuchDeviceId", "No device with id {0}"),
    CHANGE_DEVICE_CONFIG_CONFLICT(269, "ChangeDeviceConfigConflict", "Found change device configuration conflict with id {0}"),
    NO_SUCH_LOAD_PROFILE_TYPE(270, "NoSuchLoadProfileType", "Load profile type does not exist with that id"),
    NO_SUCH_REGISTER_TYPE(271, "NoSuchRegisterType", "Register type does not exist with that id"),
    VALUE_MAY_NOT_EXCEED_OVERFLOW_VALUE(272, "ValueMayNotExceedOverflowValue", "The value {0} may not exceed the configured overflow value {1}"),
    INVALID_TRACKING_ID(273, "InvalidTrackingId", "The tracking id is invalid for this tracking category"),
    BAD_REQUEST(274, "badRequest", "Bad request"),
    INVALID_COORDINATES(275, "invalidCoordinates", "All coordinates fields must contain valid values"),
    VERIFY_CALENDAR_TASK_IS_NOT_ACTIVE(276, "VerifyCalendarTaskNotActive", "The ''Check time of use calendar'' action can''t be executed because there is no communication task with the ''Status information - Read'' action on the device configuration of this device."),
    UNABLE_TO_FIND_CALENDAR(277, "CannotFindCalendar", "Unable to find the given calendar in the system."),
    NO_ALLOWED_CALENDAR_DEVICE_MESSAGE(278, "NoAllowedCalendarMessage", "Unable to find an allowed calendar command with the given information"),
    NO_ACTIVE_CALENDAR(279, "NoActiveCalendar", "This device doesn't have an active calendar"),
    ACTIVE_CALENDAR_IS_GHOST(280, "ActiveCalendarIsGhost", "The active calendar on this device is a ghost calendar and can not be previewed"),
    CALENDAR_NOT_ACTIVE_ON_DEVICE(281, "CalendarNotActiveOnDevice", "The requested calendar is not an active calendar on this device"),
    COMMAND_NOT_ALLOWED_OR_SUPPORTED(282, "CommandNotAllowedOrSupported", "The command you want to add is not supported or allowed on the device type"),
    UNIQUE_NAME(283, "unique.name", "The name of the device must be unique"), // we only use this validation/error when we use the wizard to create slave devices
    INVALID_MULTIPLIER(284, "invalidMultiplier", "Should be larger than zero"),
    CANNOT_ADDEDITREMOVE_REGISTER_VALUE_WHEN_LINKED_TO_SLAVE(285, "cannotAddRegisterValueWhenLinkedToSlave", "You cannot add, edit or remove a register value at a timestamp at which you have a linked slave"),
    CANNOT_ADDEDITREMOVE_CHANNEL_VALUE_WHEN_LINKED_TO_SLAVE(286, "cannotAddChannelValueWhenLinkedToSlave", "You cannot add, edit or remove a channel value at a timestamp at which you have a linked slave"),
    PROCESS_STATUS_PENDING(287, "ProcessStatusPending", "Pending"),
    PROCESS_STATUS_ACTIVE(288, "ProcessStatusActive", "Active"),
    PROCESS_STATUS_COMPLETED(289, "ProcessStatusCompleted", "Completed"),
    PROCESS_STATUS_ABORTED(290, "ProcessStatusAborted", "Aborted"),
    PROCESS_STATUS_SUSPENDED(291, "ProcessStatusSuspended", "Suspended"),
    OVERLAPPING_COMTASKS_IN_COMSCHEDULES(292, "OverlappingComTasks", "There are overlapping communication tasks in the schedules"),
    NO_SUCH_VALIDATION_RULE(293, "noSuchValidationRule", "No validation rule with id {0}"),
    NO_SUCH_READINGTYPE(294, "noReadingType", "No reading type with MRID {0}"),
    READINGTYPES_DONT_MATCH(295, "ReadingTypesDontMatch", "Reading types don''t match"),
    READINGTYPE_NOT_FOUND_ON_DEVICE(296, "ReadingTypeNotFoundOnDevice", "Reading type not found on device"),
    NO_SUCH_ESTIMATION_RULE(298, "noSuchEstimationRule", "No estimation rule with id {0}"),
    NO_SUCH_READINGTYPE_ON_CHANNEL(299, "noSuchReadingTypeOnChannel", "Device''s channel doesn''t provide reading type {0}"),
    VALIDATION_RULE_IS_NOT_APPLICABLE_TO_READINGTYPE(300, "ValidationRuleIsNotApplicableToReadingType", "Validation rule with id {0} is not applicable to reading type {1}"),
    ESTIMATION_RULE_IS_NOT_APPLICABLE_TO_READINGTYPE(301, "EstimationRuleIsNotApplicableToReadingType", "Estimation rule with id {0} is not applicable to reading type {1}"),
    NO_SUCH_PROTOCOL_PROPERTIES(302, "NoSuchProtocolDialectProperties", "No protocol dialect with name ''{0}''"),
    UPDATE_OF_DEVICE_FAILED(303, "UpdateOfDeviceFailed", "Update of device failed"),
    NO_CALCULATED_READINGTYPE_ON_CHANNEL(304, "noCalculatedReadingTypeOnChannel", "Device''s channel with id {0} doesn''t provide calculated reading type"),
    NO_SUCH_KEY_ACCESSOR_TYPE(305, "NoSuchKeyAccessorType", "The device type does not have a security accessor with that id"),
    NO_SUCH_KEY_ACCESSOR(309, "NoSuchKeyAccessor", "No such security accessor"),
    COMMAND_SHOULD_HAVE_A_CONTRACT_ATTRIBUTE(310, "command.should.have.a.contract.attribute", "The device command ''{0}'' should have a 'Contract' attribute"),
    COMMAND_SHOULD_HAVE_A_TYPE_ATTRIBUTE(311, "command.should.have.a.type.attribute", "The device command ''{0}'' should have a 'Type' attribute"),
    COMMAND_SHOULD_HAVE_AN_ACTIVATION_DATE_ATTRIBUTE(312, "command.should.have.an.activation.date.attribute", "The device command ''{0}'' should have an 'Activation date' attribute"),
    NO_SUCH_DEVICE_MESSAGE_CATEGORY(313, "NoSuchDeviceMessageCategory", "No device message category with id {0}"),
    NO_SUCH_DEVICE_MESSAGE_STATUS(314, "NoSuchDeviceMessageStatus", "No device message status with name {0}"),
    NO_SUCH_DEVICE_COMMAND(315, "NoSuchDeviceMessageId", "No device message with name {0}"),
    NO_MESSAGE_WITH_ID(316, "NoMessageWithId", "No device message with id {0} exists"),
    NO_SUCH_CONNECTION_FUNCTION(317, "NoSuchConnectionFunction", "The connection function could not be found"),
    MASTER_DEVICE_CANNOT_ACT_AS_GATEWAY(318, "MasterDeviceCannotActAsGateway", "Device ''{0}'' cannot be set as master device because its device configuration prohibits usage as gateway"),
    NO_SUCH_PROCESS_DEFINITION(319, "NoSuchProcessDefinition", "No process definition found."),
    NO_SUCH_CRL_REQUEST_TASK_PROPERTIES(320, "NoCrlRequestTaskProps", "No CRL request task properties exist"),
    NO_SUCH_CRL_REQUEST_TASK(321, "NoCrlRequestTask", "No CRL request task exist"),
    CRL_REQUEST_TASK_CA_NAME_UNIQUE(322, "CrlRequestTaskCaNameUnique", "The CA name must be unique."),
    NOT_ALL_PROPS_ARE_DEFINDED(323, "notAllPropsAreDefined", "One of the following property is not set, Communication port pool, hostname or port number!"),
    CANT_RERUN_NO_PROCESSES(324, "cantRerunNoProcesses", "Can''t run bulk action ''Retry process'', no process is found to retry."),
    CANT_RERUN_SEVERAL_PROCESSES(325, "cantRerunSeveralProcesses", "Can''t run bulk action ''Retry process'' for a set of different processes."),
    PROCESS_IS_NOT_ACTIVE(326, "processIsNotActive", "Process ''{0}'' with version ''{1}'' isn''t activated."),
    PROCESS_OBJECT_TYPE_NOT_FOUND(327, "processObjectTypeNotFound", "Couldn''t find target object type for process ''{0}'' with version ''{1}''."),
    OBJECTS_FILTERED_TYPE_NOT_COMPATIBLE(328, "objectsFilteredTypeNotCompatible", "The process can''t run on the following objects of incompatible type(s): {0}."),
    OBJECTS_FILTERED_NOT_CONSISTENT(329, "objectsFilteredNotConsistent", "The process can''t run on the following objects as they don''t match the process requirements: {0}."),
    OBJECTS_FILTERED_NOT_FOUND(330, "objectsFilteredNotFound", "The following objects aren''t found: {0}."),
    OBJECTS_FILTERED_ALREADY_RUNNING(331, "objectsFilteredAlreadyRunning", "The process is already running on the following objects: {0}."),
    OBJECTS_FILTERED_DUPLICATED(332, "objectsFilteredDuplicated", "The following objects are duplicated in the selected process history list: {0}; the process will be started once on each of them."),
    ;

    private final int number;
    private final String key;
    private final String format;

    MessageSeeds(int number, String key, String format) {
        this.number = number;
        this.key = key;
        this.format = format;
    }

    @Override
    public String getModule() {
        return DeviceApplication.COMPONENT_NAME;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return format;
    }

    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }
}
