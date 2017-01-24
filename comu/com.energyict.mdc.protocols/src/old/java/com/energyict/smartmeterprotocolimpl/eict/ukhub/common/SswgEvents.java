package com.energyict.smartmeterprotocolimpl.eict.ukhub.common;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

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

    static {
        EVENTS = new ArrayList<SswgEvent>();
        EVENTS.add(new SswgEvent(0x0000, MeterEvent.COVER_OPENED, "Meter cover removed"));
        EVENTS.add(new SswgEvent(0x0001, MeterEvent.METER_COVER_CLOSED, "Meter cover closed"));
        EVENTS.add(new SswgEvent(0x0002, MeterEvent.STRONG_DC_FIELD_DETECTED, "Strong Magnetic field"));
        EVENTS.add(new SswgEvent(0x0003, MeterEvent.NO_STRONG_DC_FIELD_ANYMORE, "No Strong Magnetic field"));
        EVENTS.add(new SswgEvent(0x0004, MeterEvent.BATTERY_VOLTAGE_LOW, "Battery Failure"));
        EVENTS.add(new SswgEvent(0x0005, MeterEvent.BATTERY_VOLTAGE_LOW, "Low Battery"));
        EVENTS.add(new SswgEvent(0x0006, MeterEvent.ROM_MEMORY_ERROR, "Program Memory Error"));
        EVENTS.add(new SswgEvent(0x0007, MeterEvent.RAM_MEMORY_ERROR, "RAM Error"));
        EVENTS.add(new SswgEvent(0x0008, MeterEvent.NV_MEMORY_ERROR, "NV Memory Error"));
        EVENTS.add(new SswgEvent(0x0009, MeterEvent.MEASUREMENT_SYSTEM_ERROR, "Measurement System Error"));
        EVENTS.add(new SswgEvent(0x000A, MeterEvent.WATCHDOG_ERROR, "Watchdog Error"));
        EVENTS.add(new SswgEvent(0x000B, MeterEvent.REMOTE_DISCONNECTION, "Supply Disconnect Failure"));
        EVENTS.add(new SswgEvent(0x000C, MeterEvent.REMOTE_CONNECTION, "Supply Connect Failure"));
        EVENTS.add(new SswgEvent(0x000D, MeterEvent.CONFIGURATIONCHANGE, "Measurement Software Changed"));
        EVENTS.add(new SswgEvent(0x000E, MeterEvent.DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED, "DST Enabled"));
        EVENTS.add(new SswgEvent(0x000F, MeterEvent.DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED, "DST Disabled"));
        EVENTS.add(new SswgEvent(0x0010, MeterEvent.SETCLOCK, "Clock Adjust Backward"));
        EVENTS.add(new SswgEvent(0x0011, MeterEvent.SETCLOCK, "Clock Adjust Forward"));
        EVENTS.add(new SswgEvent(0x0012, MeterEvent.CLOCK_INVALID, "Clock Invalid"));
        EVENTS.add(new SswgEvent(0x0013, MeterEvent.APPLICATION_ALERT_START, "Comms Error HAN"));
        EVENTS.add(new SswgEvent(0x0014, MeterEvent.APPLICATION_ALERT_STOP, "Comms OK HAN"));
        EVENTS.add(new SswgEvent(0x0015, MeterEvent.TAMPER, "Fraud Attempt"));
        EVENTS.add(new SswgEvent(0x0016, MeterEvent.POWERDOWN, "Power Loss"));
        EVENTS.add(new SswgEvent(0x0017, MeterEvent.OTHER, "Incorrect Protocol"));
        EVENTS.add(new SswgEvent(0x0018, MeterEvent.OTHER, "Unusal HAN Traffic"));
        EVENTS.add(new SswgEvent(0x0019, MeterEvent.SETCLOCK, "Unexpected Clock Change"));
        EVENTS.add(new SswgEvent(0x001A, MeterEvent.TAMPER, "Comms Using Unauthenticated Component"));
        EVENTS.add(new SswgEvent(0x001B, MeterEvent.CLEAR_DATA, "Error Reg Clear"));
        EVENTS.add(new SswgEvent(0x001C, MeterEvent.ALARM_REGISTER_CLEARED, "Alarm Reg Clear"));
        EVENTS.add(new SswgEvent(0x001D, MeterEvent.HARDWARE_ERROR, "UnexpectedHWReset"));
        EVENTS.add(new SswgEvent(0x001E, MeterEvent.PROGRAM_FLOW_ERROR, "UnexpectedProgramExecution"));
        EVENTS.add(new SswgEvent(0x001F, MeterEvent.EVENT_LOG_CLEARED, "Event Log Cleared"));
        EVENTS.add(new SswgEvent(0x0020, MeterEvent.MANUAL_DISCONNECTION, "Manual Disconnect"));
        EVENTS.add(new SswgEvent(0x0021, MeterEvent.MANUAL_CONNECTION, "Manual Connect"));
        EVENTS.add(new SswgEvent(0x0022, MeterEvent.REMOTE_DISCONNECTION, "Remote Disconnection"));
        EVENTS.add(new SswgEvent(0x0023, MeterEvent.LOCAL_DISCONNECTION, "Local Disconnection"));
        EVENTS.add(new SswgEvent(0x0024, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limit Threshold Exceeded"));
        EVENTS.add(new SswgEvent(0x0025, MeterEvent.LIMITER_THRESHOLD_OK, "Limit Threshold OK"));
        EVENTS.add(new SswgEvent(0x0026, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limit Threshold Changed"));
        EVENTS.add(new SswgEvent(0x0027, MeterEvent.OTHER, "Maximum Demand Exceeded"));
        EVENTS.add(new SswgEvent(0x0028, MeterEvent.LOADPROFILE_CLEARED, "Profile Cleared"));
        EVENTS.add(new SswgEvent(0x0029, MeterEvent.FIRMWARE_READY_FOR_ACTIVATION, "Firmware Ready For Activation"));
        EVENTS.add(new SswgEvent(0x002A, MeterEvent.FIRMWARE_ACTIVATED, "Firmware Activated"));
        EVENTS.add(new SswgEvent(0x002B, MeterEvent.OTHER, "PatchFailure"));
        EVENTS.add(new SswgEvent(0x002C, MeterEvent.TOU_ACTIVATED, "TOU Tariff Activation"));
        EVENTS.add(new SswgEvent(0x002D, MeterEvent.TOU_ACTIVATED, "8 x 8 Tariff Activated"));
        EVENTS.add(new SswgEvent(0x002E, MeterEvent.TOU_ACTIVATED, "Single Rate Tariff Activated"));
        EVENTS.add(new SswgEvent(0x002F, MeterEvent.BILLING_ACTION, "AsynchronousBillingOccurred"));
        EVENTS.add(new SswgEvent(0x0030, MeterEvent.BILLING_ACTION, "SynchronousBillingOccurred"));
        EVENTS.add(new SswgEvent(0x0031, MeterEvent.STRONG_DC_FIELD_DETECTED, "Strong DC field detected"));
        EVENTS.add(new SswgEvent(0x0032, MeterEvent.NO_STRONG_DC_FIELD_ANYMORE, "No strong DC field anymore [restored]"));
        EVENTS.add(new SswgEvent(0x0033, MeterEvent.N_TIMES_WRONG_PASSWORD, "n' times wrong password"));
        EVENTS.add(new SswgEvent(0x0034, MeterEvent.POWERUP, "Power Up"));
        EVENTS.add(new SswgEvent(0x0035, MeterEvent.OTHER, "Valve alarm"));
        EVENTS.add(new SswgEvent(0x0036, MeterEvent.APPLICATION_ALERT_START, "Communication error when reading the meter"));
        EVENTS.add(new SswgEvent(0x0037, MeterEvent.APPLICATION_ALERT_STOP, "Communication ok [restored]"));
        EVENTS.add(new SswgEvent(0x0038, MeterEvent.APPLICATION_ALERT_START, "Communications error remote"));
        EVENTS.add(new SswgEvent(0x0039, MeterEvent.APPLICATION_ALERT_STOP, "Communications ok Remote Com [restored]"));
        EVENTS.add(new SswgEvent(0x003A, MeterEvent.REMOTE_DISCONNECTION, "Remote Arm Disconnector"));
        EVENTS.add(new SswgEvent(0x003B, MeterEvent.REMOTE_DISCONNECTION, "Scheduled Disconnection"));
        EVENTS.add(new SswgEvent(0x003C, MeterEvent.LOCAL_DISCONNECTION, "Local Arm Disconnector"));

        EVENTS.add(new SswgEvent(0x0080, MeterEvent.REVERSE_RUN, "Incorrect Polarity / Rervers Run"));
        EVENTS.add(new SswgEvent(0x0081, MeterEvent.OTHER, "Current No Voltage"));
        EVENTS.add(new SswgEvent(0x0082, MeterEvent.VOLTAGE_SAG, "Under Voltage"));
        EVENTS.add(new SswgEvent(0x0083, MeterEvent.VOLTAGE_SWELL, "Over Voltage"));
        EVENTS.add(new SswgEvent(0x0084, MeterEvent.OTHER, "Normal Voltage"));
        EVENTS.add(new SswgEvent(0x0085, MeterEvent.OTHER, "PF Below Threshold"));
        EVENTS.add(new SswgEvent(0x0086, MeterEvent.OTHER, "PF Above Threshold"));
        EVENTS.add(new SswgEvent(0x0087, MeterEvent.VOLTAGE_SAG, "Under Voltage 2"));
        EVENTS.add(new SswgEvent(0x0088, MeterEvent.VOLTAGE_SWELL, "Over Voltage 2"));
        EVENTS.add(new SswgEvent(0x0089, MeterEvent.OTHER, "Over Current"));
        EVENTS.add(new SswgEvent(0x008A, MeterEvent.OTHER, "Over Frequemcey"));
        EVENTS.add(new SswgEvent(0x008B, MeterEvent.OTHER, "Under Frequencey"));
        EVENTS.add(new SswgEvent(0x008C, MeterEvent.OTHER, "Under Power Factor"));

        EVENTS.add(new SswgEvent(0x0093, MeterEvent.OTHER, "Relay enable #1"));
        EVENTS.add(new SswgEvent(0x0094, MeterEvent.OTHER, "Relay enable #2"));
        EVENTS.add(new SswgEvent(0x0095, MeterEvent.OTHER, "Relay disabled #1"));
        EVENTS.add(new SswgEvent(0x0096, MeterEvent.OTHER, "Relay disabled #2"));
        EVENTS.add(new SswgEvent(0x0097, MeterEvent.CONFIGURATIONCHANGE, "Password Change #1"));
        EVENTS.add(new SswgEvent(0x0098, MeterEvent.CONFIGURATIONCHANGE, "Password Change #2"));
        EVENTS.add(new SswgEvent(0x0099, MeterEvent.CONFIGURATIONCHANGE, "Password Change #3"));
        EVENTS.add(new SswgEvent(0x009A, MeterEvent.CONFIGURATIONCHANGE, "Password Change #4"));
        EVENTS.add(new SswgEvent(0x009B, MeterEvent.TERMINAL_OPENED, "Terminal Cover Removed"));
        EVENTS.add(new SswgEvent(0x009C, MeterEvent.TERMINAL_COVER_CLOSED, "Terminal Cover Closed"));
        EVENTS.add(new SswgEvent(0x009D, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter Enabled #1"));
        EVENTS.add(new SswgEvent(0x009E, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter Enabled #2"));
        EVENTS.add(new SswgEvent(0x009F, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter Enabled #3"));
        EVENTS.add(new SswgEvent(0x00A0, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter Enabled #4"));
        EVENTS.add(new SswgEvent(0x00A1, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter Enabled #5"));
        EVENTS.add(new SswgEvent(0x00A2, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter Enabled #6"));
        EVENTS.add(new SswgEvent(0x00A3, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter Over Threshold #1"));
        EVENTS.add(new SswgEvent(0x00A4, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter Over Threshold #2"));
        EVENTS.add(new SswgEvent(0x00A5, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter Over Threshold #3"));
        EVENTS.add(new SswgEvent(0x00A6, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter Over Threshold #4"));
        EVENTS.add(new SswgEvent(0x00A7, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter Over Threshold #5"));
        EVENTS.add(new SswgEvent(0x00A8, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter Over Threshold #6"));
        EVENTS.add(new SswgEvent(0x00A9, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter Under Threshold #1"));
        EVENTS.add(new SswgEvent(0x00AA, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter Under Threshold #2"));
        EVENTS.add(new SswgEvent(0x00AB, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter Under Threshold #3"));
        EVENTS.add(new SswgEvent(0x00AC, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter Under Threshold #4"));
        EVENTS.add(new SswgEvent(0x00AD, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter Under Threshold #5"));
        EVENTS.add(new SswgEvent(0x00AE, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Limiter Under Threshold #6"));
        EVENTS.add(new SswgEvent(0x00AF, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter Disabled #1"));
        EVENTS.add(new SswgEvent(0x00B0, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter Disabled #2"));
        EVENTS.add(new SswgEvent(0x00B1, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter Disabled #3"));
        EVENTS.add(new SswgEvent(0x00B2, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter Disabled #4"));
        EVENTS.add(new SswgEvent(0x00B3, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter Disabled #5"));
        EVENTS.add(new SswgEvent(0x00B4, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter Disabled #6"));
        EVENTS.add(new SswgEvent(0x00B5, MeterEvent.CONFIGURATIONCHANGE, "Super Vision Monitor Enabled"));
        EVENTS.add(new SswgEvent(0x00B6, MeterEvent.CONFIGURATIONCHANGE, "Super Vision Monitor Over Threshold"));
        EVENTS.add(new SswgEvent(0x00B7, MeterEvent.CONFIGURATIONCHANGE, "Super Vision Monitor under Threshold"));
        EVENTS.add(new SswgEvent(0x00B8, MeterEvent.CONFIGURATIONCHANGE, "Super Vision Monitor Disabled"));
        EVENTS.add(new SswgEvent(0x00B9, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter Threshold Change #1"));
        EVENTS.add(new SswgEvent(0x00BA, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter Threshold Change #2"));
        EVENTS.add(new SswgEvent(0x00BB, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter Threshold Change #3"));
        EVENTS.add(new SswgEvent(0x00BC, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter Threshold Change #4"));
        EVENTS.add(new SswgEvent(0x00BD, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter Threshold Change #5"));
        EVENTS.add(new SswgEvent(0x00BE, MeterEvent.LIMITER_THRESHOLD_CHANGED, "Limiter Threshold Change #6"));

        EVENTS.add(new SswgEvent(0x00C0, MeterEvent.OTHER, "Credit OK"));
        EVENTS.add(new SswgEvent(0x00C1, MeterEvent.OTHER, "Low Credit"));
        EVENTS.add(new SswgEvent(0x00C2, MeterEvent.OTHER, "Emergency Credit In Use"));
        EVENTS.add(new SswgEvent(0x00C3, MeterEvent.OTHER, "Emergency Credit Exhausted"));
        EVENTS.add(new SswgEvent(0x00C4, MeterEvent.OTHER, "Zero Credit EC Not Selected"));
        EVENTS.add(new SswgEvent(0x00C5, MeterEvent.OTHER, "Mode Change to Credit"));
        EVENTS.add(new SswgEvent(0x00C6, MeterEvent.OTHER, "Mode Change to Prepayment"));

        EVENTS.add(new SswgEvent(0x00C8, MeterEvent.OTHER, "Discount Applied"));

        EVENTS.add(new SswgEvent(0x00E0, MeterEvent.OTHER, "Manufacturer Specific A"));
        EVENTS.add(new SswgEvent(0x00E1, MeterEvent.OTHER, "Manufacturer Specific B"));
        EVENTS.add(new SswgEvent(0x00E2, MeterEvent.OTHER, "Manufacturer Specific C"));
        EVENTS.add(new SswgEvent(0x00E3, MeterEvent.OTHER, "Manufacturer Specific D"));
        EVENTS.add(new SswgEvent(0x00E4, MeterEvent.OTHER, "Manufacturer Specific E"));
        EVENTS.add(new SswgEvent(0x00E5, MeterEvent.OTHER, "Manufacturer Specific F"));
        EVENTS.add(new SswgEvent(0x00E6, MeterEvent.OTHER, "Manufacturer Specific G"));
        EVENTS.add(new SswgEvent(0x00E7, MeterEvent.OTHER, "Manufacturer Specific H"));
        EVENTS.add(new SswgEvent(0x00E8, MeterEvent.OTHER, "Manufacturer Specific I"));

        EVENTS.add(new SswgEvent(0x0100, MeterEvent.OTHER, "Price Change received"));
        EVENTS.add(new SswgEvent(0x0101, MeterEvent.OTHER, "Price Change send to Mirror"));
        EVENTS.add(new SswgEvent(0x0102, MeterEvent.OTHER, "Price Change activated on meter"));
        EVENTS.add(new SswgEvent(0x0103, MeterEvent.OTHER, "Aysn Billing Price Change"));
        EVENTS.add(new SswgEvent(0x0104, MeterEvent.OTHER, "Block Threshold Change received"));
        EVENTS.add(new SswgEvent(0x0105, MeterEvent.OTHER, "Block Threshold Change send to Mirror"));
        EVENTS.add(new SswgEvent(0x0106, MeterEvent.OTHER, "Block Threshold Change activated on meter"));
        EVENTS.add(new SswgEvent(0x0107, MeterEvent.OTHER, "Aysn Billing block threshold change"));
        EVENTS.add(new SswgEvent(0x0108, MeterEvent.OTHER, "Currancy Change received"));
        EVENTS.add(new SswgEvent(0x0109, MeterEvent.OTHER, "Currancy Change send to Mirror"));
        EVENTS.add(new SswgEvent(0x010A, MeterEvent.OTHER, "Currancy Change activated on meter"));
        EVENTS.add(new SswgEvent(0x010B, MeterEvent.OTHER, "Aysn Billing currancy change"));
        EVENTS.add(new SswgEvent(0x010C, MeterEvent.OTHER, "Tariff Change received"));
        EVENTS.add(new SswgEvent(0x010D, MeterEvent.OTHER, "Tariff Change send to Mirror"));
        EVENTS.add(new SswgEvent(0x010E, MeterEvent.OTHER, "Tariff Change activated on meter"));
        EVENTS.add(new SswgEvent(0x010F, MeterEvent.OTHER, "Aysn Billing traiff change"));
        EVENTS.add(new SswgEvent(0x0110, MeterEvent.OTHER, "TOU Change received"));
        EVENTS.add(new SswgEvent(0x0111, MeterEvent.OTHER, "TOU  Change send to Mirror"));
        EVENTS.add(new SswgEvent(0x0112, MeterEvent.OTHER, "TOU  Change activated on meter"));
        EVENTS.add(new SswgEvent(0x0113, MeterEvent.OTHER, "Aysn Billing TOU change"));
        EVENTS.add(new SswgEvent(0x0114, MeterEvent.OTHER, "Block Period Change received"));
        EVENTS.add(new SswgEvent(0x0115, MeterEvent.OTHER, "Block Period   Change send to Mirror"));
        EVENTS.add(new SswgEvent(0x0116, MeterEvent.OTHER, "Block Period   Change activated on meter"));
        EVENTS.add(new SswgEvent(0x0117, MeterEvent.OTHER, "Aysn Billing Block Period  change"));

        EVENTS.add(new SswgEvent(0x011B, MeterEvent.OTHER, "HAN Created"));
        EVENTS.add(new SswgEvent(0x011C, MeterEvent.OTHER, "HAN Creation Fault Hardware"));
        EVENTS.add(new SswgEvent(0x011D, MeterEvent.OTHER, "HAN Device Joined"));
        EVENTS.add(new SswgEvent(0x011E, MeterEvent.OTHER, "HAN Device Joined Failed"));
        EVENTS.add(new SswgEvent(0x011F, MeterEvent.OTHER, "HAN Device Service Discovery Sussesful"));
        EVENTS.add(new SswgEvent(0x0120, MeterEvent.OTHER, "HAN Device Service Discovery Failed"));
        EVENTS.add(new SswgEvent(0x0121, MeterEvent.OTHER, "HAN Mirror Created "));
        EVENTS.add(new SswgEvent(0x0122, MeterEvent.OTHER, "HAN Mirror Created Failed"));
        EVENTS.add(new SswgEvent(0x0123, MeterEvent.OTHER, "HAN Mirror Removed "));
        EVENTS.add(new SswgEvent(0x0124, MeterEvent.OTHER, "HAN Device Removed HES"));
        EVENTS.add(new SswgEvent(0x0125, MeterEvent.OTHER, "HAN Device Removed HAN"));
        EVENTS.add(new SswgEvent(0x0126, MeterEvent.OTHER, "HAN Backup performed"));
        EVENTS.add(new SswgEvent(0x0127, MeterEvent.OTHER, "HAN Backup Read"));
        EVENTS.add(new SswgEvent(0x0128, MeterEvent.OTHER, "HAN Restore"));
        EVENTS.add(new SswgEvent(0x0129, MeterEvent.OTHER, "HAN Restore Failed"));
        EVENTS.add(new SswgEvent(0x012A, MeterEvent.OTHER, "HAN Identify Request"));
        EVENTS.add(new SswgEvent(0x012B, MeterEvent.OTHER, "HAN Network Key Update"));
        EVENTS.add(new SswgEvent(0x012C, MeterEvent.OTHER, "HAN Network Key Update Fail"));
        EVENTS.add(new SswgEvent(0x012D, MeterEvent.OTHER, "HAN Link Key Update"));
        EVENTS.add(new SswgEvent(0x012E, MeterEvent.OTHER, "HAN Link Key Update Fail"));

        EVENTS.add(new SswgEvent(0x0130, MeterEvent.BILLING_ACTION, "Aysn Billing CoT"));
        EVENTS.add(new SswgEvent(0x0131, MeterEvent.OTHER, "CoT New"));
        EVENTS.add(new SswgEvent(0x0132, MeterEvent.OTHER, "reserved CoT"));
        EVENTS.add(new SswgEvent(0x0133, MeterEvent.OTHER, "reserved CoT"));
        EVENTS.add(new SswgEvent(0x0134, MeterEvent.OTHER, "reserved CoT"));
        EVENTS.add(new SswgEvent(0x0135, MeterEvent.OTHER, "reserved CoT"));
        EVENTS.add(new SswgEvent(0x0136, MeterEvent.BILLING_ACTION, "Aysn Billing CoS"));
        EVENTS.add(new SswgEvent(0x0137, MeterEvent.OTHER, "CoS New"));
        EVENTS.add(new SswgEvent(0x0138, MeterEvent.OTHER, "reserved CoS"));
        EVENTS.add(new SswgEvent(0x0139, MeterEvent.OTHER, "reserved CoS"));
        EVENTS.add(new SswgEvent(0x013A, MeterEvent.OTHER, "reserved CoS"));
        EVENTS.add(new SswgEvent(0x013B, MeterEvent.OTHER, "reserved CoS"));
        EVENTS.add(new SswgEvent(0x013C, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key/Password Auth Client Public Change"));
        EVENTS.add(new SswgEvent(0x013D, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key/Password Auth Client Data Collection Change"));
        EVENTS.add(new SswgEvent(0x013E, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key/Password Auth Client Extended Data Collection Change"));
        EVENTS.add(new SswgEvent(0x013F, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key/Password Auth Client Managment Change"));
        EVENTS.add(new SswgEvent(0x0140, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key/Password Auth Client Firmware Change"));

        EVENTS.add(new SswgEvent(0x0143, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key/Password Encryption Client Public Change"));
        EVENTS.add(new SswgEvent(0x0144, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key/Password Encryption Client Data Collection Change"));
        EVENTS.add(new SswgEvent(0x0145, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key/Password Encryption Client Extended Data Collection Change"));
        EVENTS.add(new SswgEvent(0x0146, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key/Password Encryption Client Managment Change"));
        EVENTS.add(new SswgEvent(0x0147, MeterEvent.CONFIGURATIONCHANGE, "DLMS Key/Password Encryption Client Firmware Change"));

        EVENTS.add(new SswgEvent(0x017E, MeterEvent.OTHER, "MirrorReportAttributeResponse"));
        EVENTS.add(new SswgEvent(0x017F, MeterEvent.OTHER, "ChangeReportingProfile"));
        EVENTS.add(new SswgEvent(0x0180, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));
        EVENTS.add(new SswgEvent(0x0181, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));
        EVENTS.add(new SswgEvent(0x0182, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));
        EVENTS.add(new SswgEvent(0x0183, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));
        EVENTS.add(new SswgEvent(0x0184, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));
        EVENTS.add(new SswgEvent(0x0185, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));
        EVENTS.add(new SswgEvent(0x0186, MeterEvent.FIRMWARE_ACTIVATED, "New OTA Firmware "));
        EVENTS.add(new SswgEvent(0x0187, MeterEvent.OTHER, "CBKE Update Request"));
        EVENTS.add(new SswgEvent(0x0188, MeterEvent.SETCLOCK, "Time Sync"));
        EVENTS.add(new SswgEvent(0x0189, MeterEvent.CONFIGURATIONCHANGE, "New Password"));
        EVENTS.add(new SswgEvent(0x018A, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));
        EVENTS.add(new SswgEvent(0x018B, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));
        EVENTS.add(new SswgEvent(0x018C, MeterEvent.OTHER, "Stay Awake Request HAN"));
        EVENTS.add(new SswgEvent(0x018D, MeterEvent.OTHER, "Stay Awake Request HES"));
        EVENTS.add(new SswgEvent(0x018E, MeterEvent.OTHER, "PublishPrice"));
        EVENTS.add(new SswgEvent(0x018F, MeterEvent.OTHER, "PublishBlockPeriod"));
        EVENTS.add(new SswgEvent(0x0190, MeterEvent.OTHER, "PublishTariffInformation"));
        EVENTS.add(new SswgEvent(0x0191, MeterEvent.OTHER, "PublishConversionFactor"));
        EVENTS.add(new SswgEvent(0x0192, MeterEvent.OTHER, "PublishCalorificValue"));
        EVENTS.add(new SswgEvent(0x0193, MeterEvent.OTHER, "PublishCO2Value"));
        EVENTS.add(new SswgEvent(0x0194, MeterEvent.OTHER, "PublishBillingPeriod"));
        EVENTS.add(new SswgEvent(0x0195, MeterEvent.OTHER, "PublishConsolidatedBill"));
        EVENTS.add(new SswgEvent(0x0196, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));
        EVENTS.add(new SswgEvent(0x0197, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));
        EVENTS.add(new SswgEvent(0x0198, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));
        EVENTS.add(new SswgEvent(0x0199, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));
        EVENTS.add(new SswgEvent(0x019A, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));
        EVENTS.add(new SswgEvent(0x019B, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));
        EVENTS.add(new SswgEvent(0x019C, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));
        EVENTS.add(new SswgEvent(0x019D, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));
        EVENTS.add(new SswgEvent(0x019E, MeterEvent.OTHER, "PublishCalendar"));
        EVENTS.add(new SswgEvent(0x019F, MeterEvent.OTHER, "PublishSpecialDays"));
        EVENTS.add(new SswgEvent(0x01A0, MeterEvent.OTHER, "PublishSeasons"));
        EVENTS.add(new SswgEvent(0x01A1, MeterEvent.OTHER, "PublishWeek"));
        EVENTS.add(new SswgEvent(0x01A2, MeterEvent.OTHER, "PublishDay"));
        EVENTS.add(new SswgEvent(0x01A3, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));
        EVENTS.add(new SswgEvent(0x01A4, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));
        EVENTS.add(new SswgEvent(0x01A5, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));
        EVENTS.add(new SswgEvent(0x01A6, MeterEvent.OTHER, "Select Available Emergency Credit"));
        EVENTS.add(new SswgEvent(0x01A7, MeterEvent.OTHER, "Change Debt"));
        EVENTS.add(new SswgEvent(0x01A8, MeterEvent.OTHER, "Emergency Credit Setup"));
        EVENTS.add(new SswgEvent(0x01A9, MeterEvent.OTHER, "Consumer Top Up"));
        EVENTS.add(new SswgEvent(0x01AA, MeterEvent.OTHER, "Credit Adjustment"));
        EVENTS.add(new SswgEvent(0x01AB, MeterEvent.OTHER, "Change Payment Mode"));
        EVENTS.add(new SswgEvent(0x01AC, MeterEvent.OTHER, "Get Prepay Snapshot"));
        EVENTS.add(new SswgEvent(0x01AD, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));
        EVENTS.add(new SswgEvent(0x01AE, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));
        EVENTS.add(new SswgEvent(0x01AF, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));
        EVENTS.add(new SswgEvent(0x01B0, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));
        EVENTS.add(new SswgEvent(0x01B1, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));
        EVENTS.add(new SswgEvent(0x01B2, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));
        EVENTS.add(new SswgEvent(0x01B3, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));
        EVENTS.add(new SswgEvent(0x01B4, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));
        EVENTS.add(new SswgEvent(0x01B5, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));
        EVENTS.add(new SswgEvent(0x01B6, MeterEvent.OTHER, "PublishChangeofTenancy"));
        EVENTS.add(new SswgEvent(0x01B7, MeterEvent.OTHER, "PublishChangeofSupplier"));
        EVENTS.add(new SswgEvent(0x01B8, MeterEvent.OTHER, "ChangeSupply"));
        EVENTS.add(new SswgEvent(0x01B9, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));
        EVENTS.add(new SswgEvent(0x01BA, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));
        EVENTS.add(new SswgEvent(0x01BB, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));
        EVENTS.add(new SswgEvent(0x01BC, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));
        EVENTS.add(new SswgEvent(0x01BD, MeterEvent.OTHER, "Notification Flag Reserved DON'T USE"));

        EVENTS.add(new SswgEvent(0x01C0, MeterEvent.OTHER, "Manufacturer Specific 63"));
        EVENTS.add(new SswgEvent(0x01C1, MeterEvent.OTHER, "Manufacturer Specific 62"));
        EVENTS.add(new SswgEvent(0x01C2, MeterEvent.OTHER, "Manufacturer Specific 61"));
        EVENTS.add(new SswgEvent(0x01C3, MeterEvent.OTHER, "Manufacturer Specific 60"));
        EVENTS.add(new SswgEvent(0x01C4, MeterEvent.OTHER, "Manufacturer Specific 59"));
        EVENTS.add(new SswgEvent(0x01C5, MeterEvent.OTHER, "Manufacturer Specific 58"));
        EVENTS.add(new SswgEvent(0x01C6, MeterEvent.OTHER, "Manufacturer Specific 57"));
        EVENTS.add(new SswgEvent(0x01C7, MeterEvent.OTHER, "Manufacturer Specific 56"));
        EVENTS.add(new SswgEvent(0x01C8, MeterEvent.OTHER, "Manufacturer Specific 55"));
        EVENTS.add(new SswgEvent(0x01C9, MeterEvent.OTHER, "Manufacturer Specific 54"));
        EVENTS.add(new SswgEvent(0x01CA, MeterEvent.OTHER, "Manufacturer Specific 53"));
        EVENTS.add(new SswgEvent(0x01CB, MeterEvent.OTHER, "Manufacturer Specific 52"));
        EVENTS.add(new SswgEvent(0x01CC, MeterEvent.OTHER, "Manufacturer Specific 51 - Comms hub Power down "));
        EVENTS.add(new SswgEvent(0x01CD, MeterEvent.OTHER, "Manufacturer Specific 50 - Comms hub Power up"));
        EVENTS.add(new SswgEvent(0x01CE, MeterEvent.OTHER, "Manufacturer Specific 49"));
        EVENTS.add(new SswgEvent(0x01CF, MeterEvent.OTHER, "Manufacturer Specific 48 - Configuration change in comms hub"));
        EVENTS.add(new SswgEvent(0x01D0, MeterEvent.OTHER, "Manufacturer Specific 47 - Comms Hub received NCP image"));
        EVENTS.add(new SswgEvent(0x01D1, MeterEvent.OTHER, "Manufacturer Specific 46 - Comms Hub activated NCP image for sending to NCP"));
        EVENTS.add(new SswgEvent(0x01D2, MeterEvent.OTHER, "Manufacturer Specific 45 - Comms Hub received IHD image"));
        EVENTS.add(new SswgEvent(0x01D3, MeterEvent.OTHER, "Manufacturer Specific 44 - Comms Hub activated IHD image for sending over OTA"));
        EVENTS.add(new SswgEvent(0x01D4, MeterEvent.OTHER, "Manufacturer Specific 43"));
        EVENTS.add(new SswgEvent(0x01D5, MeterEvent.OTHER, "Manufacturer Specific 42"));
        EVENTS.add(new SswgEvent(0x01D6, MeterEvent.OTHER, "Manufacturer Specific 41"));
        EVENTS.add(new SswgEvent(0x01D7, MeterEvent.OTHER, "Manufacturer Specific 40"));
        EVENTS.add(new SswgEvent(0x01D8, MeterEvent.OTHER, "Manufacturer Specific 39"));
        EVENTS.add(new SswgEvent(0x01D9, MeterEvent.OTHER, "Manufacturer Specific 38"));
        EVENTS.add(new SswgEvent(0x01DA, MeterEvent.OTHER, "Manufacturer Specific 37"));
        EVENTS.add(new SswgEvent(0x01DB, MeterEvent.OTHER, "Manufacturer Specific 36"));
        EVENTS.add(new SswgEvent(0x01DC, MeterEvent.OTHER, "Manufacturer Specific 35"));
        EVENTS.add(new SswgEvent(0x01DD, MeterEvent.OTHER, "Manufacturer Specific 34"));
        EVENTS.add(new SswgEvent(0x01DE, MeterEvent.OTHER, "Manufacturer Specific 33"));
        EVENTS.add(new SswgEvent(0x01DF, MeterEvent.OTHER, "Manufacturer Specific 32"));
        EVENTS.add(new SswgEvent(0x01E0, MeterEvent.OTHER, "Manufacturer Specific 31"));
        EVENTS.add(new SswgEvent(0x01E1, MeterEvent.OTHER, "Manufacturer Specific 30"));
        EVENTS.add(new SswgEvent(0x01E2, MeterEvent.OTHER, "Manufacturer Specific 29"));
        EVENTS.add(new SswgEvent(0x01E3, MeterEvent.OTHER, "Manufacturer Specific 28"));
        EVENTS.add(new SswgEvent(0x01E4, MeterEvent.OTHER, "Manufacturer Specific 27"));
        EVENTS.add(new SswgEvent(0x01E5, MeterEvent.OTHER, "Manufacturer Specific 26"));
        EVENTS.add(new SswgEvent(0x01E6, MeterEvent.OTHER, "Manufacturer Specific 25"));
        EVENTS.add(new SswgEvent(0x01E7, MeterEvent.OTHER, "Manufacturer Specific 24"));
        EVENTS.add(new SswgEvent(0x01E8, MeterEvent.OTHER, "Manufacturer Specific 23"));
        EVENTS.add(new SswgEvent(0x01E9, MeterEvent.OTHER, "Manufacturer Specific 22"));
        EVENTS.add(new SswgEvent(0x01EA, MeterEvent.OTHER, "Manufacturer Specific 21"));
        EVENTS.add(new SswgEvent(0x01EB, MeterEvent.OTHER, "Manufacturer Specific 20"));
        EVENTS.add(new SswgEvent(0x01EC, MeterEvent.OTHER, "Manufacturer Specific 19"));
        EVENTS.add(new SswgEvent(0x01ED, MeterEvent.OTHER, "Manufacturer Specific 18"));
        EVENTS.add(new SswgEvent(0x01EE, MeterEvent.OTHER, "Manufacturer Specific 17"));
        EVENTS.add(new SswgEvent(0x01EF, MeterEvent.OTHER, "Manufacturer Specific 16"));
        EVENTS.add(new SswgEvent(0x01F0, MeterEvent.OTHER, "Manufacturer Specific 15"));
        EVENTS.add(new SswgEvent(0x01F1, MeterEvent.OTHER, "Manufacturer Specific 14"));
        EVENTS.add(new SswgEvent(0x01F2, MeterEvent.OTHER, "Manufacturer Specific 13"));
        EVENTS.add(new SswgEvent(0x01F3, MeterEvent.OTHER, "Manufacturer Specific 12"));
        EVENTS.add(new SswgEvent(0x01F4, MeterEvent.OTHER, "Manufacturer Specific 11"));
        EVENTS.add(new SswgEvent(0x01F5, MeterEvent.OTHER, "Manufacturer Specific 10"));
        EVENTS.add(new SswgEvent(0x01F6, MeterEvent.OTHER, "Manufacturer Specific 9"));
        EVENTS.add(new SswgEvent(0x01F7, MeterEvent.OTHER, "Manufacturer Specific 8"));
        EVENTS.add(new SswgEvent(0x01F8, MeterEvent.OTHER, "Manufacturer Specific 7"));
        EVENTS.add(new SswgEvent(0x01F9, MeterEvent.OTHER, "Manufacturer Specific 6"));
        EVENTS.add(new SswgEvent(0x01FA, MeterEvent.OTHER, "Manufacturer Specific 5"));
        EVENTS.add(new SswgEvent(0x01FB, MeterEvent.OTHER, "Manufacturer Specific 4"));
        EVENTS.add(new SswgEvent(0x01FC, MeterEvent.OTHER, "Manufacturer Specific 3"));
        EVENTS.add(new SswgEvent(0x01FD, MeterEvent.OTHER, "Manufacturer Specific 2"));
        EVENTS.add(new SswgEvent(0x01FE, MeterEvent.OTHER, "Manufacturer Specific 1"));
        EVENTS.add(new SswgEvent(0x01FF, MeterEvent.OTHER, "Manufacturer Specific 0"));
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

}
