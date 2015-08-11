package com.energyict.mdc.device.data.rest;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public final class DevicePrivileges {
    private DevicePrivileges() {}

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
    public static final String DEVICES_PAGES_COMMUNICATION_PLANNING = "devices.pages.communication.planning";

    public static List<String> getPrivilegesFor(Device device, User user){
        return PrivilegesBasedOnDeviceState.get(device.getState()).getPrivileges(user);
    }

    private enum PrivilegesBasedOnDeviceState {
        DEFAULT(null),
        DECOMMISSIONED(Collections.singletonList(DefaultState.DECOMMISSIONED)){
            @Override
            List<String> getPrivileges(User user) {
                List<String> privileges = new ArrayList<>();
                if (user != null && user.hasPrivilege("MDC", Privileges.ADMINISTER_DECOMMISSIONED_DEVICE_DATA)) {
                    privileges.add(DEVICES_ACTIONS_DATA_EDIT);
                }
                return privileges;
            }
        },
        IN_STOCK(Collections.singletonList(DefaultState.IN_STOCK)){
            @Override
            List<String> getPrivileges(User user) {
                List<String> privileges = new ArrayList<>(super.getPrivileges(user));
                privileges.remove(DEVICES_WIDGET_ISSUES);
                privileges.remove(DEVICES_WIDGET_VALIDATION);
                privileges.remove(DEVICES_ACTIONS_VALIDATION);
                privileges.remove(DEVICES_ACTIONS_ESTIMATION);
                return privileges;
            }
        },
        ;

        private List<DefaultState> matchedStates;

        PrivilegesBasedOnDeviceState(List<DefaultState> matchedStates) {
            this.matchedStates = matchedStates;
        }

        List<String> getPrivileges(User user){
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
                    DevicePrivileges.DEVICES_ACTIONS_FIRMWARE_MANAGEMENT,
                    DevicePrivileges.DEVICES_PAGES_COMMUNICATION_PLANNING
            );
        }

        static PrivilegesBasedOnDeviceState get(State deviceState){
            Optional<DefaultState> defaultStateRef = DefaultState.from(deviceState);
            if (defaultStateRef.isPresent()) {
                return EnumSet.complementOf(EnumSet.of(DEFAULT))
                        .stream()
                        .filter(privilegeState -> privilegeState.matchedStates.contains(defaultStateRef.get()))
                        .findFirst()
                        .orElse(DEFAULT);
            }
            return DEFAULT;
        }
    }
}
