package com.energyict.protocolimplv2.nta.esmr50.itron;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimplv2.nta.esmr50.common.events.ESMR50MbusEventLog;

import java.util.Date;
import java.util.List;

import static com.energyict.protocol.MeterEvent.*;

/**
 *
 */

//TODO: md: some events specific for slaved devices overriden here with generic event codes; check correctness
public class ItronMBusEventLog extends ESMR50MbusEventLog {

    public ItronMBusEventLog(DataContainer dc) {
        super(dc);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int protocolCode) {
        Event event = Event.forProtocolCode(protocolCode);
        if (event!=null) {
            meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), event.getEiCode(), protocolCode, event.getMessage()));
        }
    }

    public enum Event {
        BATTERY_LOW                         (100, OTHER,                    "Battery low"),
        BATTERY_CONSUMPTION_HIGH            (101, OTHER,                    "Battery consumption high"),
        REVERSE_FLOW                        (102, OTHER,                    "Reverse flow"),
        TAMPER_P2                           (103, OTHER,                    "Tamper P2"),
        TAMPER_P0                           (104, OTHER,                    "Tamper P0"),
        TAMPER_CASE                         (105, OTHER,                    "Tamper case"),
        TAMPER_MAGNETIC                     (106, OTHER,                    "Tamper magnetic"),
        TEMP_OUT_OF_RANGE                   (107, OTHER,                    "Temp out of range"),
        CLOCK_SYNC_ERROR                    (108, CLOCK_INVALID,            "Clock sync error"),
        SW_ERROR                            (109, OTHER,                    "Software checksum error"),
        EVENT_WATCHDOG_ERROR                (110, WATCHDOG_ERROR,           "Watchdog error"),
        SYSTEM_HW_ERROR                     (111, OTHER,                    "Selfcheck failure"),
        CFG_CALIBRATION_ERROR               (112, OTHER,                    "CFG Calibration error"),
        HIGH_FLOW_ERROR                     (113, OTHER,                    "Flow higher than Qmax (High flow)"),
        TEMP_SENSOR_ERROR                   (114, OTHER,                    "Temp sensor error"),
        BINDING_FLAG                        (115, OTHER,                    "Binding flag"),
        FIRMWARE_UPGRADE_SUCCESSFUL         (116, OTHER,                    "FW upgrade successful"),
        FIRMWARE_UPGRADE_UNSUCCESSFUL       (117, OTHER,                    "FW upgrade unsuccessful"),
        FUAK_CHANGE_SUCCESSFUL              (118, OTHER,                    "FUAK change successful"),
        // 119      reserved for future use
        EVENT_POWER_FAIL                          (120, POWER_FAIL,         "Powerfail"),
        EVENT_MAX_FLOW                            (121, MAX_FLOW,           "Max Flow"),
        EVENT_TEMP_MIN_LIMIT                      (123, TEMP_MIN_LIMIT,     "TempMinLimit"),
        EVENT_TEMP_MAX_LIMIT                      (124, TEMP_MAX_LIMIT,     "TempMaxLimit"),
        EVENT_PULSE_ERROR                         (125, PULSE_ERROR,        "Pulse error"),
        EVENT_CONSUMPTION_ERROR                   (126, CONSUMPTION_ERROR,  "Consumption Error"),
        EVENT_ELSTER_RESERVED_1                   (127, OTHER,              "Elster reserved 4"),
        EVENT_ELSTER_RESERVED_2                   (128, OTHER,              "Elster reserved 5"),
        EVENT_ELSTER_RESERVED_3                   (129, OTHER,              "Elster reserved 6"),
        EVENT_ELSTER_RESERVED_4                   (130, OTHER,              "Elster reserved 7"),
        EVENT_ELSTER_RESERVED_7                   (131, OTHER,              "Elster reserved 8"),

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

        Event(int protocolCode, int eiCode, String message) {
            this.protocolCode = protocolCode;
            this.eiCode = eiCode;
            this.message = message;
        }

        public static Event forProtocolCode(int protocolCode) {
            for (Event event: values()) {
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


}
