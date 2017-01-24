package com.energyict.smartmeterprotocolimpl.actaris.sl7000;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 *
 * @author  sva
 */
public class Logbook {

    private static final int DEBUG = 0;

    private TimeZone timeZone;
    private boolean oldFimware;

    /// logbook event type
    private static final int EVENT_PERIODICAL_EOI=0;
    private static final int EVENT_ASYNCHRONOUS_EOI=1;
    private static final int EVENT_PERIODICAL_EOB=2;
    private static final int EVENT_PROGRAMMED_EOB=3;
    private static final int EVENT_ASYNCHRONOUS_EOB=4;
    private static final int EVENT_INDEX_DPM=5;
    private static final int EVENT_RESTORE_INTERNAL_INDEX=6;
    private static final int EVENT_INDEX_CI=7;
    private static final int EVENT_DAY_PROFILE_CL=8;
    private static final int EVENT_RESTORE_INTERNAL_DAY_PROFILE=9;
    private static final int EVENT_DAY_PROFILE_CI=10;
    private static final int EVENT_SEASON_SM=11;
    private static final int EVENT_RESTORE_INTERNAL_SEASON=12;
    private static final int EVENT_SEASON_CI=13;
    private static final int EVENT_DST_WITH_SEASON=14;
    private static final int EVENT_EXTERNAL_SYNCHRO_AND_DST_WITH_SEASON=15;
    private static final int EVENT_ENTER_DOWNLOAD_MODE=16;
    private static final int EVENT_EOR=17;
    private static final int EVENT_ASSOCIATION_LN_PROGRAMMING=18;
    private static final int EVENT_EXCESS_ENERGY_ABOVE_THRESHOLD_ER=19;
    private static final int EVENT_EXCESS_ENERGY_UNDER_THRESHOLD_ER=20;
    private static final int EVENT_EXCESS_ENERGY_ABOVE_THRESHOLD_LP=21;
    private static final int EVENT_EXCESS_ENERGY_UNDER_THRESHOLD_LP=22;
    private static final int EVENT_NON_FATAL_ALARM_APPEARANCE=23;
    private static final int EVENT_NON_FATAL_ALARM_DISAPPEARANCE=24;
    private static final int EVENT_FATAL_ALARM_APPEARANCE=25;
    private static final int EVENT_PARAMETERS_SAVING=26;
    private static final int EVENT_CLEAR_NON_FATAL_ALARMS=27;
    private static final int EVENT_CLEAR_FATAL_ALARMS=28;
    private static final int EVENT_INTERNAL_CLOCK_SYNCHRO=29;
    private static final int EVENT_EXTERNAL_CLOCK_SYNCHRO=30;
    private static final int EVENT_CLOCK_SETTING=31;
    private static final int EVENT_EXTERNAL_SYNCHRO_AND_DST_WITHOUT_SEASON=32;
    private static final int EVENT_DST_WITHOUT_SEASON=33;
    private static final int EVENT_AC_FAIL_APPEARANCE=34;
    private static final int EVENT_AC_FAIL_DISAPPEARANCE=35;
    private static final int EVENT_PWR_FAIL_APPEARANCE=36;
    private static final int EVENT_POWER_UP=37;
    private static final int EVENT_PROGRAMMING_CM=38;
    private static final int EVENT_PROGRAMMING_DI=39;
    private static final int EVENT_CANCEL_PROGRAMMING_DI=40;
    private static final int EVENT_RESET_MEASUREMENT_DATA=41;
    private static final int EVENT_START_MEASUREMENT=42;
    private static final int EVENT_STOP_MEASUREMENT=43;
    private static final int EVENT_START_TRIGGERED_TESTS=44;
    private static final int EVENT_STOP_TRIGGERED_TESTS=45;
    private static final int EVENT_END_OF_CURRENT_DATA_SAVING=46;
    private static final int EVENT_LOAD_PROFILE_RESET=47;
    private static final int EVENT_PASSWORD_RESTORATION=48;
    private static final int EVENT_INDEX_CLOCK_LOSS=49;
    private static final int EVENT_SUCCESSFUL_COMMUNICATION=50;
    private static final int EVENT_COMMUNICATION_WITH_CONTRACT=51;
    private static final int EVENT_PROGRAMMING_CM2=52;

    /** Creates a new instance of Logbook */
    public Logbook(TimeZone timeZone, String firmwareVersion) {
        this.timeZone=timeZone;
        this.oldFimware = isFirmwareLowerThan520(firmwareVersion);
    }

    /**
     * Checks if the firmware version is below 5.20.
     * Firmware versions before 5.20 have slightly different event descriptions.
     *
     * @return true if firmware version is below 5.20
     *         false if firmware version is equal or higher 5.20
     */

    private boolean isFirmwareLowerThan520(String firmwareVersion) {
        try {
            float version = Float.parseFloat(firmwareVersion);
            if (version < 5.2f) {
                return true;
            }
        } catch (NumberFormatException e) {
        }

        return false;
    }

    public List getMeterEvents(DataContainer dc) {
        int size = dc.getRoot().getNrOfElements();
        List meterEvents = new ArrayList(size);

        Date eventTimeStamp = null;
        for (int i = (size - 1); i >= 0; i--) {
            DataStructure ds = dc.getRoot().getStructure(i);
            int eventType = ds.getStructure(0).getInteger(0);
            int eventParameter = ds.getStructure(0).getInteger(1);
            int eventId = ds.getInteger(1);

            // Warning: The fields date (year high byte, year low byte, month, day of month, day of week) in UTC are set only once a day for the first event following midnight.
            // For all other elements in the same day, these fields are set to 0xFF.
            // -> We use OctetString.toDate(Date date, TimeZone timeZone) instead of OctetString.toDate(TimeZone timeZone)
            eventTimeStamp = new Date(ds.getOctetString(2).toDate(eventTimeStamp, timeZone).getTime());

            // The first events - before the date fields are send out for the first time - will have an invalid eventTimeStamp (e.g.: Jan 01 23:00:00 CET 1970) - these will be removed.
            MeterEvent meterEvent = (eventTimeStamp.getYear() != 70 ? buildMeterEvent(eventType, eventParameter, eventTimeStamp) : null);

            if (meterEvent != null) {
                meterEvents.add(meterEvent);
            }
            if (DEBUG >= 1) {
                System.out.println("KV_DEBUG> eventType=" + eventType + ", eventParameter=" + eventParameter + ", eventId=" + eventId + ", eventTimeStamp=" + eventTimeStamp);
            }
        }
        return meterEvents;
    }

    private MeterEvent buildMeterEvent(int eventType, int eventParameter, Date eventTimeStamp) {
        int eiCode=MeterEvent.OTHER;
        String message=null;

        switch(eventType) {
            case EVENT_PERIODICAL_EOI:
                return null;    // For normal end of interval events - occurring at the end of each interval period - we do not create a EiServer event
            case EVENT_ASYNCHRONOUS_EOI:
                message = "Asynchronous end of interval";
                break;
            case EVENT_PERIODICAL_EOB:
                eiCode = MeterEvent.BILLING_ACTION;
                message = "Periodical end of billing";
                break;
            case EVENT_PROGRAMMED_EOB:
                eiCode = MeterEvent.BILLING_ACTION;
                message = "Programmed end of billing";
                break;
            case EVENT_ASYNCHRONOUS_EOB:
                eiCode = MeterEvent.BILLING_ACTION;
                message = "Asynchronous end of billing";
                break;
            case EVENT_INDEX_DPM:
                message = "Index DPM";
                break;
            case EVENT_RESTORE_INTERNAL_INDEX:
                message = "Restore internal index";
                break;
            case EVENT_INDEX_CI:
                message = "Index CI";
                break;
            case EVENT_DAY_PROFILE_CL:
                message = "Day profile CL";
                break;
            case EVENT_RESTORE_INTERNAL_DAY_PROFILE:
                message = "Restore internal day profile";
                break;
            case EVENT_DAY_PROFILE_CI:
                message = "Day profile CI";
                break;
            case EVENT_SEASON_SM:
                message = "Season SM";
                break;
            case EVENT_RESTORE_INTERNAL_SEASON:
                message = "Restore internal season";
                break;
            case EVENT_SEASON_CI:
                message = "Season CI";
                break;
            case EVENT_DST_WITH_SEASON:
                message = "DST with season";
                break;
            case EVENT_EXTERNAL_SYNCHRO_AND_DST_WITH_SEASON:
                message = "External synchro and DST with season";
                break;
            case EVENT_ENTER_DOWNLOAD_MODE:
                message = "Enter download mode";
                break;
            case EVENT_EOR:
                message ="End of record";
               break;
            case EVENT_ASSOCIATION_LN_PROGRAMMING:
                message = "Association LN programming";
                eiCode = MeterEvent.CONFIGURATIONCHANGE;
                break;
            case EVENT_EXCESS_ENERGY_ABOVE_THRESHOLD_ER:
                if (oldFimware) {
                    message = "Excess energy above threshold ER";
                    eiCode = MeterEvent.METER_ALARM;
                } else {
                    message = "Data push activation";
                }
                break;
            case EVENT_EXCESS_ENERGY_UNDER_THRESHOLD_ER:
                if (oldFimware) {
                    message = "Excess energy under threshold ER";
                    eiCode = MeterEvent.METER_ALARM;
                } else {
                    message = "Save manufacturer parameters";
                    eiCode = MeterEvent.CONFIGURATIONCHANGE;
                }
                break;
            case EVENT_EXCESS_ENERGY_ABOVE_THRESHOLD_LP:
                if (oldFimware) {
                    message = "Excess energy above threshold LP";
                    eiCode = MeterEvent.METER_ALARM;
                } else {
                    message = "Reset TER";
                }
                break;
            case EVENT_EXCESS_ENERGY_UNDER_THRESHOLD_LP:
                message = "Excess energy under threshold LP";
                eiCode = MeterEvent.METER_ALARM;
                break;
            case EVENT_NON_FATAL_ALARM_APPEARANCE:
                message = "Non fatal alarm appearance";
                break;
            case EVENT_NON_FATAL_ALARM_DISAPPEARANCE:
                message = "Non fatal alarm disappearance";
                eiCode = MeterEvent.METER_ALARM;
                break;
            case EVENT_FATAL_ALARM_APPEARANCE:
                message = "Fatal alarm appearance";
                eiCode = MeterEvent.FATAL_ERROR;
                break;
            case EVENT_PARAMETERS_SAVING:
                message = "Parameters savings";
                eiCode = MeterEvent.CONFIGURATIONCHANGE;
                break;
            case EVENT_CLEAR_NON_FATAL_ALARMS:
                message = "Clear non fatal alarms";
                break;
            case EVENT_CLEAR_FATAL_ALARMS:
                message = "Clear fatal alarms";
                break;
            case EVENT_INTERNAL_CLOCK_SYNCHRO:
                message = "Internal clock synchro";
                eiCode = MeterEvent.SETCLOCK;
                break;
            case EVENT_EXTERNAL_CLOCK_SYNCHRO:
                message = "External clock synchro";
                eiCode = MeterEvent.SETCLOCK;
                break;
            case EVENT_CLOCK_SETTING:
                message = "clock setting";
                eiCode = MeterEvent.SETCLOCK;
                break;
            case EVENT_EXTERNAL_SYNCHRO_AND_DST_WITHOUT_SEASON:
                message = "External synchro and DST without season";
                break;
            case EVENT_DST_WITHOUT_SEASON:
                message = "DST without season";
                break;
            case EVENT_AC_FAIL_APPEARANCE:
                message = "AC fail appearance";
                break;
            case EVENT_AC_FAIL_DISAPPEARANCE:
                message = "AC fail disappearance";
                break;
            case EVENT_PWR_FAIL_APPEARANCE:
                message = "Power fail appearance";
                eiCode = MeterEvent.POWERDOWN;
                break;
            case EVENT_POWER_UP:
                message = "Power up";
                eiCode = MeterEvent.POWERUP;
                break;
            case EVENT_PROGRAMMING_CM:
                message = "Programming CM";
                eiCode = MeterEvent.CONFIGURATIONCHANGE;
                break;
            case EVENT_PROGRAMMING_DI:
                message = "Programming DI";
                eiCode = MeterEvent.CONFIGURATIONCHANGE;
                break;
            case EVENT_CANCEL_PROGRAMMING_DI:
                message = "Cancel programming DI";
                eiCode = MeterEvent.CONFIGURATIONCHANGE;
                break;
            case EVENT_RESET_MEASUREMENT_DATA:
                message = "Reset measurement data";
                eiCode = MeterEvent.CLEAR_DATA;
                break;
            case EVENT_START_MEASUREMENT:
                message = "Start measurement";
                break;
            case EVENT_STOP_MEASUREMENT:
                message = "Stop measurement";
                break;
            case EVENT_START_TRIGGERED_TESTS:
                message = "Start triggered tests";
                break;
            case EVENT_STOP_TRIGGERED_TESTS:
                message = "Stop triggered tests";
                break;
            case EVENT_END_OF_CURRENT_DATA_SAVING:
                message = "End of current data saving";
                break;
            case EVENT_LOAD_PROFILE_RESET:
                message = "Load profile reset";
                eiCode = MeterEvent.CLEAR_DATA;
                break;
            case EVENT_PASSWORD_RESTORATION:
                message = "Password restoration";
                break;
            case EVENT_INDEX_CLOCK_LOSS:
                message = "Index clock loss";
                break;
            case EVENT_SUCCESSFUL_COMMUNICATION:
                message = "Successfull communication";
                break;
            case EVENT_COMMUNICATION_WITH_CONTRACT:
                message = "Communication with contract";
                break;
            case EVENT_PROGRAMMING_CM2:
                message = "Programming CM2";
                eiCode = MeterEvent.CONFIGURATIONCHANGE;
                break;
            default:
                break;
        }

        if (message == null) {
            return new MeterEvent(eventTimeStamp, eiCode, eventType);
        } else {
            return new MeterEvent(eventTimeStamp, eiCode, eventType, message + " (event parameter=" + eventParameter + ")");
        }
    }
}