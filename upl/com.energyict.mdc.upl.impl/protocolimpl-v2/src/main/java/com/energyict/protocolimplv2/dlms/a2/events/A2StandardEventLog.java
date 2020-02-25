package com.energyict.protocolimplv2.dlms.a2.events;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.dlms.idis.events.AbstractEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class A2StandardEventLog extends AbstractEvent {

    public A2StandardEventLog(TimeZone timeZone, DataContainer dataContainer) {
        super(dataContainer, timeZone);
    }

    @Override
    public List<MeterEvent> getMeterEvents() {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        int size = this.dcEvents.getRoot().getNrOfElements();
        Date eventTimeStamp;
        for (int i = 0; i <= (size - 1); i++) {
            int eventId = (int) this.dcEvents.getRoot().getStructure(i).getValue(2) & 0xFF; // To prevent negative values
            long unixTime = (long) dcEvents.getRoot().getStructure(i).getInteger(0);
            eventTimeStamp = new Date(unixTime * 1000);
            buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
        return meterEvents;
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case 3:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.LOG_RESET, eventId, "Log reset"));
                break;
            case 13:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MANUAL_CONNECTION, eventId, "Local communication start"));
                break;
            case 14:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.LOCAL_DISCONNECTION, eventId, "Local communication end"));
                break;
            case 16:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CONFIGURATIONCHANGE, eventId, "Not metrological parameter configuration"));
                break;
            case 40:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.GAS_FLOW_RATE_ABOVE_THRESHOLD_START, eventId, "Gas flow rate above threshold start"));
                break;
            case 41:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.GAS_FLOW_RATE_ABOVE_THRESHOLD_END, eventId, "Gas flow rate above threshold end"));
                break;
            case 42:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REVERSE_RUN, eventId, "Gas reverse flow start"));
                break;
            case 43:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REVERSE_RUN, eventId, "Gas reverse flow end"));
                break;
            case 58:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.LIMITER_THRESHOLD_EXCEEDED, eventId, "Gas temperature above physical threshold start"));
                break;
            case 59:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.LIMITER_THRESHOLD_EXCEEDED, eventId, "Gas temperature above physical threshold end"));
                break;
            case 60:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.LIMITER_THRESHOLD_EXCEEDED, eventId, "Gas temperature below physical threshold start"));
                break;
            case 61:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.LIMITER_THRESHOLD_EXCEEDED, eventId, "Gas temperature below physical threshold end"));
                break;
            case 62:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.LIMITER_THRESHOLD_EXCEEDED, eventId, "Temperature failure start"));
                break;
            case 63:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.LIMITER_THRESHOLD_EXCEEDED, eventId, "Temperature failure end"));
                break;
            case 68:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CONFIGURATIONCHANGE, eventId, "Password changed"));
                break;
            case 75:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.BATTERY_VOLTAGE_LOW, eventId, "Battery level below low level end"));
                break;
            case 84:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REMOTE_DISCONNECTION, eventId, "Remote communication failure"));
                break;
            case 88:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REMOTE_CONNECTION, eventId, "Push error start"));
                break;
            case 95:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.BATTERY_VOLTAGE_LOW, eventId, "Battery below critical level"));
                break;
            case 101:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FIRMWARE_UPGRADE, eventId, "Firmware update activation failure"));
                break;
            case 115:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PHYSICAL_MODULE_DISCONNECT, eventId, "Physical module disconnect"));
                break;
            case 121:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.UNAUTHORIZED_ACCESS, eventId, "Unauthorized access"));
                break;
            case 126:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.DATABASE_RESET_AFTER_UPDATE, eventId, "Database reset after update"));
                break;
            case 134:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.POWER_LEVEL_INCREASED, eventId, "Power level increased"));
                break;
            case 135:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.POWER_LEVEL_DECREASED, eventId, "Power level decreased"));
                break;
            case 136:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.POWER_LEVEL_MAXIMUM_REACHED, eventId, "Power level maximum reached"));
                break;
            case 137:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.POWER_LEVEL_MINIMUM_REACHED, eventId, "Power level minimum reached"));
                break;
            case 138:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PM1_CHANNEL_CHANGED, eventId, "PM1 channel changed"));
                break;
            case 139:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PM1_ACTIVE_MODE_START, eventId, "PM1 active mode start"));
                break;
            case 140:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PM1_ACTIVE_MODE_END, eventId, "PM1 active mode end"));
                break;
            case 141:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PM1_ORPHANED_MODE_START, eventId, "PM1 orphaned mode start"));
                break;
            case 142:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PM1_ORPHANED_MODE_END, eventId, "PM1 orphaned mode end"));
                break;
            case 144:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PM1_PIB_UPDATED, eventId, "PM1 PIB updated"));
                break;
            case 145:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PM1_MIB_UPDATED, eventId, "PM1 MIB updated"));
                break;
            case 146:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PM1_SYNC_ACCESS_CHANGED, eventId, "PM1 sync access changed"));
                break;
            case 147:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PM1_SYNC_PERIOD_CHANGED, eventId, "PM1 sync period changed"));
                break;
            case 148:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PM1_MAINTENANCE_WINDOW_CHANGED, eventId, "PM1 maintenance window changed"));
                break;
            case 149:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PM1_ORPHANED_THRESHOLD_CHANGED, eventId, "PM1 orphaned threshold changed"));
                break;
            case 150:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PM1_AFFILIATION_PARAMS_CHANGED, eventId, "PM1 affiliation params changed"));
                break;
            case 151:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SECONDARY_ADDRESS_RF_CHANGED, eventId, "Secondary address rf changed"));
                break;
            case 152:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.VALVE_PGV_CONFIGURATION_CHANGED, eventId, "Valve PGV configuration changed"));
                break;
            case 155:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PUSH_SCHEDULER1_CHANGED, eventId, "Push scheduler 1 changed"));
                break;
            case 156:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PUSH_SETUP1_CHANGED, eventId, "Push setup 1 changed"));
                break;
            case 157:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PUSH_SCHEDULER2_CHANGED, eventId, "Push scheduler 2 changed"));
                break;
            case 158:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PUSH_SETUP2_CHANGED, eventId, "Push setup 2 changed"));
                break;
            case 159:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PUSH_SCHEDULER3_CHANGED, eventId, "Push scheduler 3 changed"));
                break;
            case 160:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PUSH_SETUP3_CHANGED, eventId, "Push setup 3 changed"));
                break;
            case 161:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PUSH_SCHEDULER4_CHANGED, eventId, "Push scheduler 4 changed"));
                break;
            case 162:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PUSH_SETUP4_CHANGED, eventId, "Push setup 4 changed"));
                break;
            case 164:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.ENABLING_INSTALLER_MANTAINER, eventId, "Enabling installer maintainer"));
                break;
            case 165:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FC_THRESHOLDS_CHANGED, eventId, "FC thresholds changed"));
                break;
            case 171:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REMOTE_CONNECTION, eventId, "Remote connection start"));
                break;
            case 174:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MAINTENANCE_WINDOW_HW_FAILURE, eventId, "Maintenance window hw failure"));
                break;
            case 175:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MAINTENANCE_WINDOW_SW_FAILURE, eventId, "Maintenance window sw failure"));
                break;
            case 176:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MAINTENANCE_WINDOW_START, eventId, "Maintenance window start"));
                break;
            case 177:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MAINTENANCE_WINDOW_END, eventId, "Maintenance window end"));
                break;
            case 181:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.ASSOCIATION_INSTALLER_DISABLED, eventId, "Association installer disabled"));
                break;
            case 192:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.VALVE_POSITION_ERROR, eventId, "Valve position error"));
                break;
            case 193:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.VALVE_ENABLE_OPENING, eventId, "Valve enable opening"));
                break;
            case 195:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MISER_MODE_FAILURE, eventId, "Miser mode failure"));
                break;
            default:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
    }
}
