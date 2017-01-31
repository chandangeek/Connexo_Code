/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.eict.rtu3.beacon3100.logbooks;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

public abstract class Beacon3100AbstractEventLog {

    public static final int MAX_SHORT_UNSIGNED = 65535;
    public static final int EVENT_LOG_CLEARED_DEVICECODE = 255;

    protected TimeZone timeZone;
    protected DataContainer dcEvents;
    protected List<MeterEvent> meterEvents;
    protected static final HashMap eventsMap = new HashMap();

    static {
        eventsMap.put(0x00000000L, " Other");
        eventsMap.put(0x00000001L, " Power down");
        eventsMap.put(0x00000002L, " Power up");
        eventsMap.put(0x00000003L, " Watchdog reset");
        eventsMap.put(0x00000004L, " Before clock set");
        eventsMap.put(0x00000005L, " After clock set");
        eventsMap.put(0x00000006L, " Clock set");
        eventsMap.put(0x00000007L, " Configuration change");
        eventsMap.put(0x00000008L, " RAM memory error");
        eventsMap.put(0x00000009L, " Program flow error");
        eventsMap.put(0x0000000AL, " Register overflow");
        eventsMap.put(0x0000000BL, " Fatal error");
        eventsMap.put(0x0000000CL, " Clear data");
        eventsMap.put(0x0000000DL, " Hardware error");
        eventsMap.put(0x0000000EL, " Meter alarm");
        eventsMap.put(0x0000000FL, " Rom memory error");
        eventsMap.put(0x00000010L, " Maximum demand reset");
        eventsMap.put(0x00000011L, " Billing action");
        eventsMap.put(0x00000012L, " Application alert start");
        eventsMap.put(0x00000013L, " Application alert stop");
        eventsMap.put(0x00000014L, " Phase failure");
        eventsMap.put(0x00000017L, " Tamper detected");
        eventsMap.put(0x00000018L, " Meter cover opened");
        eventsMap.put(0x00000019L, " Terminal cover opened");
        eventsMap.put(0x0000001AL, " Reverse Run");
        eventsMap.put(0x0000001BL, " Load Profile cleared");
        eventsMap.put(0x0000001CL, " Event log cleared");
        eventsMap.put(0x0010001CL, " Protocol log cleared");
        eventsMap.put(0x0000001DL, " Daylight saving time enabled or disabled");
        eventsMap.put(0x0000001EL, " Clock invalid");
        eventsMap.put(0x0000001FL, " Replace Battery");
        eventsMap.put(0x00000020L, " Battery voltage low");
        eventsMap.put(0x00000021L, " TOU activated");
        eventsMap.put(0x00000022L, " Error register cleared");
        eventsMap.put(0x00000023L, " Alarm register cleared");
        eventsMap.put(0x00000024L, " Program memory error");
        eventsMap.put(0x00000025L, " NV memory error");
        eventsMap.put(0x00000026L, " Watchdog error");
        eventsMap.put(0x00000027L, " Measurement system error");
        eventsMap.put(0x00000028L, " Firmware ready for activation");
        eventsMap.put(0x00000029L, " Firmware activated");
        eventsMap.put(0x0000002AL, " Terminal cover closed");
        eventsMap.put(0x0000002BL, " Strong DC field detected");
        eventsMap.put(0x0000002CL, " No strong DC field anymore");
        eventsMap.put(0x0000002DL, " Meter cover closed");
        eventsMap.put(0x0000002EL, " n times wrong passwordd");
        eventsMap.put(0x0000002FL, " Replace Battery");
        eventsMap.put(0x00000030L, " Manual connection");
        eventsMap.put(0x00000031L, " Remote disconnection");
        eventsMap.put(0x00000032L, " Remote connection");
        eventsMap.put(0x00000033L, " Local disconnection");
        eventsMap.put(0x00000034L, " Limiter threshold exceeded");
        eventsMap.put(0x00000035L, " Limiter threshold ok");
        eventsMap.put(0x00000036L, " Limiter threshold changed");
        eventsMap.put(0x00000037L, " Communication error MBus");
        eventsMap.put(0x00000038L, " Communication ok M-Bus");
        eventsMap.put(0x00000039L, " Replace Battery M-Bus");
        eventsMap.put(0x0000003AL, " Fraud attempt M-Bus");
        eventsMap.put(0x0000003BL, " Clock adjusted M-Bus");
        eventsMap.put(0x0000003CL, " Manual disconnection M-Bus");
        eventsMap.put(0x0000003DL, " Manual connection M-Bus");
        eventsMap.put(0x0000003EL, " Remote disconnection MBus");
        eventsMap.put(0x0000003FL, " Remote connection MBus");
        eventsMap.put(0x00000040L, " Valve alarm M-Bus");
        eventsMap.put(0x00100000L, " Unknown register");
        eventsMap.put(0x00800000L, " POWER_MANAGEMENT_SWITCH_LOW_POWER");
        eventsMap.put(0x00810000L, " POWER_MANAGEMENT_SWITCH_FULL_POWER");
        eventsMap.put(0x00820000L, " POWER_MANAGEMENT_SWITCH_REDUCED_POWER");
        eventsMap.put(0x00830000L, " POWER_MANAGEMENT_MAINS_LOST");
        eventsMap.put(0x00840000L, " POWER_MANAGEMENT_MAINS_RECOVERED");
        eventsMap.put(0x00850000L, " POWER_MANAGEMENT_LAST_GASP");
        eventsMap.put(0x00860000L, " POWER_MANAGEMENT_BATTERY_CHARGE_START");
        eventsMap.put(0x00870000L, " POWER_MANAGEMENT_BATTERY_CHARGE_STOP");
        eventsMap.put(0x00A00000L, " IDIS_METER_DISCOVERY");
        eventsMap.put(0x00A10000L, " IDIS_METER_ACCEPTED");
        eventsMap.put(0x00A20000L, " IDIS_METER_REJECTED");
        eventsMap.put(0x00A30000L, " IDIS_METER_ALARM");
        eventsMap.put(0x00A40000L, " IDIS_ALARM_CONDITION");
        eventsMap.put(0x00A50000L, " IDIS_MULTI_MASTER");
        eventsMap.put(0x00A60000L, " IDIS_PLC_EQUIPMENT_IN_STATE_NEW");
        eventsMap.put(0x00A70000L, " IDIS_EXTENDED_ALARM_STATUS");
        eventsMap.put(0x00A80000L, " IDIS_METER_DELETED");
        eventsMap.put(0x00A90000L, " IDIS_STACK_EVENT");
        eventsMap.put(0x00B00000L, " PLC_PRIME_RESTARTED");
        eventsMap.put(0x00B10000L, " PLC_PRIME_STACK_EVENT");
        eventsMap.put(0x00B20000L, " PLC_PRIME_REGISTER_NODE");
        eventsMap.put(0x00B30000L, " PLC_PRIME_UNREGISTER_NODE");
        eventsMap.put(0x00C00000L, " PLC_G3_RESTARTED");
        eventsMap.put(0x00C10000L, " PLC_G3_STACK_EVENT");
        eventsMap.put(0x00C20000L, " PLC_G3_REGISTER_NODE");
        eventsMap.put(0x00C30000L, " PLC_G3_UNREGISTER_NODE");
        eventsMap.put(0x00C40000L, " PLC_G3_EVENT_RECEIVED");
        eventsMap.put(0x00C50000L, " PLC_G3_JOIN_REQUEST_NODE");
        eventsMap.put(0x00C60000L, " PLC_G3_UPPERMAC_STOPPED");
        eventsMap.put(0x00C70000L, " PLC_G3_UPPERMAC_STARTED");
        eventsMap.put(0x00C80000L, " PLC_G3_JOIN_FAILED");
        eventsMap.put(0x00C90000L, " PLC_G3_AUTH_FAILURE");
        eventsMap.put(0x00200000L, " DLMS_SERVER_SESSION_ACCEPTED");
        eventsMap.put(0x00210000L, " DLMS_SERVER_SESSION_FINISHED");
        eventsMap.put(0x00220000L, " DLMS_OTHER");
        eventsMap.put(0x00230000L, " DLMS_UPSTREAM_TEST");
        eventsMap.put(0x00300000L, " MODEM_WDG_PPPD_RESET");
        eventsMap.put(0x00310000L, " MODEM_WDG_HW_RESET");
        eventsMap.put(0x00320000L, " MODEM_WDG_REBOOT_REQUESTED");
        eventsMap.put(0x00330000L, " MODEM_CONNECTED");
        eventsMap.put(0x00340000L, " MODEM_DISCONNECTED");
        eventsMap.put(0x00350000L, " MODEM_WAKE_UP");
        eventsMap.put(0x00360000L, " PROTOCOL_PRELIMINARY_TASK_COMPLETED");
        eventsMap.put(0x00370000L, " PROTOCOL_PRELIMINARY_TASK_FAILED");
        eventsMap.put(0x00380000L, " PROTOCOL_CONSECUTIVE_FAILURE");
        eventsMap.put(0x00990000L, " FIRMWARE_UPGRADE");
        eventsMap.put(0x01000007L, " FIRMWARE_MODIFIED");
        eventsMap.put(0x01010000L, " CPU_OVERLOAD");
        eventsMap.put(0x01020000L, " RAM_TOO_HIGH");
        eventsMap.put(0x01030000L, " DISK_USAGE_TOO_HIGH");
        eventsMap.put(0x01040000L, " PACE_EXCEPTION");
        eventsMap.put(0x01050000L, " SSH_LOGIN");
        eventsMap.put(0x01060000L, " FACTORY_RESET");
        eventsMap.put(0x02000000L, " WEBPORTAL_LOGIN");
        eventsMap.put(0x02010000L, " WEBPORTAL_ACTION");
        eventsMap.put(0x02020000L, " WEBPORTAL_FAILED_LOGIN");
        eventsMap.put(0x02030000L, " WEBPORTAL_LOCKED_USER");
        eventsMap.put(0x03000000L, " METER_MULTICAST_UPGRADE_START");
        eventsMap.put(0x03010000L, " METER_MULTICAST_UPGRADE_COMPLETED");
        eventsMap.put(0x03020000L, " METER_MULTICAST_UPGRADE_FAILED");
        eventsMap.put(0x03030000L, " METER_MULTICAST_UPGRADE_INFO");
        eventsMap.put(0x00006300L, " GENERAL_SECURITY_ERROR");
        eventsMap.put(0x00016300L, " WRAP_KEY_ERROR");
        eventsMap.put(0x04006300L, " DLMS_AUTHENTICATION_LEVEL_UPDATED");
        eventsMap.put(0x04016300L, " DLMS_SECURITY_POLICY_UPDATED");
        eventsMap.put(0x04026300L, " DLMS_SECURITY_SUITE_UPDATED");
        eventsMap.put(0x04036300L, " DLMS_KEYS_UPDATED");
        eventsMap.put(0x04046300L, " DLMS_ACCESS_VIOLATION");
        eventsMap.put(0x04056300L, " DLMS_AUTHENTICATION_FAILURE");
        eventsMap.put(0x04066300L, " DLMS_CIPHERING_ERROR");
        eventsMap.put(0x000000FFL, " cleared");
    }

    public Beacon3100AbstractEventLog(DataContainer dc, TimeZone timeZone) {
        this.timeZone = timeZone;
        this.dcEvents = dc;
    }

    protected abstract String getLogBookName();

    protected String getEventInfo(int dlmsCode, int deviceCode) {
        if ((dlmsCode < MAX_SHORT_UNSIGNED) && (deviceCode < MAX_SHORT_UNSIGNED)) {
            long eventKey = (deviceCode << 16) | (dlmsCode & 0XFFFF);
            return (String) eventsMap.get(eventKey);
        } else {
            throw new IllegalArgumentException("Parameter is out of range: dlmsCode - " + dlmsCode + ";deviceCode - " + deviceCode);
        }
    }

    protected abstract void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int dlmsCode, int deviceCode, String message);

    /** https://confluence.eict.vpdc/display/G3IntBeacon3100/DLMS+event+log+books
     *
     * Event structure details:
     *
     * 0:   Timestamp        COSEM-DATE-TIME     Event timestamp
     * 1:   DLMS code        long-unsigned       Event DLMS code
     * 2:   Device code      long-unsigned       Event device code
     * 3:   Message          OCTET-STRING        (optional) message
     *

     * @return the MeterEvent List
     */
    public List<MeterEvent> getMeterEvents() throws IOException {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        int size = this.dcEvents.getRoot().getNrOfElements();
        Date eventTimeStamp;
        long dlmsCode;
        long deviceCode;
        String  message;

        for (int i = 0; i <= (size - 1); i++) {
            DataStructure eventStructure = this.dcEvents.getRoot().getStructure(i);

            eventTimeStamp = eventStructure.getOctetString(0).toDate(timeZone);
            dlmsCode = (long) eventStructure.getValue(1) & 0xFFFFFFF;      // To prevent negative values
            deviceCode = (long) eventStructure.getValue(2) & 0xFFFFFFF;    // To prevent negative values
            message = eventStructure.getOctetString(3).toString();

            buildMeterEvent(meterEvents, eventTimeStamp, (int)dlmsCode, (int)deviceCode, message);
        }

        return meterEvents;
    }

    protected String getDefaultEventDescription(int dlmsCode, int deviceCode, String message){
        return "Unknown eventcode: dlmsCode=" + dlmsCode + ", deviceCode="+deviceCode+", message=["+message+"] in "+getLogBookName()+"";
    }


    /**
     * Checks if the given {@link Object} is an {@link com.energyict.dlms.OctetString}
     *
     * @param element the object to check the type
     * @return true or false
     */
    protected boolean isOctetString(Object element) {
        return (element instanceof com.energyict.dlms.OctetString);
    }
}
