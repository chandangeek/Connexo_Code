package com.energyict.smartmeterprotocolimpl.eict.AM110R.common;

import com.energyict.protocol.MeterEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 22/07/11
 * Time: 9:10
 */
public class SswgEvents {

    private static final List<SswgEvent> EVENTS;
    private static final List<SswgEvent> ELSTER_FIRMWARE_EVENTS;
    private static final List<SswgEvent> MANUFACTURER_SPECIFIC_EVENTS;

    static {
        EVENTS = new ArrayList<SswgEvent>();
        ELSTER_FIRMWARE_EVENTS = new ArrayList<SswgEvent>();
        MANUFACTURER_SPECIFIC_EVENTS = new ArrayList<SswgEvent>();
        EVENTS.add(new SswgEvent(0x01E1, MeterEvent.COVER_OPENED, "Meter cover removed"));
        EVENTS.add(new SswgEvent(0x01E0, MeterEvent.METER_COVER_CLOSED, "Meter cover closed"));
        EVENTS.add(new SswgEvent(0x01E4, MeterEvent.STRONG_DC_FIELD_DETECTED, "Strong Magnetic field"));
        EVENTS.add(new SswgEvent(0x01E5, MeterEvent.NO_STRONG_DC_FIELD_ANYMORE, "No Strong Magnetic field"));
        EVENTS.add(new SswgEvent(0x01D8, MeterEvent.BATTERY_VOLTAGE_LOW, "Battery Failure"));
        EVENTS.add(new SswgEvent(0x01D9, MeterEvent.BATTERY_VOLTAGE_LOW, "Low Battery"));
        EVENTS.add(new SswgEvent(0x01EE, MeterEvent.ROM_MEMORY_ERROR, "Program Memory Error"));
        EVENTS.add(new SswgEvent(0x00F3, MeterEvent.RAM_MEMORY_ERROR, "RAM Error"));
        EVENTS.add(new SswgEvent(0x00F0, MeterEvent.NV_MEMORY_ERROR, "NV Memory Error"));
        EVENTS.add(new SswgEvent(0x01EC, MeterEvent.MEASUREMENT_SYSTEM_ERROR, "Measurement System Error"));
        EVENTS.add(new SswgEvent(0x00F6, MeterEvent.WATCHDOG_ERROR, "Watchdog Error"));
        EVENTS.add(new SswgEvent(0x0068, MeterEvent.REMOTE_DISCONNECTION, "Supply Disconnect Failure"));
        EVENTS.add(new SswgEvent(0x0067, MeterEvent.REMOTE_CONNECTION, "Supply Connect Failure"));
        EVENTS.add(new SswgEvent(0x01ED, MeterEvent.CONFIGURATIONCHANGE, "Measurement Software Changed"));
        EVENTS.add(new SswgEvent(0x0038, MeterEvent.DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED, "DST Enabled"));
        EVENTS.add(new SswgEvent(0x0037, MeterEvent.DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED, "DST Disabled"));
        EVENTS.add(new SswgEvent(0x0039, MeterEvent.CLOCK_INVALID, "Clock Invalid"));
        EVENTS.add(new SswgEvent(0x0033, MeterEvent.SETCLOCK, "Clock Adjust Backward"));
        EVENTS.add(new SswgEvent(0x0032, MeterEvent.SETCLOCK, "Clock Adjust Forward"));

        EVENTS.add(new SswgEvent(0x002A, MeterEvent.APPLICATION_ALERT_START, "Comms tamper/Comms Error HAN"));
        EVENTS.add(new SswgEvent(0x002B, MeterEvent.APPLICATION_ALERT_STOP, "Comms restored/Comms OK HAN"));
        EVENTS.add(new SswgEvent(0x01DE, MeterEvent.POWERUP, "Power Up"));
        EVENTS.add(new SswgEvent(0x01DD, MeterEvent.POWERDOWN, "Power Down"));
        EVENTS.add(new SswgEvent(0x01F2, MeterEvent.TAMPER, "Comms Using Unauthenticated Component"));
        EVENTS.add(new SswgEvent(0x0002, MeterEvent.EVENT_LOG_CLEARED, "Event Log Cleared"));
        EVENTS.add(new SswgEvent(0x005A, MeterEvent.TOU_ACTIVATED, "TOU activated"));

        EVENTS.add(new SswgEvent(0x00CE, MeterEvent.OTHER, "Excess Flow"));
        EVENTS.add(new SswgEvent(0x00CC, MeterEvent.OTHER, "Leak Detected"));
        EVENTS.add(new SswgEvent(0x00D4, MeterEvent.REVERSE_RUN, "Reverse Flow"));
        EVENTS.add(new SswgEvent(0x00D8, MeterEvent.VOLTAGE_SAG, "Under Voltage"));
        EVENTS.add(new SswgEvent(0x00D7, MeterEvent.VOLTAGE_SWELL, "Over Voltage"));
        EVENTS.add(new SswgEvent(0x01D7, MeterEvent.TERMINAL_OPENED, "Battery Cover Removed"));
        EVENTS.add(new SswgEvent(0x01D6, MeterEvent.TERMINAL_COVER_CLOSED, "Battery Cover Closed"));
        EVENTS.add(new SswgEvent(0x00D5, MeterEvent.TAMPER, "Tilt Tamper"));
        EVENTS.add(new SswgEvent(0x00D6, MeterEvent.TAMPER, "Tilt Tamper Ended"));
        EVENTS.add(new SswgEvent(0x0113, MeterEvent.OTHER, "Mirror Report Attribute Response Activated"));
        EVENTS.add(new SswgEvent(0x01CE, MeterEvent.OTHER, "Stay Awake Request HES"));

        EVENTS.add(new SswgEvent(0x01F4, MeterEvent.OTHER, "Manufacturer Specific A"));
        EVENTS.add(new SswgEvent(0x01F5, MeterEvent.OTHER, "Manufacturer Specific B"));
        EVENTS.add(new SswgEvent(0x01F6, MeterEvent.OTHER, "Manufacturer Specific C"));
        EVENTS.add(new SswgEvent(0x01F7, MeterEvent.OTHER, "Manufacturer Specific D"));
        EVENTS.add(new SswgEvent(0x01F8, MeterEvent.OTHER, "Manufacturer Specific E"));
        EVENTS.add(new SswgEvent(0x01F9, MeterEvent.OTHER, "Manufacturer Specific F"));
        EVENTS.add(new SswgEvent(0x01FA, MeterEvent.OTHER, "Manufacturer Specific G"));
        EVENTS.add(new SswgEvent(0x01FB, MeterEvent.OTHER, "Manufacturer Specific H"));

        EVENTS.add(new SswgEvent(0x0055, MeterEvent.OTHER, "Price Change received"));
        EVENTS.add(new SswgEvent(0x0054, MeterEvent.OTHER, "Price Change activated"));
        EVENTS.add(new SswgEvent(0x0053, MeterEvent.OTHER, "Tariff Change received"));
        EVENTS.add(new SswgEvent(0x0052, MeterEvent.OTHER, "Tariff Change activated"));
        EVENTS.add(new SswgEvent(0x0059, MeterEvent.OTHER, "TOU Change received"));
        EVENTS.add(new SswgEvent(0x005A, MeterEvent.OTHER, "TOU  Change activated"));

        EVENTS.add(new SswgEvent(0x016C, MeterEvent.OTHER, "PublishPrice"));
        EVENTS.add(new SswgEvent(0x0174, MeterEvent.OTHER, "PublishTariffInformation"));
        EVENTS.add(new SswgEvent(0x0160, MeterEvent.OTHER, "PublishConversionFactor"));
        EVENTS.add(new SswgEvent(0x0154, MeterEvent.OTHER, "PublishCalorificValue"));
        EVENTS.add(new SswgEvent(0x0150, MeterEvent.OTHER, "PublishCalendar"));
        EVENTS.add(new SswgEvent(0x013C, MeterEvent.OTHER, "PublishSpecialDays"));
        EVENTS.add(new SswgEvent(0x0138, MeterEvent.OTHER, "PublishSeasons"));
        EVENTS.add(new SswgEvent(0x0140, MeterEvent.OTHER, "PublishWeek"));
        EVENTS.add(new SswgEvent(0x0134, MeterEvent.OTHER, "PublishDay"));
        EVENTS.add(new SswgEvent(0x0130, MeterEvent.OTHER, "PublishChangeofTenancy"));
        EVENTS.add(new SswgEvent(0x0131, MeterEvent.OTHER, "PublishChangeofTenancy Cancelled"));
        EVENTS.add(new SswgEvent(0x0132, MeterEvent.OTHER, "PublishChangeofTenancy Received"));
        EVENTS.add(new SswgEvent(0x012C, MeterEvent.OTHER, "PublishChangeofSupplier"));
        EVENTS.add(new SswgEvent(0x012D, MeterEvent.OTHER, "PublishChangeofSupplier Cancelled"));
        EVENTS.add(new SswgEvent(0x012E, MeterEvent.OTHER, "PublishChangeofSupplier Received"));
        EVENTS.add(new SswgEvent(0x012F, MeterEvent.OTHER, "PublishChangeofSupplier Rejected"));

        EVENTS.add(new SswgEvent(0x01B0, MeterEvent.OTHER, "HAN Created"));
        EVENTS.add(new SswgEvent(0x01B2, MeterEvent.OTHER, "HAN Creation failed Due to Hardware"));
        EVENTS.add(new SswgEvent(0x01B1, MeterEvent.OTHER, "HAN Creation failed Due to Configuration"));
        EVENTS.add(new SswgEvent(0x01B3, MeterEvent.OTHER, "HAN Device Joined"));
        EVENTS.add(new SswgEvent(0x01B4, MeterEvent.OTHER, "HAN Device Joined Failed"));
        EVENTS.add(new SswgEvent(0x01BA, MeterEvent.OTHER, "HAN Mirror Created"));
        EVENTS.add(new SswgEvent(0x01BB, MeterEvent.OTHER, "HAN Mirror Created Failed"));
        EVENTS.add(new SswgEvent(0x01BC, MeterEvent.OTHER, "HAN Mirror Removed"));
        EVENTS.add(new SswgEvent(0x01B8, MeterEvent.OTHER, "HAN Device Removed HES"));
        EVENTS.add(new SswgEvent(0x01B7, MeterEvent.OTHER, "HAN Device Removed HAN"));
        EVENTS.add(new SswgEvent(0x01AE, MeterEvent.OTHER, "HAN Backup performed"));
        EVENTS.add(new SswgEvent(0x01AF, MeterEvent.OTHER, "HAN Backup Sent"));
        EVENTS.add(new SswgEvent(0x01BD, MeterEvent.OTHER, "HAN Restore"));
        EVENTS.add(new SswgEvent(0x01BE, MeterEvent.OTHER, "HAN Restore Failed"));

        EVENTS.add(new SswgEvent(0x01BF, MeterEvent.OTHER, "ZigBee Link Key Update Failure"));
        EVENTS.add(new SswgEvent(0x01C0, MeterEvent.OTHER, "ZigBee Link Key Update Request"));
        EVENTS.add(new SswgEvent(0x01C1, MeterEvent.OTHER, "ZigBee Link Key Update Request Rejected"));
        EVENTS.add(new SswgEvent(0x01C2, MeterEvent.OTHER, "ZigBee Link Key Update Successfully Updated"));
        EVENTS.add(new SswgEvent(0x01C3, MeterEvent.OTHER, "ZigBee Network Key Update Failure"));
        EVENTS.add(new SswgEvent(0x01C4, MeterEvent.OTHER, "ZigBee Network Key Update Request"));
        EVENTS.add(new SswgEvent(0x01C5, MeterEvent.OTHER, "ZigBee Network Key Update Request Rejected"));
        EVENTS.add(new SswgEvent(0x01C6, MeterEvent.OTHER, "ZigBee Network Key Update Successfully Updated"));

        EVENTS.add(new SswgEvent(0x000B, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key/Password Auth Client Data Collection Change"));
        EVENTS.add(new SswgEvent(0x000E, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key/Password Auth Client Extended Data Collection Change"));
        EVENTS.add(new SswgEvent(0x0014, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key/Password Auth Client Management Change"));
        EVENTS.add(new SswgEvent(0x0011, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key/Password Auth Client Firmware Change"));
        EVENTS.add(new SswgEvent(0x0017, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key/Password Encryption Client Data Collection Change"));
        EVENTS.add(new SswgEvent(0x001A, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key/Password Encryption Client Extended Data Collection Change"));
        EVENTS.add(new SswgEvent(0x0020, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key/Password Encryption Client Management Change"));
        EVENTS.add(new SswgEvent(0x001D, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key/Password Encryption Client Firmware Change"));

        // Manufacturer Specific Hub Events
        MANUFACTURER_SPECIFIC_EVENTS.add(new SswgEvent(0x0000, MeterEvent.OTHER, "Software watchdog"));
        MANUFACTURER_SPECIFIC_EVENTS.add(new SswgEvent(0x0001, MeterEvent.RAM_MEMORY_ERROR, "Out of Memory"));
        MANUFACTURER_SPECIFIC_EVENTS.add(new SswgEvent(0x0002, MeterEvent.RAM_MEMORY_ERROR, "Memory Leak"));
        MANUFACTURER_SPECIFIC_EVENTS.add(new SswgEvent(0x0003, MeterEvent.OTHER, "Webserver Reboot"));
        MANUFACTURER_SPECIFIC_EVENTS.add(new SswgEvent(0x0004, MeterEvent.OTHER, "DLMS Reboot"));
        MANUFACTURER_SPECIFIC_EVENTS.add(new SswgEvent(0x0005, MeterEvent.OTHER, "Self-check Failed"));
        MANUFACTURER_SPECIFIC_EVENTS.add(new SswgEvent(0x0006, MeterEvent.OTHER, "Initialization"));

        // Elster Firmware Events
        ELSTER_FIRMWARE_EVENTS.add(new SswgEvent(0x0000, MeterEvent.OTHER, "Firmware Image received from HES"));
        ELSTER_FIRMWARE_EVENTS.add(new SswgEvent(0x0001, MeterEvent.OTHER, "Firmware Image received from HHT/Local"));
        ELSTER_FIRMWARE_EVENTS.add(new SswgEvent(0x0002, MeterEvent.OTHER, "OTA Store Full"));
        ELSTER_FIRMWARE_EVENTS.add(new SswgEvent(0x0003, MeterEvent.OTHER, "OTA Store Overwritten"));
        ELSTER_FIRMWARE_EVENTS.add(new SswgEvent(0x0004, MeterEvent.OTHER, "OTA Store Corrupt"));
        ELSTER_FIRMWARE_EVENTS.add(new SswgEvent(0x0005, MeterEvent.OTHER, "OTA Firmware File Corrupt"));
        ELSTER_FIRMWARE_EVENTS.add(new SswgEvent(0x0006, MeterEvent.OTHER, "OTA Firmware File device Notified"));
        ELSTER_FIRMWARE_EVENTS.add(new SswgEvent(0x0007, MeterEvent.OTHER, "OTA File Sent to device"));
        ELSTER_FIRMWARE_EVENTS.add(new SswgEvent(0x0008, MeterEvent.OTHER, "OTA File Start sending"));
        ELSTER_FIRMWARE_EVENTS.add(new SswgEvent(0x0009, MeterEvent.FIRMWARE_READY_FOR_ACTIVATION, "OTA File Verified"));
        ELSTER_FIRMWARE_EVENTS.add(new SswgEvent(0x000A, MeterEvent.OTHER, "OTA File Verification failed"));
        ELSTER_FIRMWARE_EVENTS.add(new SswgEvent(0x000B, MeterEvent.FIRMWARE_ACTIVATED, "OTA File Activated on device"));
        ELSTER_FIRMWARE_EVENTS.add(new SswgEvent(0x000C, MeterEvent.OTHER, "OTA File failed to activate on device"));
        ELSTER_FIRMWARE_EVENTS.add(new SswgEvent(0x000D, MeterEvent.OTHER, "OTA File Firmware Activation Date Set On DLMS"));
    }

    private SswgEvents() {
        // SswgEvents object should not be instantiated
    }

    public static class SswgEvent {

        private final int deviceCode;
        private final int eiserverCode;
        private final String description;

        public SswgEvent(int deviceCode, int eiserverCode, String description) {
            this.deviceCode = deviceCode;
            this.eiserverCode = eiserverCode;
            this.description = description;
        }

        public int getDeviceCode() {
            return deviceCode;
        }

        public int getEiserverCode() {
            return eiserverCode;
        }

        public String getDescription() {
            return description;
        }

        public MeterEvent toMeterEvent(Date eventDate, final int logbookId, final int eventNumber) {
            return new MeterEvent(eventDate, getEiserverCode(), getDeviceCode(), getDescription(), logbookId, eventNumber);
        }

        public MeterEvent toMeterEvent(Date eventDate, final int logbookId, final int eventNumber, final String macAddress) {
            String description = getDescription() + " (" + macAddress + ")";
            return new MeterEvent(eventDate, getEiserverCode(), getDeviceCode(), description, logbookId, eventNumber);
        }
    }

    /**
     * Find device code in EVENT list. If the given device code does not exists,
     * create a new SswgEvent with this device code and MeterEvent.OTHER as EiServer code
     *
     * @param deviceCode The device code to find
     * @return a new SswgEvent that contains all the information about this device code
     */
    public static SswgEvent getSswgEventFromDeviceCode(int deviceCode) {
        for (SswgEvent event : EVENTS) {
            if (event.getDeviceCode() == deviceCode) {
                return event;
            }
        }
        return new SswgEvent(deviceCode, MeterEvent.OTHER, "Unknown event code [" + deviceCode + "]");
    }

    /**
     * Find device code in ElSTER_FIRMWARE_EVENTS list. If the given device code does not exists,
     * create a new SswgEvent with this device code and MeterEvent.OTHER as EiServer code
     *
     * @param deviceCode The device code to find in the Elster Firmware Event log
     * @return a new SswgEvent that contains all the information about this device code
     */
    public static SswgEvent getSswgElsterFirmwareEventFromDeviceCode(int deviceCode) {
        for (SswgEvent event : ELSTER_FIRMWARE_EVENTS) {
            if (event.getDeviceCode() == deviceCode) {
                return event;
            }
        }
        return new SswgEvent(deviceCode, MeterEvent.OTHER, "Unknown event code [" + deviceCode + "]");
    }


    /**
     * Find device code in MANUFACTURER_SPECIFIC_EVENT list. If the given device code does not exists,
     * create a new SswgEvent with this device code and MeterEvent.OTHER as EiServer code
     *
     * @param deviceCode The device code to find in the Manufacturer Specific Event log
     * @return a new SswgEvent that contains all the information about this device code
     */
    public static SswgEvent getSswgManufacturerSpecificEventFromDeviceCode(int deviceCode) {
        for (SswgEvent event : MANUFACTURER_SPECIFIC_EVENTS) {
            if (event.getDeviceCode() == deviceCode) {
                return event;
            }
        }
        return new SswgEvent(deviceCode, MeterEvent.OTHER, "Unknown event code [" + deviceCode + "]");
    }
}