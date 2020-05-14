package com.energyict.protocolimplv2.dlms.a2.events;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.dlms.idis.events.AbstractEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class A2MetrologicalEventLog extends AbstractEvent {
    public A2MetrologicalEventLog(TimeZone timeZone, DataContainer dataContainer) {
        super(dataContainer, timeZone);
    }

    @Override
    public List<MeterEvent> getMeterEvents() {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        int size = this.dcEvents.getRoot().getNrOfElements();
        Date eventTimeStamp;
        for (int i = 0; i <= (size - 1); i++) {
            int eventId = (int) this.dcEvents.getRoot().getStructure(i).getValue(1) & 0xFF; // To prevent negative values
            long unixTime = (long) dcEvents.getRoot().getStructure(i).getLong(0);
            eventTimeStamp = new Date(unixTime * 1000);
            buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
        return meterEvents;
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case 1:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.DEVICE_RESET, eventId, "Device reset"));
                break;
            case 2:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.METROLOGIC_RESET, eventId, "Metrologic reset"));
                break;
            case 8:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.ACTIVATION_NEW_TARIFF_PLAN, eventId, "Activation new tariff plan"));
                break;
            case 9:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PLANNING_NEW_TARIFF_PLAN, eventId, "Planning new tariff plan"));
                break;
            case 10:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CLOCK_SYNC_FAIL, eventId, "Clock sync fail"));
                break;
            case 12:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CLOCK_SYNC, eventId, "Clock sync"));
                break;
            case 15:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.METROLOGICAL_PARAMETER_CONFIGURATION, eventId, "Metrological parameter configuration"));
                break;
            case 20:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MEASURE_ALGORITHM_ERROR_START, eventId, "Measure algorithm error start"));
                break;
            case 21:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MEASURE_ALGORITHM_ERROR_END, eventId, "Measure algorithm error end"));
                break;
            case 22:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.GENERAL_ERROR_DEVICE_START, eventId, "General error device start"));
                break;
            case 23:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.GENERAL_ERROR_DEVICE_END, eventId, "General error device end"));
                break;
            case 26:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.BUFFER_FULL, eventId, "Buffer full"));
                break;
            case 27:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.BUFFER_ALMOST_FULL, eventId, "Buffer almost full"));
                break;
            case 30:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.VALVE_CLOSED_BECAUSE_OF_COMMAND, eventId, "Valve closed because of command"));
                break;
            case 31:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.VALVE_OPENED, eventId, "Valve opened"));
                break;
            case 66:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MEMORY_FAILURE, eventId, "Memory failure"));
                break;
            case 67:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.UNITS_STATUS_CHANGED, eventId, "Uni ts status changed"));
                break;
            case 72:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MAIN_POWER_OUTAGE_START, eventId, "Main power outage start"));
                break;
            case 73:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MAIN_POWER_OUTAGE_END, eventId, "Main power outage end"));
                break;
            case 74:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.BATTERY_LEVEL_BELOW_LOW_LEVEL_START, eventId, "Battery level below low level start"));
                break;
            case 80:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.DEVICE_TAMPER_DETECTION_START, eventId, "Device tamper detection start"));
                break;
            case 81:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.DEVICE_TAMPER_DETECTION_END, eventId, "Device tamper detection end"));
                break;
            case 85:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CRITICAL_SOFTWARE_ERROR, eventId, "Critical software error"));
                break;
            case 86:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.DST_START, eventId, "DST start"));
                break;
            case 87:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.DST_END, eventId, "DST end"));
                break;
            case 92:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.BILLING_PERIOD_CLOSING_LOCAL_REQUEST, eventId, "Billing period closing local request"));
                break;
            case 93:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.BILLING_PERIOD_CLOSING_REMOTE_REQUEST, eventId, "Billing period closing remote request"));
                break;
            case 94:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.BATTERY_ABOVE_CRITICAL_LEVEL, eventId, "Battery above critical level"));
                break;
            case 96:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FIRMWARE_UPDATE_START, eventId, "Firmware update start"));
                break;
            case 97:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FIRMWARE_UPDATE_DATE_ACTIVATION, eventId, "Firmware update date activation"));
                break;
            case 98:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FIRMWARE_UPDATE_VERIFY_OK, eventId, "Firmware update verify ok"));
                break;
            case 99:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FIRMWARE_UPDATE_VERIFY_FAILURE, eventId, "Firmware update verify failure"));
                break;
            case 100:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FIRMWARE_UPDATE_ACTIVATION_OK, eventId, "Firmware update activation ok"));
                break;
            case 102:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CLOSE_VALVE_LEAKAGE_CAUSE, eventId, "Close valve leakage cause"));
                break;
            case 103:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CLOSE_VALVE_BATTERY_REMOVED_WITH_NO_AUTH, eventId, "Close valve battery removed with no auth"));
                break;
            case 104:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CLOSE_VALVE_BATTERY_BELOW_CRITICAL_POINT, eventId, "Close valve battery below critical point"));
                break;
            case 105:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CLOSE_VALVE_MEASURE_FAILURE, eventId, "Close valve measure failure"));
                break;
            case 106:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.VALVE_PASSWORD_INVALID, eventId, "Valve password invalid"));
                break;
            case 107:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CLOSE_VALVE_COMMUNICATION_TIMEOUT, eventId, "Close valve communication timeout"));
                break;
            case 108:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.VALVE_NEW_PASSWORD, eventId, "Valve new password"));
                break;
            case 109:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.VALVE_READY_PASSWORD_VALID, eventId, "Valve ready password valid"));
                break;
            case 110:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.VALVE_READY_CONNECTION_OK, eventId, "Valve ready connection ok"));
                break;
            case 111:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.VALVE_RECONNECT_START, eventId, "Valve reconnect start"));
                break;
            case 112:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.VALVE_RECONNECT_END, eventId, "Valve reconnect end"));
                break;
            case 113:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.VALVE_IS_CLOSED_BUT_LEAKAGE_IS_PRESENT, eventId, "Valve is closed but leakage is present"));
                break;
            case 114:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.VALVE_CANNOT_OPEN_CLOSE, eventId, "Valve cannot open close"));
                break;
            case 116:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.EXTERNAL_FIELD_APPLICATION_INTERFERING_START, eventId, "External field application interfering start"));
                break;
            case 117:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.EXTERNAL_FIELD_APPLICATION_INTERFERING_END, eventId, "External field application interfering end"));
                break;
            case 118:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.ACCESS_TO_ELECTRONIC, eventId, "Access to electronic"));
                break;
            case 124:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.UNAUTHORIZED_BATTERY_REMOVE, eventId, "Unauthorized battery remove"));
                break;
            case 125:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.DATABASE_RESET, eventId, "Database reset"));
                break;
            case 127:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.DATABASE_CORRUPTED, eventId, "Database corrupted"));
                break;
            case 128:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.UPDATED_MASTERKEY, eventId, "Updated master key"));
                break;
            case 129:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.UPDATED_KEYC, eventId, "Updated keyC"));
                break;
            case 130:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.UPDATED_KEYT, eventId, "Updated keyT"));
                break;
            case 131:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.UPDATED_KEYS, eventId, "Updated keyS"));
                break;
            case 132:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.UPDATED_KEYN, eventId, "Updated keyN"));
                break;
            case 133:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.UPDATED_KEYM, eventId, "Updated keyM"));
                break;
            case 153:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.GAS_DAY_UPDATED, eventId, "Gas day updated"));
                break;
            case 154:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.BILLING_PERIOD_UPDATED, eventId, "Billing period updated"));
                break;
            case 163:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.INSTALLER_MAINTAINER_USER_CHANGED, eventId, "Installer mantainer user changeD"));
                break;
            case 166:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CLOCK_PARAMETER_SCHANGED, eventId, "Clock parameters changed"));
                break;
            case 167:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SYNC_ALGORITHM_CHANGED, eventId, "Sync algorithm changed"));
                break;
            case 168:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PDR_CHANGED, eventId, "PDR changed"));
                break;
            case 169:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.DEFAULT_TEMPERATURE, eventId, "Default temperature changed"));
                break;
            case 170:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FALLBACK_TEMPERATURE_CHANGED, eventId, "Fallback temperature changed"));
                break;
            case 179:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.VALVE_CLOSE_FOR_MAX_FRAUD_ATTEMPTS, eventId, "Valve close for max fraud attempts"));
                break;
            case 180:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.VALVE_CLOSE_FOR_EXCEEDED_BATTERY_REMOVAL_TIME, eventId, "Valve close for exceeded battery removal time"));
                break;
            case 194:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.VALVE_CONFIGURATION_PGV_BIT8_CHANGED, eventId, "Valve configuration PGV bit 8 changed"));
                break;
            default:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
    }
}
