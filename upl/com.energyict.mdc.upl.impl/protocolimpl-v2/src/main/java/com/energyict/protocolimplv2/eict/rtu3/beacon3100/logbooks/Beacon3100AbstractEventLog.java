package com.energyict.protocolimplv2.eict.rtu3.beacon3100.logbooks;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 */
public abstract class Beacon3100AbstractEventLog {

    private static final int EVENT_LOG_CLEARED_DEVICECODE = 255;

    protected TimeZone timeZone;
    protected DataContainer dcEvents;
    protected List<MeterEvent> meterEvents;

    /**
     * Mapping between Beacon deviceCode and EiServer MeterEvents
     */
    private static final HashMap <Integer, Integer> deviceToEIServerCodeMapping = new HashMap<>();

    static {
        deviceToEIServerCodeMapping.put(0x000000FF, MeterEvent.CLEARED);
        deviceToEIServerCodeMapping.put(0x00800000, MeterEvent.POWER_MANAGEMENT_SWITCH_LOW_POWER);
        deviceToEIServerCodeMapping.put(0x00810000, MeterEvent.POWER_MANAGEMENT_SWITCH_FULL_POWER);
        deviceToEIServerCodeMapping.put(0x00820000, MeterEvent.POWER_MANAGEMENT_SWITCH_REDUCED_POWER);
        deviceToEIServerCodeMapping.put(0x00830000, MeterEvent.POWER_MANAGEMENT_MAINS_LOST);
        deviceToEIServerCodeMapping.put(0x00840000, MeterEvent.POWER_MANAGEMENT_MAINS_RECOVERED);
        deviceToEIServerCodeMapping.put(0x00850000, MeterEvent.POWER_MANAGEMENT_LAST_GASP);
        deviceToEIServerCodeMapping.put(0x00860000, MeterEvent.POWER_MANAGEMENT_BATTERY_CHARGE_START);
        deviceToEIServerCodeMapping.put(0x00870000, MeterEvent.POWER_MANAGEMENT_BATTERY_CHARGE_STOP);
        deviceToEIServerCodeMapping.put(0x00a00000, MeterEvent.IDIS_METER_DISCOVERY);
        deviceToEIServerCodeMapping.put(0x00a10000, MeterEvent.IDIS_METER_ACCEPTED);
        deviceToEIServerCodeMapping.put(0x00a20000, MeterEvent.IDIS_METER_REJECTED);
        deviceToEIServerCodeMapping.put(0x00a30000, MeterEvent.IDIS_METER_ALARM);
        deviceToEIServerCodeMapping.put(0x00a40000, MeterEvent.IDIS_ALARM_CONDITION);
        deviceToEIServerCodeMapping.put(0x00a50000, MeterEvent.IDIS_MULTI_MASTER);
        deviceToEIServerCodeMapping.put(0x00a60000, MeterEvent.IDIS_PLC_EQUIPMENT_IN_STATE_NEW);
        deviceToEIServerCodeMapping.put(0x00a70000, MeterEvent.IDIS_EXTENDED_ALARM_STATUS);
        deviceToEIServerCodeMapping.put(0x00a80000, MeterEvent.IDIS_METER_DELETED);
        deviceToEIServerCodeMapping.put(0x00a90000, MeterEvent.IDIS_STACK_EVENT);
        deviceToEIServerCodeMapping.put(0x00b00000, MeterEvent.PLC_PRIME_RESTARTED);
        deviceToEIServerCodeMapping.put(0x00b10000, MeterEvent.PLC_PRIME_STACK_EVENT);
        deviceToEIServerCodeMapping.put(0x00b20000, MeterEvent.PLC_PRIME_REGISTER_NODE);
        deviceToEIServerCodeMapping.put(0x00b30000, MeterEvent.PLC_PRIME_UNREGISTER_NODE);
        deviceToEIServerCodeMapping.put(0x00c00000, MeterEvent.PLC_G3_RESTARTED);
        deviceToEIServerCodeMapping.put(0x00c10000, MeterEvent.PLC_G3_STACK_EVENT);
        deviceToEIServerCodeMapping.put(0x00c20000, MeterEvent.PLC_G3_REGISTER_NODE);
        deviceToEIServerCodeMapping.put(0x00c30000, MeterEvent.PLC_G3_UNREGISTER_NODE);
        deviceToEIServerCodeMapping.put(0x00c40000, MeterEvent.PLC_G3_EVENT_RECEIVED);
        deviceToEIServerCodeMapping.put(0x00c50000, MeterEvent.PLC_G3_JOIN_REQUEST_NODE);
        deviceToEIServerCodeMapping.put(0x00c60000, MeterEvent.PLC_G3_UPPERMAC_STOPPED);
        deviceToEIServerCodeMapping.put(0x00c70000, MeterEvent.PLC_G3_UPPERMAC_STARTED);
        deviceToEIServerCodeMapping.put(0x00c80000, MeterEvent.PLC_G3_JOIN_FAILED);
        deviceToEIServerCodeMapping.put(0x00c90000, MeterEvent.PLC_G3_AUTH_FAILURE);
        //
        deviceToEIServerCodeMapping.put(0x00ca0000, MeterEvent.PLC_G3_BLACKLIST);
        deviceToEIServerCodeMapping.put(0x00cb0000, MeterEvent.PLC_G3_NODE_LINK_LOST);
        deviceToEIServerCodeMapping.put(0x00cc0000, MeterEvent.PLC_G3_NODE_LINK_RECOVERED);
        deviceToEIServerCodeMapping.put(0x00cd0000, MeterEvent.PLC_G3_PAN_ID);
        deviceToEIServerCodeMapping.put(0x00ce0000, MeterEvent.PLC_G3_TOPOLOGY_UPDATE);
        //
        deviceToEIServerCodeMapping.put(0x00D00000, MeterEvent.CLEAR_NODE_LIST);
        deviceToEIServerCodeMapping.put(0x00D10000, MeterEvent.HEART_BEAT);
        //
        deviceToEIServerCodeMapping.put(0x00200000, MeterEvent.DLMS_SERVER_SESSION_ACCEPTED);
        deviceToEIServerCodeMapping.put(0x00210000, MeterEvent.DLMS_SERVER_SESSION_FINISHED);
        deviceToEIServerCodeMapping.put(0x00220000, MeterEvent.DLMS_OTHER);
        deviceToEIServerCodeMapping.put(0x00230000, MeterEvent.DLMS_UPSTREAM_TEST);
        deviceToEIServerCodeMapping.put(0x00300000, MeterEvent.MODEM_WDG_PPPD_RESET);
        deviceToEIServerCodeMapping.put(0x00310000, MeterEvent.MODEM_WDG_HW_RESET);
        deviceToEIServerCodeMapping.put(0x00320000, MeterEvent.MODEM_WDG_REBOOT_REQUESTED);
        deviceToEIServerCodeMapping.put(0x00330000, MeterEvent.MODEM_CONNECTED);
        deviceToEIServerCodeMapping.put(0x00340000, MeterEvent.MODEM_DISCONNECTED);
        deviceToEIServerCodeMapping.put(0x00350000, MeterEvent.MODEM_WAKE_UP);
        //
        deviceToEIServerCodeMapping.put(0x00362600, MeterEvent.MODEM_NEW_SIM);
        deviceToEIServerCodeMapping.put(0x00372600, MeterEvent.MODEM_NEW_EQUIPMENT);
        //
        deviceToEIServerCodeMapping.put(0x00360000, MeterEvent.PROTOCOL_PRELIMINARY_TASK_COMPLETED);
        deviceToEIServerCodeMapping.put(0x00370000, MeterEvent.PROTOCOL_PRELIMINARY_TASK_FAILED);
        deviceToEIServerCodeMapping.put(0x00380000, MeterEvent.PROTOCOL_CONSECUTIVE_FAILURE);
        //
        deviceToEIServerCodeMapping.put(0x00390000, MeterEvent.CHECK_DATA_CONCENTRATOR_CONFIG);
        //
        deviceToEIServerCodeMapping.put(0x00990000, MeterEvent.FIRMWARE_UPGRADE);
        deviceToEIServerCodeMapping.put(0x01000007, MeterEvent.FIRMWARE_MODIFIED);
        deviceToEIServerCodeMapping.put(0x01010000, MeterEvent.CPU_OVERLOAD);
        deviceToEIServerCodeMapping.put(0x01020008, MeterEvent.RAM_TOO_HIGH);
        deviceToEIServerCodeMapping.put(0x01030000, MeterEvent.DISK_USAGE_TOO_HIGH);
        deviceToEIServerCodeMapping.put(0x01040000, MeterEvent.PACE_EXCEPTION); // SMART_CARD_EXCEPTION
        deviceToEIServerCodeMapping.put(0x01050000, MeterEvent.SSH_LOGIN);
        deviceToEIServerCodeMapping.put(0x01060000, MeterEvent.FACTORY_RESET);
        //
        deviceToEIServerCodeMapping.put(0x01070000, MeterEvent.LINK_UP);
        deviceToEIServerCodeMapping.put(0x01080000, MeterEvent.LINK_DOWN);
        deviceToEIServerCodeMapping.put(0x01090000, MeterEvent.USB_ADD);
        deviceToEIServerCodeMapping.put(0x010a0000, MeterEvent.USB_REMOVE);
        deviceToEIServerCodeMapping.put(0x010b0000, MeterEvent.FILE_TRANSFER_COMPLETED);
        deviceToEIServerCodeMapping.put(0x010c0000, MeterEvent.FILE_TRANSFER_FAILED);
        deviceToEIServerCodeMapping.put(0x010d0000, MeterEvent.SCRIPT_EXECUTION_STARTED);
        deviceToEIServerCodeMapping.put(0x010e0000, MeterEvent.SCRIPT_EXECUTION_COMPLETED);
        deviceToEIServerCodeMapping.put(0x010f0000, MeterEvent.SCRIPT_EXECUTION_FAILED);
        deviceToEIServerCodeMapping.put(0x01100000, MeterEvent.SCRIPT_EXECUTION_SCHEDULED);
        deviceToEIServerCodeMapping.put(0x01110000, MeterEvent.SCRIPT_EXECUTION_DESCHEDULED);
        //
        deviceToEIServerCodeMapping.put(0x02000000, MeterEvent.WEBPORTAL_LOGIN);
        deviceToEIServerCodeMapping.put(0x02010000, MeterEvent.WEBPORTAL_ACTION);
        deviceToEIServerCodeMapping.put(0x02020000, MeterEvent.WEBPORTAL_FAILED_LOGIN);
        deviceToEIServerCodeMapping.put(0x02030000, MeterEvent.WEBPORTAL_LOCKED_USER);
        //
        deviceToEIServerCodeMapping.put(0x02040000, MeterEvent.WEBPORTAL_CSRF_ATTACK);
        //
        deviceToEIServerCodeMapping.put(0x03000000, MeterEvent.METER_MULTICAST_UPGRADE_START);
        deviceToEIServerCodeMapping.put(0x03010000, MeterEvent.METER_MULTICAST_UPGRADE_COMPLETED);
        deviceToEIServerCodeMapping.put(0x03020000, MeterEvent.METER_MULTICAST_UPGRADE_FAILED);
        deviceToEIServerCodeMapping.put(0x03030000, MeterEvent.METER_MULTICAST_UPGRADE_INFO);
        //
        deviceToEIServerCodeMapping.put(0x00005300, MeterEvent.SNMP_OTHER);
        deviceToEIServerCodeMapping.put(0x00015300, MeterEvent.SNMP_INFO);
        deviceToEIServerCodeMapping.put(0x00025300, MeterEvent.SNMP_UNSUPPORTED_VERSION);
        deviceToEIServerCodeMapping.put(0x00035300, MeterEvent.SNMP_UNSUPPORTED_SEC_MODEL);
        deviceToEIServerCodeMapping.put(0x00045300, MeterEvent.SNMP_INVALID_USER_NAME);
        deviceToEIServerCodeMapping.put(0x00055300, MeterEvent.SNMP_INVALID_ENGINE_ID);
        //
        deviceToEIServerCodeMapping.put(0x00006300, MeterEvent.GENERAL_SECURITY_ERROR);
        deviceToEIServerCodeMapping.put(0x00016300, MeterEvent.WRAP_KEY_ERROR);
        deviceToEIServerCodeMapping.put(0x04006300, MeterEvent.DLMS_AUTHENTICATION_LEVEL_UPDATED);
        deviceToEIServerCodeMapping.put(0x04016300, MeterEvent.DLMS_SECURITY_POLICY_UPDATED);
        deviceToEIServerCodeMapping.put(0x04026300, MeterEvent.DLMS_SECURITY_SUITE_UPDATED);
        deviceToEIServerCodeMapping.put(0x04036300, MeterEvent.DLMS_KEYS_UPDATED);
        deviceToEIServerCodeMapping.put(0x04046300, MeterEvent.DLMS_ACCESS_VIOLATION);
        deviceToEIServerCodeMapping.put(0x04056300, MeterEvent.DLMS_AUTHENTICATION_FAILURE);
        deviceToEIServerCodeMapping.put(0x04066300, MeterEvent.DLMS_CIPHERING_ERROR);
        //
        deviceToEIServerCodeMapping.put(0x04076300, MeterEvent.SNMP_AUTHENTICATION_FAILURE);
        deviceToEIServerCodeMapping.put(0x04086300, MeterEvent.SNMP_KEYS_UPDATED);
        deviceToEIServerCodeMapping.put(0x04096300, MeterEvent.CRL_UPDATED);
        deviceToEIServerCodeMapping.put(0x040a6300, MeterEvent.CRL_UPDATE_REJECTED);
        deviceToEIServerCodeMapping.put(0x040b6300, MeterEvent.KEY_UPDATE_REQUEST);
        deviceToEIServerCodeMapping.put(0x040c6300, MeterEvent.CRL_REMOVED);
        deviceToEIServerCodeMapping.put(0x040d6300, MeterEvent.DOT1X_SUCCESS);
        deviceToEIServerCodeMapping.put(0x040e6300, MeterEvent.DOT1X_FAILURE);
        deviceToEIServerCodeMapping.put(0x040f6300, MeterEvent.REPLAY_ATTACK);
        deviceToEIServerCodeMapping.put(0x04106300, MeterEvent.CERTIFICATE_ADDED);
        deviceToEIServerCodeMapping.put(0x04116300, MeterEvent.CERTIFICATE_REMOVED);
        deviceToEIServerCodeMapping.put(0x04126300, MeterEvent.CERTIFICATE_EXPIRED);
        //
        deviceToEIServerCodeMapping.put(0x00100000, MeterEvent.UNKNOWN_REGISTER);
        // General
        deviceToEIServerCodeMapping.put(0x00010001, MeterEvent.POWER_DOWN_POWER_LOST);
        deviceToEIServerCodeMapping.put(0x00020001, MeterEvent.POWER_DOWN_USER_REQUEST);
        deviceToEIServerCodeMapping.put(0x00030001, MeterEvent.POWER_DOWN_SOFTWARE_FAULT);
        deviceToEIServerCodeMapping.put(0x00040001, MeterEvent.POWER_DOWN_HARDWARE_FAULT);
        deviceToEIServerCodeMapping.put(0x00050001, MeterEvent.POWER_DOWN_NETWORK_INACTIVITY);
        deviceToEIServerCodeMapping.put(0x00060001, MeterEvent.POWER_DOWN_FIRMWARE_UPGRADE);
        deviceToEIServerCodeMapping.put(0x00070001, MeterEvent.POWER_DOWN_FIRMWARE_ROLLBACK);
        deviceToEIServerCodeMapping.put(0x00080001, MeterEvent.POWER_DOWN_DISK_ERROR);
        deviceToEIServerCodeMapping.put(0x00090001, MeterEvent.POWER_DOWN_CONFIGURATION_ERROR);
        deviceToEIServerCodeMapping.put(0x000a0001, MeterEvent.POWER_DOWN_FACTORY_RESET);
        deviceToEIServerCodeMapping.put(0x000b0001, MeterEvent.POWER_DOWN_TAMPERING);
        deviceToEIServerCodeMapping.put(0x000c0001, MeterEvent.POWER_DOWN_TEMPERATURE);
        deviceToEIServerCodeMapping.put(0x01000001, MeterEvent.POWER_DOWN_SYSTEM_WATCHDOG);
        deviceToEIServerCodeMapping.put(0x01010001, MeterEvent.POWER_DOWN_WWAN_MODEM_WATCHDOG);
        deviceToEIServerCodeMapping.put(0x01020001, MeterEvent.POWER_DOWN_SECURE_ELEMENT_WATCHDOG);
        deviceToEIServerCodeMapping.put(0x01030001, MeterEvent.POWER_DOWN_EXTERNAL_WATCHDOG);
        deviceToEIServerCodeMapping.put(0x0010001c, MeterEvent.PROTOCOL_LOG_CLEARED);
        deviceToEIServerCodeMapping.put(0x0001001e, MeterEvent.METER_CLOCK_INVALID);
    }

    public Beacon3100AbstractEventLog(DataContainer dc, TimeZone timeZone) {
        this.timeZone = timeZone;
        this.dcEvents = dc;
    }

    protected abstract String getLogBookName();

    private static void addMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int dlmsCode, int deviceCode, String message, String logbookName){
        meterEvents.add(buildMeterEvent(eventTimeStamp, dlmsCode, deviceCode, message, logbookName));
    }

    public static MeterEvent buildMeterEvent(Date eventTimeStamp, int dlmsCode, int deviceCode, String message, String logbookName){

        int eiCode = MeterEvent.OTHER;
        String eventMessage = "";

        Integer eventKey = (deviceCode << 16) | (dlmsCode & 0XFFFF);

        if (deviceCode == EVENT_LOG_CLEARED_DEVICECODE){
            eiCode = MeterEvent.EVENT_LOG_CLEARED;
            eventMessage = logbookName + " cleared";
        } else {
            if(eventKey <= MeterEvent.MAX_NUMBER_OF_EVENTS){
                eiCode = dlmsCode;
            } else {
                Integer eiCodeMapping = deviceToEIServerCodeMapping.get(eventKey);
                if(eiCodeMapping != null){
                    eiCode = eiCodeMapping;
                } else {
                    eiCode = MeterEvent.OTHER;
                }
                //Get code message from MeterEvent (message is the same, codes will be remapped)
                eventMessage = MeterEvent.codeToMessage(eiCode);
                //All events for which we don't have a mapping in MeterEvent will result in Unknown Event (0)
            }
        }
        String composedEventMessage = eventMessage.isEmpty() ? MeterEvent.codeToMessage(eiCode) : getDescriptionPrefix(eiCode) + message;
        return new MeterEvent((Date) eventTimeStamp.clone(), eiCode, eventKey, composedEventMessage);
    }

    /**
     *
     * @return value to be appended in front of received event description
     */
    public static String getDescriptionPrefix(int eiCode){
        return MeterEvent.codeToMessage(eiCode) + " ";
    }

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

            addMeterEvent(meterEvents, eventTimeStamp, (int)dlmsCode, (int)deviceCode, message, getLogBookName());
        }

        return meterEvents;
    }

    public List<MeterProtocolEvent> getMeterProtocolEvents() throws IOException {
        return MeterEvent.mapMeterEventsToMeterProtocolEvents(this.getMeterEvents());
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
