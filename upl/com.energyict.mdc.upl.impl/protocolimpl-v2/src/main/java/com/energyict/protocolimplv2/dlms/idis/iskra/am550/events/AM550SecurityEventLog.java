package com.energyict.protocolimplv2.dlms.idis.iskra.am550.events;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.dlms.idis.events.AbstractEvent;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Dmitry Borisov on 26/10/2021.
 */
public class AM550SecurityEventLog extends AbstractEvent {

    public AM550SecurityEventLog(DataContainer dc, TimeZone timeZone) {
        super(dc, timeZone);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case 1:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.POWERDOWN, eventId, "Power down"));
                break;
            case 2:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.POWERUP, eventId, "Power up"));
                break;
            case 4:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SETCLOCK_BEFORE, eventId, "Clock adjusted (old date/time)"));
                break;
            case 5:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SETCLOCK_AFTER, eventId, "Clock adjusted (new date/time)"));
                break;
            case 6:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CLOCK_INVALID, eventId, "Clock invalid, power reserve may be exhausted"));
                break;
            case 10:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.ERROR_REGISTER_CLEARED, eventId, "Error register cleared"));
                break;
            case 11:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.ALARM_REGISTER_CLEARED, eventId, "Alarm register cleared"));
                break;
            case 12:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PROGRAM_MEMORY_ERROR, eventId, "Program memory error"));
                break;
            case 13:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.RAM_MEMORY_ERROR, eventId, "RAM  error"));
                break;
            case 14:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.NV_MEMORY_ERROR, eventId, "NV memory error"));
                break;
            case 15:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.WATCHDOG_ERROR, eventId, "Watchdog error"));
                break;
            case 17:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FIRMWARE_READY_FOR_ACTIVATION, eventId, "Firmware ready for activation"));
                break;
            case 18:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FIRMWARE_ACTIVATED, eventId, "Firmware activated"));
                break;
            case 40:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.TERMINAL_OPENED, eventId, "Terminal cover removed"));
                break;
            case 41:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.TERMINAL_COVER_CLOSED, eventId, "Terminal cover closed"));
                break;
            case 42:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.STRONG_DC_FIELD_DETECTED, eventId, "Strong DC field detected"));
                break;
            case 43:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.NO_STRONG_DC_FIELD_ANYMORE, eventId, "No strong DC field anymore"));
                break;
            case 44:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.METER_COVER_OPENED, eventId, "Meter cover removed"));
                break;
            case 45:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.METER_COVER_CLOSED, eventId, "Meter cover closed"));
                break;
            case 46:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.N_TIMES_WRONG_PASSWORD, eventId, "Association authentication failure (n time failed authentication)"));
                break;
            case 48:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CONFIGURATIONCHANGE, eventId, "Global key(s) changed"));
                break;
            case 49:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.N_TIMES_WRONG_PASSWORD, eventId, "Decryption or authentication failure (n time failure)"));
                break;
            case 50:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REPLAY_ATTACK, eventId, "Replay attack"));
                break;
            case 51:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FIRMWARE_VERIFICATION_FAIL, eventId, "FW verification failed"));
                break;
            case 59:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.DISCONNECTOR_READY_FOR_RECONN, eventId, "Disconnector ready for manual reconnection"));
                break;
            case 60:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MANUAL_DISCONNECTION, eventId, "Manual disconnection"));
                break;
            case 62:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REMOTE_DISCONNECTION, eventId, "Remote disconnection"));
                break;
            case 63:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REMOTE_CONNECTION, eventId, "Remote connection"));
                break;
            case 64:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.LOCAL_DISCONNECTION, eventId, "Local disconnection"));
                break;
            case 105:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "New M-Bus device installed channel 1"));
                break;
            case 115:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "New M-Bus device installed channel 2"));
                break;
            case 125:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "New M-Bus device installed channel 2"));
                break;
            case 135:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "New M-Bus device installed channel 2"));
                break;
            case 204:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FIRMWARE_ACTIVATED, eventId, "Firmware golden copy activated"));
                break;
            case 226:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FIRMWARE_VERIFICATION_FAIL, eventId, "LR firmware verification failed"));
                break;
            case 227:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FIRMWARE_READY_FOR_ACTIVATION, eventId, "LR firmware ready for activation"));
                break;
            case 228:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FIRMWARE_ACTIVATED, eventId, "LR firmware activated"));
                break;
            case 238:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.STATUS_CHANGED, eventId, "Disconnector physical connect"));
                break;
            case 244:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.METER_COVER_OPENED, eventId, "Module cover opened"));
                break;
            case 245:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.METER_COVER_CLOSED, eventId, "Module cover closed"));
                break;
            case 246:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "M-Bus Channel 1 key change"));
                break;
            case 247:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "M-Bus Channel 2 key change"));
                break;
            case 248:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "M-Bus Channel 3 key change"));
                break;
            case 249:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "M-Bus Channel 4 key change"));
                break;
            case 250:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Decryption or authentication successful"));
                break;
            case 255:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.EVENT_LOG_CLEARED, eventId, "Security event log cleared"));
                break;
            default:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
    }
}
