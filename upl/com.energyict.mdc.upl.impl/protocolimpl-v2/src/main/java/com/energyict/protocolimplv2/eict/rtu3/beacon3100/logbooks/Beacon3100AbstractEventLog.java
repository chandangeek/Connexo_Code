package com.energyict.protocolimplv2.eict.rtu3.beacon3100.logbooks;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.protocol.MeterEvent;

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

    private static final int MAX_SHORT_UNSIGNED = 65535;
    private static final int EVENT_LOG_CLEARED_DEVICECODE = 255;

    protected TimeZone timeZone;
    protected DataContainer dcEvents;
    protected List<MeterEvent> meterEvents;

    /**
     * Mapping between Beacon deviceCode and EiServer MeterEvents
     */
    private static final HashMap <Long, Integer> deviceToEIServerCodeMapping = new HashMap<>();

    static {
        deviceToEIServerCodeMapping.put(0x000000FFL, MeterEvent.CLEARED);
        deviceToEIServerCodeMapping.put(0x00800000L, MeterEvent.POWER_MANAGEMENT_SWITCH_LOW_POWER);
        deviceToEIServerCodeMapping.put(0x00810000L, MeterEvent.POWER_MANAGEMENT_SWITCH_FULL_POWER);
        deviceToEIServerCodeMapping.put(0x00820000L, MeterEvent.POWER_MANAGEMENT_SWITCH_REDUCED_POWER);
        deviceToEIServerCodeMapping.put(0x00830000L, MeterEvent.POWER_MANAGEMENT_MAINS_LOST);
        deviceToEIServerCodeMapping.put(0x00840000L, MeterEvent.POWER_MANAGEMENT_MAINS_RECOVERED);
        deviceToEIServerCodeMapping.put(0x00850000L, MeterEvent.POWER_MANAGEMENT_LAST_GASP);
        deviceToEIServerCodeMapping.put(0x00860000L, MeterEvent.POWER_MANAGEMENT_BATTERY_CHARGE_START);
        deviceToEIServerCodeMapping.put(0x00870000L, MeterEvent.POWER_MANAGEMENT_BATTERY_CHARGE_STOP);
        deviceToEIServerCodeMapping.put(0x00a00000L, MeterEvent.IDIS_METER_DISCOVERY);
        deviceToEIServerCodeMapping.put(0x00a10000L, MeterEvent.IDIS_METER_ACCEPTED);
        deviceToEIServerCodeMapping.put(0x00a20000L, MeterEvent.IDIS_METER_REJECTED);
        deviceToEIServerCodeMapping.put(0x00a30000L, MeterEvent.IDIS_METER_ALARM);
        deviceToEIServerCodeMapping.put(0x00a40000L, MeterEvent.IDIS_ALARM_CONDITION);
        deviceToEIServerCodeMapping.put(0x00a50000L, MeterEvent.IDIS_MULTI_MASTER);
        deviceToEIServerCodeMapping.put(0x00a60000L, MeterEvent.IDIS_PLC_EQUIPMENT_IN_STATE_NEW);
        deviceToEIServerCodeMapping.put(0x00a70000L, MeterEvent.IDIS_EXTENDED_ALARM_STATUS);
        deviceToEIServerCodeMapping.put(0x00a80000L, MeterEvent.IDIS_METER_DELETED);
        deviceToEIServerCodeMapping.put(0x00a90000L, MeterEvent.IDIS_STACK_EVENT);
        deviceToEIServerCodeMapping.put(0x00b00000L, MeterEvent.PLC_PRIME_RESTARTED);
        deviceToEIServerCodeMapping.put(0x00b10000L, MeterEvent.PLC_PRIME_STACK_EVENT);
        deviceToEIServerCodeMapping.put(0x00b20000L, MeterEvent.PLC_PRIME_REGISTER_NODE);
        deviceToEIServerCodeMapping.put(0x00b30000L, MeterEvent.PLC_PRIME_UNREGISTER_NODE);
        deviceToEIServerCodeMapping.put(0x00c00000L, MeterEvent.PLC_G3_RESTARTED);
        deviceToEIServerCodeMapping.put(0x00c10000L, MeterEvent.PLC_G3_STACK_EVENT);
        deviceToEIServerCodeMapping.put(0x00c20000L, MeterEvent.PLC_G3_REGISTER_NODE);
        deviceToEIServerCodeMapping.put(0x00c30000L, MeterEvent.PLC_G3_UNREGISTER_NODE);
        deviceToEIServerCodeMapping.put(0x00c40000L, MeterEvent.PLC_G3_EVENT_RECEIVED);
        deviceToEIServerCodeMapping.put(0x00c50000L, MeterEvent.PLC_G3_JOIN_REQUEST_NODE);
        deviceToEIServerCodeMapping.put(0x00c60000L, MeterEvent.PLC_G3_UPPERMAC_STOPPED);
        deviceToEIServerCodeMapping.put(0x00c70000L, MeterEvent.PLC_G3_UPPERMAC_STARTED);
        deviceToEIServerCodeMapping.put(0x00c80000L, MeterEvent.PLC_G3_JOIN_FAILED);
        deviceToEIServerCodeMapping.put(0x00c90000L, MeterEvent.PLC_G3_AUTH_FAILURE);
        deviceToEIServerCodeMapping.put(0x00200000L, MeterEvent.DLMS_SERVER_SESSION_ACCEPTED);
        deviceToEIServerCodeMapping.put(0x00210000L, MeterEvent.DLMS_SERVER_SESSION_FINISHED);
        deviceToEIServerCodeMapping.put(0x00220000L, MeterEvent.DLMS_OTHER);
        deviceToEIServerCodeMapping.put(0x00230000L, MeterEvent.DLMS_UPSTREAM_TEST);
        deviceToEIServerCodeMapping.put(0x00300000L, MeterEvent.MODEM_WDG_PPPD_RESET);
        deviceToEIServerCodeMapping.put(0x00310000L, MeterEvent.MODEM_WDG_HW_RESET);
        deviceToEIServerCodeMapping.put(0x00320000L, MeterEvent.MODEM_WDG_REBOOT_REQUESTED);
        deviceToEIServerCodeMapping.put(0x00330000L, MeterEvent.MODEM_CONNECTED);
        deviceToEIServerCodeMapping.put(0x00340000L, MeterEvent.MODEM_DISCONNECTED);
        deviceToEIServerCodeMapping.put(0x00350000L, MeterEvent.MODEM_WAKE_UP);
        deviceToEIServerCodeMapping.put(0x00360000L, MeterEvent.PROTOCOL_PRELIMINARY_TASK_COMPLETED);
        deviceToEIServerCodeMapping.put(0x00370000L, MeterEvent.PROTOCOL_PRELIMINARY_TASK_FAILED);
        deviceToEIServerCodeMapping.put(0x00380000L, MeterEvent.PROTOCOL_CONSECUTIVE_FAILURE);
        deviceToEIServerCodeMapping.put(0x00990000L, MeterEvent.FIRMWARE_UPGRADE);
        deviceToEIServerCodeMapping.put(0x01000007L, MeterEvent.FIRMWARE_MODIFIED);
        deviceToEIServerCodeMapping.put(0x01010000L, MeterEvent.CPU_OVERLOAD);
        deviceToEIServerCodeMapping.put(0x01020000L, MeterEvent.RAM_TOO_HIGH);
        deviceToEIServerCodeMapping.put(0x01030000L, MeterEvent.DISK_USAGE_TOO_HIGH);
        deviceToEIServerCodeMapping.put(0x01040000L, MeterEvent.PACE_EXCEPTION);
        deviceToEIServerCodeMapping.put(0x01050000L, MeterEvent.SSH_LOGIN);
        deviceToEIServerCodeMapping.put(0x01060000L, MeterEvent.FACTORY_RESET);
        deviceToEIServerCodeMapping.put(0x02000000L, MeterEvent.WEBPORTAL_LOGIN);
        deviceToEIServerCodeMapping.put(0x02010000L, MeterEvent.WEBPORTAL_ACTION);
        deviceToEIServerCodeMapping.put(0x02020000L, MeterEvent.WEBPORTAL_FAILED_LOGIN);
        deviceToEIServerCodeMapping.put(0x02030000L, MeterEvent.WEBPORTAL_LOCKED_USER);
        deviceToEIServerCodeMapping.put(0x03000000L, MeterEvent.METER_MULTICAST_UPGRADE_START);
        deviceToEIServerCodeMapping.put(0x03010000L, MeterEvent.METER_MULTICAST_UPGRADE_COMPLETED);
        deviceToEIServerCodeMapping.put(0x03020000L, MeterEvent.METER_MULTICAST_UPGRADE_FAILED);
        deviceToEIServerCodeMapping.put(0x03030000L, MeterEvent.METER_MULTICAST_UPGRADE_INFO);
        deviceToEIServerCodeMapping.put(0x00006300L, MeterEvent.GENERAL_SECURITY_ERROR);
        deviceToEIServerCodeMapping.put(0x00016300L, MeterEvent.WRAP_KEY_ERROR);
        deviceToEIServerCodeMapping.put(0x04006300L, MeterEvent.DLMS_AUTHENTICATION_LEVEL_UPDATED);
        deviceToEIServerCodeMapping.put(0x04016300L, MeterEvent.DLMS_SECURITY_POLICY_UPDATED);
        deviceToEIServerCodeMapping.put(0x04026300L, MeterEvent.DLMS_SECURITY_SUITE_UPDATED);
        deviceToEIServerCodeMapping.put(0x04036300L, MeterEvent.DLMS_KEYS_UPDATED);
        deviceToEIServerCodeMapping.put(0x04046300L, MeterEvent.DLMS_ACCESS_VIOLATION);
        deviceToEIServerCodeMapping.put(0x04056300L, MeterEvent.DLMS_AUTHENTICATION_FAILURE);
        deviceToEIServerCodeMapping.put(0x04066300L, MeterEvent.DLMS_CIPHERING_ERROR);
        deviceToEIServerCodeMapping.put(0x00100000L, MeterEvent.UNKNOWN_REGISTER);

    }

    public Beacon3100AbstractEventLog(DataContainer dc, TimeZone timeZone) {
        this.timeZone = timeZone;
        this.dcEvents = dc;
    }

    protected abstract String getLogBookName();

    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int dlmsCode, int deviceCode, String message){

        int eiCode = MeterEvent.OTHER;
        String eventMessage = "";

        if (deviceCode == EVENT_LOG_CLEARED_DEVICECODE){
            eiCode = MeterEvent.EVENT_LOG_CLEARED;
            eventMessage = getLogBookName() + " cleared";
        } else {
            if(deviceCode <= MeterEvent.MAX_NUMBER_OF_EVENTS){
                eiCode = deviceCode;
            } else {
                Long code = Long.valueOf((deviceCode << 16) | (dlmsCode & 0XFFFF));
                Integer eiCodeMapping = deviceToEIServerCodeMapping.get(code);
                if(eiCodeMapping != null){
                    eiCode = eiCodeMapping.intValue();
                } else {
                    eiCode = MeterEvent.OTHER;
        }
                //Get code message from MeterEvent (message is the same, codes will be remapped)
                eventMessage = MeterEvent.codeToMessage(eiCode);
                //New event and CIM codes must be created in EiServer
                //Until then all above MeterEvent.MAX_NUMBER_OF_EVENTS are Unknown Event (0)
                eiCode = 0;
    }
        }
        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), eiCode, deviceCode, eventMessage));
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
