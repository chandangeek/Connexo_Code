package com.elster.jupiter.metering;

import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

public enum EndDeviceControlTypeMapping {

    OTHER                   ("0.0.0.0"),
    //Demand Controls
    RESET                   ("0.8.0.214"),
    //Locad Control Controls
    LOAD_CONTROL_INITITATE  ("0.15.0.54"),
    LOAD_CONTROL_TERMINATE  ("0.15.0.55"),

    //PAN / HAN Controls
    PAN_PAIRING_WINDOW_OPEN ("0.10.73.298"),
    PAN_PAIRING_WINDOW_CLOSE ("0.10.73.299"),
    PAN_TXT_MSG_DISPLAY ("0.13.112.77"),
    PAN_TXT_MSG_CANCEL ("0.13.112.8"),
    PAN_PRICE_SGN_CREATE ("0.20.9.82"),
    PAN_LOAD_CTRL_ALL_EVENT_CANCEL("0.15.148.8"),
    PAN_LOAD_CTRL_EVENT_CANCEL ("0.15.43.8"),
    PAN_LOAD_CTRL_EVENT_SCHEDULE ("0.15.43.300"),
    PAN_NETWORK_ENABLE ("0.23.0.26"),
    PAN_NETWORK_DISABLE ("0.23.0.22"),

    //RCDSwitch controls

    DISABLE_EMERGENCY_SUP_CAPACITY_LIM ("0.31.138.22"),
    ENABLE_EMERGENCY_SUP_CAPACITY_LIM ("0.31.138.26"),
    ARM_REMOTE_SWITCH_FOR_CLOSURE ("0.31.0.5"),
    ARM_REMOTE_SWITCH_FOR_OPEN ("0.31.0.6"),
    CLOSE_REMOTE_SWITCH ("0.31.0.18"),
    DISABLE_SWITCH("0.31.0.22"),
    OPEN_REMOTE_SWITCH ("0.31.0.23"),
    ENABLE_SWITCH("0.31.0.26"),
    DISABLE_SUP_CAPACITY_LIM ("0.31.139.22"),
    ENABLE_SUP_CAPACITY_LIM ("0.31.139.26");


    private static final Logger LOGGER = Logger.getLogger(EndDeviceControlTypeMapping.class.getName());

    private final String endDeviceControlTypeMRID;
    private EndDeviceControlType controlType;

    private EndDeviceControlTypeMapping(String endDeviceControlTypeMRID) {
        this.endDeviceControlTypeMRID = endDeviceControlTypeMRID;
    }

    public int getEisCode() {
        return this.ordinal();
    }

    public String getEndDeviceControlTypeMRID() {
        return endDeviceControlTypeMRID;
    }

    public Optional<EndDeviceControlType> getControlType(MeteringService meteringService) {
        if (controlType == null) {
            Optional<EndDeviceControlType> endDeviceControlType = meteringService.getEndDeviceControlType(this.endDeviceControlTypeMRID);
            if (endDeviceControlType.isPresent()) {
                controlType = endDeviceControlType.orElse(null);
            }
            else {
                LOGGER.severe(() -> "EndDeviceControlType missing: " + this.endDeviceControlTypeMRID);
            }
            return endDeviceControlType;
        }
        else {
            return Optional.of(this.controlType);
        }
    }

    public static Optional<EndDeviceControlType> getControlTypeCorrespondingToEISCode(int eisCode, MeteringService meteringService) {
        return Stream.of(EndDeviceControlTypeMapping.values())
                .filter(each -> each.getEisCode() == eisCode)
                .findFirst()
                .map(each -> each.getControlType(meteringService))
                .orElse(OTHER.getControlType(meteringService));
    }

}
