/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.metering.ami.EndDeviceCommand;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.ami.commands.ArmRemoteSwitchCommand;
import com.energyict.mdc.device.data.impl.ami.commands.CloseRemoteSwitchCommand;
import com.energyict.mdc.device.data.impl.ami.commands.LoadControlInitiateCommand;
import com.energyict.mdc.device.data.impl.ami.commands.LoadControlTerminateCommand;
import com.energyict.mdc.device.data.impl.ami.commands.OpenRemoteSwitchCommand;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public enum EndDeviceControlTypeMapping {

    OTHER("0.0.0.0"),
    //Demand Controls
    RESET("0.8.0.214"),
    //Load Control Controls
    LOAD_CONTROL_INITIATE("0.15.0.54", Collections.singletonList(DeviceMessageId.LOAD_BALANCING_SET_LOAD_LIMIT_THRESHOLD), Collections.singletonList(DeviceMessageId.LOAD_BALANCING_CONFIGURE_LOAD_LIMIT_THRESHOLD_AND_DURATION)) {
        @Override
        public Optional<EndDeviceCommand> getNewEndDeviceCommand(EndDevice endDevice, EndDeviceControlType endDeviceControlType, List<DeviceMessageId> possibleDeviceMessageIds, DeviceService deviceService, DeviceMessageSpecificationService deviceMessageSpecificationService, Thesaurus thesaurus) {
            return Optional.of(new LoadControlInitiateCommand(endDevice, endDeviceControlType, possibleDeviceMessageIds, deviceService, deviceMessageSpecificationService, thesaurus));
        }
    },
    LOAD_CONTROL_TERMINATE("0.15.0.55", Collections.singletonList(DeviceMessageId.LOAD_BALANCING_DISABLE_LOAD_LIMITING)) {
        @Override
        public Optional<EndDeviceCommand> getNewEndDeviceCommand(EndDevice endDevice, EndDeviceControlType endDeviceControlType, List<DeviceMessageId> possibleDeviceMessageIds, DeviceService deviceService, DeviceMessageSpecificationService deviceMessageSpecificationService, Thesaurus thesaurus) {
            return Optional.of(new LoadControlTerminateCommand(endDevice, endDeviceControlType, possibleDeviceMessageIds, deviceService, deviceMessageSpecificationService, thesaurus));
        }
    },

    //PAN / HAN Controls
    PAN_PAIRING_WINDOW_OPEN("0.10.73.298"),
    PAN_PAIRING_WINDOW_CLOSE("0.10.73.299"),
    PAN_TXT_MSG_DISPLAY("0.13.112.77"),
    PAN_TXT_MSG_CANCEL("0.13.112.8"),
    PAN_PRICE_SGN_CREATE("0.20.9.82"),
    PAN_LOAD_CTRL_ALL_EVENT_CANCEL("0.15.148.8"),
    PAN_LOAD_CTRL_EVENT_CANCEL("0.15.43.8"),
    PAN_LOAD_CTRL_EVENT_SCHEDULE("0.15.43.300"),
    PAN_NETWORK_ENABLE("0.23.0.26"),
    PAN_NETWORK_DISABLE("0.23.0.22"),

    //RCDSwitch controls
    DISABLE_EMERGENCY_SUP_CAPACITY_LIM("0.31.138.22"),
    ENABLE_EMERGENCY_SUP_CAPACITY_LIM("0.31.138.26"),
    ARM_REMOTE_SWITCH_FOR_CLOSURE("0.31.0.5", Arrays.asList(DeviceMessageId.CONTACTOR_OPEN, DeviceMessageId.CONTACTOR_ARM), Arrays.asList(DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE, DeviceMessageId.CONTACTOR_ARM_WITH_ACTIVATION_DATE)) {
        @Override
        public Optional<EndDeviceCommand> getNewEndDeviceCommand(EndDevice endDevice, EndDeviceControlType endDeviceControlType, List<DeviceMessageId> possibleDeviceMessageIds, DeviceService deviceService, DeviceMessageSpecificationService deviceMessageSpecificationService, Thesaurus thesaurus) {
            return Optional.of(new ArmRemoteSwitchCommand(endDevice, endDeviceControlType, possibleDeviceMessageIds, deviceService, deviceMessageSpecificationService, thesaurus));
        }
    },
    ARM_REMOTE_SWITCH_FOR_OPEN("0.31.0.6", Arrays.asList(DeviceMessageId.CONTACTOR_OPEN, DeviceMessageId.CONTACTOR_ARM), Arrays.asList(DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE, DeviceMessageId.CONTACTOR_ARM_WITH_ACTIVATION_DATE)) {
        @Override
        public Optional<EndDeviceCommand> getNewEndDeviceCommand(EndDevice endDevice, EndDeviceControlType endDeviceControlType, List<DeviceMessageId> possibleDeviceMessageIds, DeviceService deviceService, DeviceMessageSpecificationService deviceMessageSpecificationService, Thesaurus thesaurus) {
            return Optional.of(new ArmRemoteSwitchCommand(endDevice, endDeviceControlType, possibleDeviceMessageIds, deviceService, deviceMessageSpecificationService, thesaurus));
        }
    },
    CLOSE_REMOTE_SWITCH("0.31.0.18", Collections.singletonList(DeviceMessageId.CONTACTOR_CLOSE), Collections.singletonList(DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE)) {
        @Override
        public Optional<EndDeviceCommand> getNewEndDeviceCommand(EndDevice endDevice, EndDeviceControlType endDeviceControlType, List<DeviceMessageId> possibleDeviceMessageIds, DeviceService deviceService, DeviceMessageSpecificationService deviceMessageSpecificationService, Thesaurus thesaurus) {
            return Optional.of(new CloseRemoteSwitchCommand(endDevice, endDeviceControlType, possibleDeviceMessageIds, deviceService, deviceMessageSpecificationService, thesaurus));
        }
    },
    DISABLE_SWITCH("0.31.0.22"),
    OPEN_REMOTE_SWITCH("0.31.0.23", Collections.singletonList(DeviceMessageId.CONTACTOR_OPEN), Collections.singletonList(DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE)) {
        @Override
        public Optional<EndDeviceCommand> getNewEndDeviceCommand(EndDevice endDevice, EndDeviceControlType endDeviceControlType, List<DeviceMessageId> possibleDeviceMessageIds, DeviceService deviceService, DeviceMessageSpecificationService deviceMessageSpecificationService, Thesaurus thesaurus) {
            return Optional.of(new OpenRemoteSwitchCommand(endDevice, endDeviceControlType, possibleDeviceMessageIds, deviceService, deviceMessageSpecificationService, thesaurus));
        }
    },
    ENABLE_SWITCH("0.31.0.26"),
    DISABLE_SUP_CAPACITY_LIM("0.31.139.22"),
    ENABLE_SUP_CAPACITY_LIM("0.31.139.26");

    private final String endDeviceControlTypeMRID;
    private final List<List<DeviceMessageId>> possibleDeviceMessageIdGroups;

    EndDeviceControlTypeMapping(String endDeviceControlTypeMRID, List<DeviceMessageId>... possibleDeviceMessageIdGroups) {
        this.endDeviceControlTypeMRID = endDeviceControlTypeMRID;
        this.possibleDeviceMessageIdGroups = Arrays.asList(possibleDeviceMessageIdGroups);
    }

    public String getEndDeviceControlTypeMRID() {
        return endDeviceControlTypeMRID;
    }

    /**
     * Returns a ist of all possible groups of {@link DeviceMessageId}s which might be used for the given given {@link EndDeviceControlType}
     */
    public List<List<DeviceMessageId>> getPossibleDeviceMessageIdGroups() {
        return possibleDeviceMessageIdGroups;
    }

    /**
     * Returns a list containing all possible {@link DeviceMessageId}s which might be used for the given {@link EndDeviceControlType}
     */
    public List<DeviceMessageId> getPossibleDeviceMessageIds() {
        List<DeviceMessageId> allDeviceMessageIds = new ArrayList<>();
        possibleDeviceMessageIdGroups.stream().forEach(allDeviceMessageIds::addAll);
        return allDeviceMessageIds;
    }

    public static EndDeviceControlTypeMapping getMappingFor(EndDeviceControlType endDeviceControlType) {
        for (EndDeviceControlTypeMapping controlTypeMapping : values()) {
            if (controlTypeMapping.getEndDeviceControlTypeMRID().equals(endDeviceControlType.getMRID())) {
                return controlTypeMapping;
            }
        }
        return OTHER;
    }

    public Optional<EndDeviceCommand> getNewEndDeviceCommand(EndDevice endDevice, EndDeviceControlType endDeviceControlType, List<DeviceMessageId> possibleDeviceMessageIds, DeviceService deviceService, DeviceMessageSpecificationService deviceMessageSpecificationService, Thesaurus thesaurus) {
        return Optional.empty();
    }
}