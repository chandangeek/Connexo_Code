package com.energyict.protocolimplv2.nta.esmr50.itron.events;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimplv2.nta.esmr50.common.events.ESMR50MbusEventLog;
import com.energyict.protocolimplv2.nta.esmr50.common.events.EventEnum;


import java.util.Date;
import java.util.List;

import static com.energyict.protocol.MeterEvent.*;

/**
 *
 */

enum ItronEventEnum implements EventEnum {
    EVENT_BATTERY_LOW                   (100, BATTERY_VOLTAGE_LOW,      "Battery low"),
    EVENT_BATTERY_CONSUMPTION_HIGH      (101, BATTERY_CONSUMPTION_HIGH, "Battery consumption high"),
    EVENT_REVERSE_FLOW                  (102, REVERSE_FLOW,             "Reverse flow"),
    EVENT_TAMPER_P2                     (103, TAMPER_P2,                "Tamper P2"),
    EVENT_TAMPER_P0                     (104, TAMPER_P0,                "Tamper P0"),
    EVENT_TAMPER_CASE                   (105, TAMPER_CASE,              "Tamper case"),
    EVENT_TAMPER_MAGNETIC               (106, OTHER,                    "Tamper magnetic"),
    EVENT_TEMP_OUT_OF_RANGE             (107, OTHER,                    "Temp out of range"),
    EVENT_CLOCK_SYNC_ERROR              (108, CLOCK_INVALID,            "Clock sync error"),
    EVENT_SW_ERROR                      (109, OTHER,                    "Software checksum error"),
    EVENT_EVENT_WATCHDOG_ERROR          (110, WATCHDOG_ERROR,           "Watchdog error"),
    EVENT_SYSTEM_HW_ERROR               (111, SYSTEM_HW_ERROR,          "Selfcheck failure"),
    EVENT_CFG_CALIBRATION_ERROR         (112, CFG_CALIBRATION_ERROR,    "CFG Calibration error"),
    EVENT_HIGH_FLOW_ERROR               (113, LIMITER_THRESHOLD_EXCEEDED,"Flow higher than Qmax (High flow)"),
    EVENT_TEMP_SENSOR_ERROR             (114, TEMPERATURE_SENSOR_ERROR, "Temp sensor error"),
    EVENT_BINDING_FLAG                  (115, BINDING_FLAG,             "Binding flag"),
    FIRMWARE_UPGRADE_SUCCESSFUL         (116, OTHER,                    "FW upgrade successful"),
    FIRMWARE_UPGRADE_UNSUCCESSFUL       (117, OTHER,                    "FW upgrade unsuccessful"),
    FUAK_CHANGE_SUCCESSFUL              (118, OTHER,                    "FUAK change successful"),
    // 119      reserved for future use
    EVENT_POWER_FAIL                    (120, POWER_FAIL,         "Powerfail"),
    EVENT_MAX_FLOW                      (121, MAX_FLOW,           "Max Flow"),
    EVENT_TEMP_MIN_LIMIT                (122, TEMP_MIN_LIMIT,     "TempMinLimit"),
    EVENT_TEMP_MAX_LIMIT                (123, TEMP_MAX_LIMIT,     "TempMaxLimit"),
    EVENT_PULSE_ERROR                   (124, PULSE_ERROR,        "Pulse error"),
    EVENT_CONSUMPTION_ERROR             (125, CONSUMPTION_ERROR,  "Consumption Error"),
    EVENT_ELSTER_RESERVED_1             (126, OTHER,              "Elster reserved 1"),
    EVENT_ELSTER_RESERVED_2             (127, OTHER,              "Elster reserved 2"),
    EVENT_ELSTER_RESERVED_3             (128, OTHER,              "Elster reserved 3"),
    EVENT_ELSTER_RESERVED_4             (129, OTHER,              "Elster reserved 4"),
    EVENT_ELSTER_RESERVED_5             (130, OTHER,              "Elster reserved 5"),
    EVENT_ELSTER_RESERVED_6             (131, OTHER,              "Elster reserved 6"),

    MBUS_COMMUNICATION_ERROR            (132, COMMUNICATION_ERROR_MBUS, "M-Bus communication error"),
    MBUS_COMMUNICATION_ERROR_RESOLVED   (133, COMMUNICATION_OK_MBUS,    "M-Bus communication error resolved"),
    MBUS_SECURITY_ERROR                 (134, COMMUNICATION_ERROR_MBUS, "M-Bus security error"),
    MBUS_SECURITY_ERROR_RESOLVED        (135, COMMUNICATION_OK_MBUS,    "M-Bus security error resolved"),
    NEW_MBUS_DISCOVERED                 (136, OTHER,                    "New M-Bus device discovered"),
    // 137-254  reserved for future use
    LOGBOOK_CLEARED                     (255, EVENT_LOG_CLEARED,        "Mbus event log profile cleared");

    private final int protocolCode;
    private final int eiCode;
    private final String message;

    ItronEventEnum(int protocolCode, int eiCode, String message) {
        this.protocolCode = protocolCode;
        this.eiCode = eiCode;
        this.message = message;
    }

    public static ItronEventEnum forProtocolCode(int protocolCode) {
        for (ItronEventEnum event: values()) {
            if (event.getProtocolCode()==protocolCode) {
                return event;
            }
        }
        return null;
    }

    public int getProtocolCode() {
        return protocolCode;
    }

    public int getEiCode() {
        return eiCode;
    }

    public String getMessage() {
        return message;
    }
}

//TODO: md: some events specific for slaved devices overriden here with generic event codes; check correctness
public class ItronMBusEventLog extends ESMR50MbusEventLog<ItronEventEnum> {

    public ItronMBusEventLog(DataContainer dc) {
        super(dc);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int protocolCode) {
        ItronEventEnum event = ItronEventEnum.forProtocolCode(protocolCode);
        if (event!=null) {
            meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), event.getEiCode(), protocolCode, event.getMessage()));
        }
    }
}
