package com.energyict.protocolimplv2.nta.esmr50.elster.events;

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

enum ElsterEventEnum implements EventEnum
{
    EVENT_BATTERY_LOW                         (100, BATTERY_VOLTAGE_LOW,      "Battery low"),
    EVENT_BATTERY_CONSUMPTION_HIGH            (101, BATTERY_CONSUMPTION_HIGH, "Battery consumption high"),
    EVENT_REVERSE_FLOW                        (102, REVERSE_FLOW,             "Reverse flow"),
    EVENT_TAMPER_P2                           (103, TAMPER_P2,                "Tamper P2"),
    EVENT_TAMPER_P0                           (104, TAMPER_P0,                "Tamper P0"),
    EVENT_TAMPER_CASE                         (105, TAMPER_CASE,              "Tamper case"),
    EVENT_TAMPER_MAGNETIC                     (106, OTHER,                    "Tamper magnetic"),
    EVENT_TEMP_OUT_OF_RANGE                   (107, OTHER,                    "Temp out of range"),
    EVENT_CLOCK_SYNC_ERROR                    (108, CLOCK_INVALID,            "Clock sync error"),
    EVENT_SW_ERROR                            (109, OTHER,                    "Software checksum error"),
    EVENT_EVENT_WATCHDOG_ERROR                (110, WATCHDOG_ERROR,           "Watchdog error"),
    EVENT_SYSTEM_HW_ERROR                     (111, SYSTEM_HW_ERROR,          "Selfcheck failure"),
    EVENT_CFG_CALIBRATION_ERROR               (112, CFG_CALIBRATION_ERROR,    "CFG Calibration error"),
    EVENT_HIGH_FLOW_ERROR                     (113, LIMITER_THRESHOLD_EXCEEDED,"Flow higher than Qmax (High flow)"),
    EVENT_TEMP_SENSOR_ERROR                   (114, TEMPERATURE_SENSOR_ERROR, "Temp sensor error"),
    EVENT_BINDING_FLAG                        (115, BINDING_FLAG,             "Binding flag"),
    FIRMWARE_UPGRADE_SUCCESSFUL               (116, OTHER,                    "FW upgrade successful"),
    FIRMWARE_UPGRADE_UNSUCCESSFUL             (117, OTHER,                    "FW upgrade unsuccessful"),
    FUAK_CHANGE_SUCCESSFUL                    (118, OTHER,                    "FUAK change successful"),
    // 119      reserved for future use
    EVENT_TAMPER_BATTERY                      (120, TAMPER_BATTERY,     "Tamper battery"),
    EVENT_HLC_DAMAGE                          (121, HLC_DAMAGE,         "HLC damage"),
    EVENT_PERMANENT_LOG_FILLED_UP_90_PERSENT  (122, PERMANENT_LOG_FILLED_UP_90_PERSENT, "Permanent Log filled up to 90%"),
    EVENT_DEVICE_ABOUT_HIBERNATION_MODE       (123, DEVICE_ABOUT_HIBERNATION_MODE, "Device is about to enter hibernation mode"),
    EVENT_BROKEN_CASE_SWITCH                  (124, BROKEN_CASE_SWITCH, "Broken case switch"),
    EVENT_ELSTER_RESERVED_2                   (125, OTHER,              "Elster reserved 2"),
    EVENT_ELSTER_RESERVED_3                   (126, OTHER,              "Elster reserved 3"),
    EVENT_ELSTER_RESERVED_4                   (127, OTHER,              "Elster reserved 4"),
    EVENT_ELSTER_RESERVED_5                   (128, OTHER,              "Elster reserved 5"),
    EVENT_ELSTER_RESERVED_6                   (129, OTHER,              "Elster reserved 6"),
    EVENT_ELSTER_RESERVED_7                   (130, OTHER,              "Elster reserved 7"),
    EVENT_ELSTER_RESERVED_8                   (131, OTHER,              "Elster reserved 8"),

    MBUS_COMMUNICATION_ERROR                  (132, COMMUNICATION_ERROR_MBUS, "M-Bus communication error"),
    MBUS_COMMUNICATION_ERROR_RESOLVED         (133, COMMUNICATION_OK_MBUS,    "M-Bus communication error resolved"),
    MBUS_SECURITY_ERROR                       (134, COMMUNICATION_ERROR_MBUS, "M-Bus security error"),
    MBUS_SECURITY_ERROR_RESOLVED              (135, COMMUNICATION_OK_MBUS,    "M-Bus security error resolved"),
    NEW_MBUS_DISCOVERED                       (136, OTHER,                    "New M-Bus device discovered"),
    // 137-254  reserved for future use
    LOGBOOK_CLEARED                           (255, EVENT_LOG_CLEARED,        "Mbus event log profile cleared");

    private final int protocolCode;
    private final int eiCode;
    private final String message;

    ElsterEventEnum(int protocolCode, int eiCode, String message) {
            this.protocolCode = protocolCode;
            this.eiCode = eiCode;
            this.message = message;
        }

    public static ElsterEventEnum forProtocolCode(int protocolCode) {
        for (ElsterEventEnum event: values()) {
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
public class ElsterMBusEventLog extends ESMR50MbusEventLog<ElsterEventEnum> {

    public ElsterMBusEventLog(DataContainer dc) {
        super(dc);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int protocolCode) {
        ElsterEventEnum event = ElsterEventEnum.forProtocolCode(protocolCode);
        if (event!=null) {
            meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), event.getEiCode(), protocolCode, event.getMessage()));
        }
    }
}
