/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest;

import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.TimeOfUseOptions;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.protocol.api.calendars.ProtocolSupportedCalendarOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class DevicePrivileges {
    private DevicePrivileges() {
    }

    public static final String DEVICES_WIDGET_ISSUES = "devices.widget.issues";
    public static final String DEVICES_WIDGET_VALIDATION = "devices.widget.validation";
    public static final String DEVICES_WIDGET_COMMUNICATION_TOPOLOGY = "devices.widget.communication.topology";
    public static final String DEVICES_WIDGET_CONNECTION = "devices.widget.connection";
    public static final String DEVICES_WIDGET_COMMUNICATION_TASKS = "devices.widget.communication.tasks";
    public static final String DEVICES_ACTIONS_VALIDATION = "devices.actions.validation";
    public static final String DEVICES_ACTIONS_ESTIMATION = "devices.actions.estimation";
    public static final String DEVICES_ACTIONS_VALIDATION_RULE_SETS = "devices.actions.validation.rulesets";
    public static final String DEVICES_ACTIONS_ESTIMATION_RULE_SETS = "devices.actions.estimation.rulesets";
    public static final String DEVICES_ACTIONS_COMMUNICATION_PLANNING = "devices.actions.communication.planning";
    public static final String DEVICES_ACTIONS_COMMUNICATION_TOPOLOGY = "devices.actions.communication.topology";
    public static final String DEVICES_ACTIONS_DEVICE_COMMANDS = "devices.actions.device.commands";
    public static final String DEVICES_ACTIONS_SECURITY_SETTINGS = "devices.actions.security.settings";
    public static final String DEVICES_ACTIONS_PROTOCOL_DIALECTS = "devices.actions.protocol.dialects";
    public static final String DEVICES_ACTIONS_GENERAL_ATTRIBUTES = "devices.actions.general.attributes";
    public static final String DEVICES_ACTIONS_COMMUNICATION_TASKS = "devices.actions.communication.tasks";
    public static final String DEVICES_ACTIONS_CONNECTION_METHODS = "devices.actions.connection.methods";
    public static final String DEVICES_ACTIONS_FIRMWARE_MANAGEMENT = "devices.actions.firmware.management";
    public static final String DEVICES_ACTIONS_DATA_EDIT = "devices.actions.data.edit";
    public static final String DEVICES_ACTIONS_CHANGE_DEVICE_CONFIGURATION = "devices.actions.change.device.configuration";
    public static final String DEVICES_PAGES_COMMUNICATION_PLANNING = "devices.pages.communication.planning";
    public static final String DEVICES_TIME_OF_USE_ALLOWED = "devices.pages.timeofuseallowed";

    private static Map<ProtocolSupportedCalendarOptions, String> option2Privilege = createAllPrivilegesMap();

    public static List<String> getPrivilegesFor(Device device, User user) {
        List<String> privileges = PrivilegesBasedOnDeviceStage.get(device.getStage()).getPrivileges(user);
        return privileges;
    }

    public static List<String> getTimeOfUsePrivilegesFor(Device device, DeviceConfigurationService deviceConfigurationService) {
        Set<ProtocolSupportedCalendarOptions> supportedCalendarOptions = deviceConfigurationService.getSupportedTimeOfUseOptionsFor(device.getDeviceConfiguration().getDeviceType(), true);
        Optional<TimeOfUseOptions> timeOfUseOptions = deviceConfigurationService.findTimeOfUseOptions(device.getDeviceConfiguration().getDeviceType());
        List<String> privileges = new ArrayList<>();
        Set<ProtocolSupportedCalendarOptions> allowedOptions = timeOfUseOptions.map(TimeOfUseOptions::getOptions).orElse(Collections
                .emptySet());
        if (allowedOptions.size() > 0) {
            if (supportedCalendarOptions.contains(ProtocolSupportedCalendarOptions.VERIFY_ACTIVE_CALENDAR)) {
                allowedOptions.add(ProtocolSupportedCalendarOptions.VERIFY_ACTIVE_CALENDAR);
            }

            if (allowedOptions.contains(ProtocolSupportedCalendarOptions.CLEAR_AND_DISABLE_PASSIVE_TARIFF) || allowedOptions
                    .contains(ProtocolSupportedCalendarOptions.ACTIVATE_PASSIVE_CALENDAR)) {
                privileges.add("devices.timeofuse.supportspassive");

            }
            if (containsSendOption(allowedOptions)) {
                privileges.add("devices.timeofuse.supportssend");
            }
            privileges.addAll(getTimeOfUsePrivileges(allowedOptions));
            return privileges;
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    private static boolean containsSendOption(Set<ProtocolSupportedCalendarOptions> allowedOptions) {
        return allowedOptions.stream()
                .filter(createSendPrivilegesMap().keySet()::contains)
                .findAny()
                .isPresent();
    }

    private static List<String> getTimeOfUsePrivileges(Set<ProtocolSupportedCalendarOptions> allowedOptions) {
        List<String> list = allowedOptions.stream()
                .map(option -> option2Privilege.get(option))
                .collect(Collectors.toList());
        list.add(DEVICES_TIME_OF_USE_ALLOWED);
        return list;
    }

    private static Map<ProtocolSupportedCalendarOptions, String> createAllPrivilegesMap() {
        Map<ProtocolSupportedCalendarOptions, String> map = new EnumMap<>(ProtocolSupportedCalendarOptions.class);
        map.put(ProtocolSupportedCalendarOptions.VERIFY_ACTIVE_CALENDAR, "devices.actions.timeofuse.verify");
        map.put(ProtocolSupportedCalendarOptions.CLEAR_AND_DISABLE_PASSIVE_TARIFF, "devices.actions.timeofuse.clearanddisable");
        map.put(ProtocolSupportedCalendarOptions.ACTIVATE_PASSIVE_CALENDAR, "devices.actions.timeofuse.activatepassive");
        map.putAll(createSendPrivilegesMap());
        return map;
    }

    private static Map<ProtocolSupportedCalendarOptions, String> createSendPrivilegesMap() {
        Map<ProtocolSupportedCalendarOptions, String> map = new EnumMap<>(ProtocolSupportedCalendarOptions.class);
        map.put(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR, "devices.actions.timeofuse.send");
        map.put(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATE_AND_TYPE, "devices.actions.timeofuse.sendDateType");
        map.put(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATE, "devices.actions.timeofuse.sendDate");
        map.put(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATE_AND_CONTRACT, "devices.actions.timeofuse.sendDateContract");
        map.put(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATETIME, "devices.actions.timeofuse.sendDateTime");
        map.put(ProtocolSupportedCalendarOptions.SEND_SPECIAL_DAYS_CALENDAR, "devices.actions.timeofuse.sendSpecial");
        map.put(ProtocolSupportedCalendarOptions.SEND_SPECIAL_DAYS_CALENDAR_WITH_TYPE, "devices.actions.timeofuse.sendSpecialType");
        map.put(ProtocolSupportedCalendarOptions.SEND_SPECIAL_DAYS_CALENDAR_WITH_CONTRACT_AND_DATE, "devices.actions.timeofuse.sendSpecialContractDate");
        return map;
    }


    private enum PrivilegesBasedOnDeviceStage {
        DEFAULT(null),
        POST_OPERATIONAL(Collections.singletonList(EndDeviceStage.POST_OPERATIONAL)) {
            @Override
            List<String> getPrivileges(User user) {
                List<String> privileges = new ArrayList<>();
                if (user != null && user.hasPrivilege("MDC", Privileges.Constants.ADMINISTER_DECOMMISSIONED_DEVICE_DATA)) {
                    privileges.add(DEVICES_ACTIONS_DATA_EDIT);
                }
                return privileges;
            }
        },
        PRE_OPERATIONAL(Collections.singletonList(EndDeviceStage.PRE_OPERATIONAL)) {
            @Override
            List<String> getPrivileges(User user) {
                List<String> privileges = new ArrayList<>(super.getPrivileges(user));
                privileges.remove(DEVICES_WIDGET_ISSUES);
                privileges.remove(DEVICES_WIDGET_VALIDATION);
                privileges.remove(DEVICES_ACTIONS_VALIDATION);
                privileges.remove(DEVICES_ACTIONS_ESTIMATION);
                return privileges;
            }
        },;

        private List<EndDeviceStage> matchedStages;

        PrivilegesBasedOnDeviceStage(List<EndDeviceStage> matchedStages) {
            this.matchedStages = matchedStages;
        }

        List<String> getPrivileges(User user) {
            return Arrays.asList(
                    DevicePrivileges.DEVICES_WIDGET_ISSUES,
                    DevicePrivileges.DEVICES_WIDGET_VALIDATION,
                    DevicePrivileges.DEVICES_WIDGET_COMMUNICATION_TOPOLOGY,
                    DevicePrivileges.DEVICES_WIDGET_CONNECTION,
                    DevicePrivileges.DEVICES_WIDGET_COMMUNICATION_TASKS,
                    DevicePrivileges.DEVICES_ACTIONS_VALIDATION,
                    DevicePrivileges.DEVICES_ACTIONS_ESTIMATION,
                    DevicePrivileges.DEVICES_ACTIONS_VALIDATION_RULE_SETS,
                    DevicePrivileges.DEVICES_ACTIONS_ESTIMATION_RULE_SETS,
                    DevicePrivileges.DEVICES_ACTIONS_COMMUNICATION_PLANNING,
                    DevicePrivileges.DEVICES_ACTIONS_COMMUNICATION_TOPOLOGY,
                    DevicePrivileges.DEVICES_ACTIONS_DEVICE_COMMANDS,
                    DevicePrivileges.DEVICES_ACTIONS_SECURITY_SETTINGS,
                    DevicePrivileges.DEVICES_ACTIONS_PROTOCOL_DIALECTS,
                    DevicePrivileges.DEVICES_ACTIONS_GENERAL_ATTRIBUTES,
                    DevicePrivileges.DEVICES_ACTIONS_COMMUNICATION_TASKS,
                    DevicePrivileges.DEVICES_ACTIONS_CONNECTION_METHODS,
                    DevicePrivileges.DEVICES_ACTIONS_DATA_EDIT,
                    DevicePrivileges.DEVICES_ACTIONS_CHANGE_DEVICE_CONFIGURATION,
                    DevicePrivileges.DEVICES_ACTIONS_FIRMWARE_MANAGEMENT,
                    DevicePrivileges.DEVICES_PAGES_COMMUNICATION_PLANNING
            );
        }

        static PrivilegesBasedOnDeviceStage get(Stage stage) {
            EndDeviceStage endDeviceStage = EndDeviceStage.valueOf(stage.getName());
            return EnumSet.complementOf(EnumSet.of(DEFAULT))
                    .stream()
                    .filter(privilegeState -> privilegeState.matchedStages.contains(endDeviceStage))
                    .findFirst()
                    .orElse(DEFAULT);
        }
    }
}
