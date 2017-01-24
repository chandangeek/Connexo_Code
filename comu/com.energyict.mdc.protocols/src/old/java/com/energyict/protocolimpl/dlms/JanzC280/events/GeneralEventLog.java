package com.energyict.protocolimpl.dlms.JanzC280.events;

import com.energyict.dlms.DataContainer;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class GeneralEventLog extends AbstractEvent {

    public GeneralEventLog(TimeZone timeZone, DataContainer dc) {
        super(dc, timeZone);
    }

    /**
     * @return the MeterEvent List
     * @throws java.io.IOException
     */
    @Override
    public List<MeterEvent> getMeterEvents() throws IOException {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        int size = this.dcEvents.getRoot().getNrOfElements();

        for (int i = 0; i <= (size - 1); i++) {
            int eventDefHighByte = (byte) this.dcEvents.getRoot().getStructure(i).getValue(0);
            int eventDefLowByte = (byte) this.dcEvents.getRoot().getStructure(i).getValue(1);

            Calendar cal = Calendar.getInstance(this.timeZone);         // Received timestamp is in the device timezone, not GMT!
            cal.setTimeInMillis((946684800 + this.dcEvents.getRoot().getStructure(i).getValue(2)) * 1000);    // Number of seconds [1970 - 2000] + number of seconds [2000 - meter time]
            if (cal.getTime() != null) {
                buildMeterEvent(meterEvents, cal.getTime(), eventDefHighByte, eventDefLowByte);
            }
        }
        return meterEvents;
    }

    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventDefHighByte, int eventDefLowByte) {
        int eventId = Integer.parseInt(String.valueOf(eventDefHighByte)
                + (eventDefLowByte < 10 ? "0" : "")
                + String.valueOf(eventDefLowByte));

        switch (eventDefHighByte) {
            case 0:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Not available"));
                break;
            case 1:
                switch (eventDefLowByte) {
                    case 1:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Password Fraud"));
                        break;
                    case 2:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.COVER_OPENED, eventId, "Modem lid opened"));
                        break;
                    default:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Detection of fraud"));
                        break;
                }
                break;
            case 2:
                switch (eventDefLowByte) {
                    case 1:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CONFIGURATIONCHANGE, eventId, "Permission of energy supply: Connected due to incoherent data"));
                        break;
                    case 2:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CONFIGURATIONCHANGE, eventId, "Permission of energy supply: ICP opened due to excessive power"));
                        break;
                    case 3:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CONFIGURATIONCHANGE, eventId, "Permission of energy supply: ICP closed"));
                        break;
                    case 4:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CONFIGURATIONCHANGE, eventId, "Permission of energy supply: ICP opened on commercial command"));
                        break;
                    default:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Permission of energy supply"));
                        break;
                }
                break;
            case 4:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CONFIGURATIONCHANGE, eventId, "Alteration of the configuration"));
                break;
            case 5:
                switch (eventDefLowByte) {
                    case 1:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SETCLOCK, eventId, "Event of clock: Initialized with default data"));
                        break;
                    case 2:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SETCLOCK, eventId, "Event of clock: Station 1 Time"));
                        break;
                    case 3:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SETCLOCK, eventId, "Event of clock: Station 2 Time"));
                        break;
                    case 4:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SETCLOCK_BEFORE, eventId, "Event of clock: Before Clock Sync"));
                        break;
                    case 5:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SETCLOCK_AFTER, eventId, "Event of clock: After Clock Sync"));
                        break;
                    default:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Event of clock"));
                        break;
                }
                break;
            case 6:
                switch (eventDefLowByte) {
                    case 1:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.BILLING_ACTION, eventId, "Close of the billing period: Billing Reset Error"));
                        break;
                    case 2:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.BILLING_ACTION, eventId, "Close of the billing period: Billing Reset"));
                        break;
                    case 3:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.BILLING_ACTION, eventId, "Close of the billing period: Maximum Demand Reset"));
                        break;
                    case 4:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.BILLING_ACTION, eventId, "Close of the billing period: Automatic Billing Reset"));
                        break;
                    case 5:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.BILLING_ACTION, eventId, "Close of the billing period: Manual Billing Reset"));
                        break;
                    default:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Close of the billing period"));
                        break;
                }
                break;
            case 7:
                switch (eventDefLowByte) {
                    case 4:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PHASE_FAILURE, eventId, "Imperfection of tension for phase - Monophase Meter Phase Error"));
                        break;
                    default:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Imperfection of tension for phase"));
                        break;
                }
                break;
            case 8:
                switch (eventDefLowByte) {
                    case 4:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PHASE_FAILURE, eventId, "Reestablishment of the tension for phase - Monophase Meter Phase Error"));
                        break;
                    default:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Reestablishment of the tension for phase"));
                        break;
                }
                break;
            case 9:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FATAL_ERROR, eventId, "Fatal error of the equipment"));
                break;
            case 10:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MEASUREMENT_SYSTEM_ERROR, eventId, "Non-fatal errors of the equipment"));
                break;
            case 11:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MANUAL_CONNECTION, eventId, "Local access"));
                break;
            case 13:
                switch (eventDefLowByte) {
                    case 1:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FIRMWARE_ACTIVATED, eventId, "Firmware Update successful"));
                        break;
                    case 2:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PROGRAM_MEMORY_ERROR, eventId, "Firmware Update Error"));
                        break;
                    default:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Remote update of firmware"));
                        break;
                }
                break;
            case 14:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.RAM_MEMORY_ERROR, eventId, "Alarm of stack imperfection"));
                break;
            case 15:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Event of corruption of data"));
                break;
            case 16:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.RAM_MEMORY_ERROR, eventId, "Event of access to back-up of the RAM memory"));
                break;
            case 17:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Tariff event"));
                break;
            case 18:
                switch (eventDefLowByte) {
                    case 1:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, " Load Diagram event: Load Profile Error"));
                        break;
                    case 2:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, " Load Diagram event: Normal Load Profile Entry"));
                        break;
                    case 3:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, " Load Diagram event: Load Profile Reset"));
                        break;
                    case 4:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CONFIGURATIONCHANGE, eventId, " Load Diagram event: Load Profile Configuration"));
                        break;
                    default:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Event of Load Diagram"));
                        break;
                }
                break;
            case 19:
                switch (eventDefLowByte) {
                    case 1:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Log Book Error"));
                        break;
                    case 2:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Log Book Reset"));
                        break;
                    default:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "General event"));
                        break;
                }
                break;
            case 20:
                switch (eventDefLowByte) {
                    case 1:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Insufficient watchdog space"));
                        break;
                    case 2:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Watchdog Error category 2"));
                        break;
                    case 3:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Watchdog Error category 3"));
                        break;
                    case 4:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Watchdog Error category 4"));
                        break;
                    case 5:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Watchdog Error category 5"));
                        break;
                    case 6:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Watchdog Error category 6"));
                        break;
                    case 7:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Watchdog Error category 7"));
                        break;
                    case 8:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Watchdog Error category 8"));
                        break;
                    case 9:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Watchdog Error category 9"));
                        break;
                    case 10:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Watchdog Error category 10"));
                        break;
                    default:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Internal event"));
                        break;
                }
                break;
            case 21:
                switch (eventDefLowByte) {
                    case 1:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Quality of Service Error"));
                        break;
                    case 2:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Quality of Service Log Reset"));
                        break;
                    default:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Event Quality of Service"));
                        break;
                }
                break;
            case 22:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.POWERUP, eventId, "Interrupting event Cut of Power"));
                break;
            case 24:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PHASE_FAILURE, eventId, "Event of overload: Monophase Meter Overload"));
                    break;
            default:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode:" + eventId));
                break;
        }
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        throw new UnsupportedOperationException();
    }
}