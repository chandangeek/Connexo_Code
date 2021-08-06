package com.energyict.protocolimplv2.umi.ei4.structures;

import com.energyict.protocolimplv2.umi.types.UmiCode;
import com.energyict.protocolimplv2.umi.util.LittleEndianData;

import java.util.HashMap;
import java.util.Map;

public class UmiwanConfiguration extends LittleEndianData {
    public static final int SIZE = 138;
    public static final UmiCode UMIWAN_CONFIGURATION_UMI_CODE = new UmiCode("umi.1.1.194.11");

    /**
     * Primary IP address for the Headend System (HES)
     */
    private String primHost;    // 32
    /**
     * Secondary IP address for the Headend System.
     * This address is used if the primary address is not responding.
     */
    private String secHost;     // 32
    /**
     * IP address for special services e.g.: the gateway
     */
    private String gateHost;    // 32
    /**
     * Contains the inactivity timeout in milliseconds (ms).
     * The GSM/GPRS modem is automatically switched off, if no messages are being received by the modem during the inactivity timeout period.
     */

    private long inactiveTimeout; // 4
    /**
     * Contains the maximum duration in milliseconds (ms)
     * of a GSM connection. After sessionTimeout, the modem is switched off.
     **/
    private long sessionTimeout;   // 4
    /**
     * Contains the preferred time of the day for the meter reporting, which is when the meter wakes up and establishes a GSM connection with the head-end.
     * This attribute has to be activated in the attribute 'controlFlags'.
     * Format: 0xddhhmmss where dd: day (set to 0), hh: hour, mm: minute, ss: second. E.g.: 0x00113000 means 17:50.
     * Value is based on UTC
     **/
    private long preferredTimeOfDay; // 4

    /**
     * Time added to 'Preferred Time of Day' for additional next call, 'Preferred Time of Day' is the fixed call time, regardless of  this value.
     * Format: 0xddhhmmss (see above)
     **/
    private long callDistance;   // 4

    /**
     * Contains the time interval between two connection attempt retries with a short repetition time (after a short break/pause).
     * Format: 0xddhhmmss (see above).
     **/
    private long shortRetryDistance; // 4

    /**
     * Contains the time interval between two connection attempt retries with a long repetition time (after a long break/pause).
     * Format: 0xddhhmmss (see above).
     **/
    private long longRetryDistance; // 4
    /**
     * The point in time of a GSM connection is scattered randomly within a time period randomZone.
     * This shall ensure that not all gas meters wake up at the same point in time and try to establish a GSM connection.
     * Format: 0xddhhmmss (see above).
     */
    private long randomZone; // 4

    /**
     * bit 0: cyclic mode.
     * bit 1: preferred time of day (only valid if cyclic mode enabled).
     * bit 2: GSM/GPRS-only mode (eg. factory test)
     */

    /**
     * Description from the GSM_Module Specifications_DataSheets_T5:
     * Bit 0 Cyclic mode
     * • 0 : disabled
     * • 1 : enabled
     * The flag activates a cyclic reporting scheme using the attribute callDistance (time interval for meter wake up).
     * Bit 1 Preferred Date and Time
     * • 0 : disabled
     * • 1 : enabled
     * The flag activates a reporting scheme using the attribute preferredTimeOfDay (dedicated time of the day for meter wake up).
     * Bit 2 Secondary host
     * • 0 : not available
     * • 1 : available
     * If the flag is activated and if a TCP connection with the primary head-end host has failed, a TCP connection with the
     * secondary head-end host will be established, without interruption/disconnection of the GPRS connection.
     * B3 EK280Handshake for Primary Host
     * • 0 : do not use Ek280 handshake
     * • 1 : use Ek280 handshake
     * B4 EK280Handshake for Secondary Host
     * • 0 : do not use Ek280 handshake
     * • 1 : use Ek280 handshake
     */
    private long controlFlags; // 4

    /**
     * Port number used for the primary address
     */
    private int primPort; // 2

    /**
     * Port number used for the secondary address
     */
    private int secPort;  // 2

    /**
     * Port number for the gateway
     */
    private int gatePort; // 2

    /**
     * Contains the number of connection attempts with a short repetition time 'shortRetryDistance'.
     */
    private int maxShortRetries; // 2

    /**
     * Contains the number of connection attempts with a long repetition time 'longRetryDistance'.
     * Connection attempts with a long repetition time will be started only if all connection attempts with a short repetition time have failed.
     */
    private int maxLongRetries; // 2

    public UmiwanConfiguration(byte[] raw) {
        super(raw, SIZE, false);

        byte[] primHostBytes = new byte[32];
        getRawBuffer().get(primHostBytes);
        this.primHost = String.copyValueOf(UmiHelper.convertBytesToChars(primHostBytes)).trim();
        byte[] secHostBytes = new byte[32];
        getRawBuffer().get(secHostBytes);
        this.secHost = String.copyValueOf(UmiHelper.convertBytesToChars(secHostBytes)).trim();
        byte[] gateHostBytes = new byte[32];
        getRawBuffer().get(gateHostBytes);
        this.gateHost = String.copyValueOf(UmiHelper.convertBytesToChars(gateHostBytes)).trim();

        this.inactiveTimeout = Integer.toUnsignedLong(getRawBuffer().getInt());
        this.sessionTimeout = Integer.toUnsignedLong(getRawBuffer().getInt());
        this.preferredTimeOfDay = Integer.toUnsignedLong(getRawBuffer().getInt());
        this.callDistance = Integer.toUnsignedLong(getRawBuffer().getInt());
        this.shortRetryDistance = Integer.toUnsignedLong(getRawBuffer().getInt());
        this.longRetryDistance = Integer.toUnsignedLong(getRawBuffer().getInt());
        this.randomZone = Integer.toUnsignedLong(getRawBuffer().getInt());
        this.controlFlags = Integer.toUnsignedLong(getRawBuffer().getInt());
        this.primPort = Short.toUnsignedInt(getRawBuffer().getShort());
        this.secPort = Short.toUnsignedInt(getRawBuffer().getShort());
        this.gatePort = Short.toUnsignedInt(getRawBuffer().getShort());
        this.maxShortRetries = Short.toUnsignedInt(getRawBuffer().getShort());
        this.maxLongRetries = Short.toUnsignedInt(getRawBuffer().getShort());
    }

    public UmiwanConfiguration(Map<String, Object> objectMap) {
        super(SIZE);
        this.primHost = (String) objectMap.get("primHost");
        this.secHost = (String) objectMap.get("secHost");
        this.gateHost = (String) objectMap.get("gateHost");
        this.inactiveTimeout = Long.parseLong(String.valueOf(objectMap.get("inactiveTimeout")));
        this.sessionTimeout = Long.parseLong(String.valueOf(objectMap.get("sessionTimeout")));
        this.preferredTimeOfDay = Long.parseLong(String.valueOf(objectMap.get("preferredTimeOfDay")));
        this.callDistance = Long.parseLong(String.valueOf(objectMap.get("callDistance")));
        this.shortRetryDistance = Long.parseLong(String.valueOf(objectMap.get("shortRetryDistance")));
        this.longRetryDistance = Long.parseLong(String.valueOf(objectMap.get("longRetryDistance")));
        this.randomZone = Long.parseLong(String.valueOf(objectMap.get("randomZone")));
        this.controlFlags = Long.parseLong(String.valueOf(objectMap.get("controlFlags")));
        this.primPort = Integer.parseInt(String.valueOf(objectMap.get("primPort")));
        this.secPort = Integer.parseInt(String.valueOf(objectMap.get("secPort")));
        this.gatePort = Integer.parseInt(String.valueOf(objectMap.get("gatePort")));
        this.maxShortRetries = Integer.parseInt(String.valueOf(objectMap.get("maxShortRetries")));
        this.maxLongRetries = Integer.parseInt(String.valueOf(objectMap.get("maxLongRetries")));

        getRawBuffer().put(UmiHelper.convertCharsToBytes(this.primHost.toCharArray()))
                .put(UmiHelper.convertCharsToBytes(this.secHost.toCharArray()))
                .put(UmiHelper.convertCharsToBytes(this.gateHost.toCharArray()))
                .putInt((int) this.inactiveTimeout)
                .putInt((int) this.sessionTimeout)
                .putInt((int) this.preferredTimeOfDay)
                .putInt((int) this.callDistance)
                .putInt((int) this.shortRetryDistance)
                .putInt((int) this.longRetryDistance)
                .putInt((int) this.randomZone)
                .putInt((int) this.controlFlags)
                .putShort((short) this.primPort)
                .putShort((short) this.secPort)
                .putShort((short) this.gatePort)
                .putShort((short) this.maxShortRetries)
                .putShort((short) this.maxLongRetries);
    }

    public String getPrimHost() {
        return this.primHost;
    }

    public int getPrimPort() {
        return this.primPort;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("primHost", primHost);
        map.put("secHost", secHost);
        map.put("gateHost", gateHost);
        map.put("inactiveTimeout", inactiveTimeout);
        map.put("sessionTimeout", sessionTimeout);
        map.put("preferredTimeOfDay", preferredTimeOfDay);
        map.put("callDistance", callDistance);
        map.put("shortRetryDistance", shortRetryDistance);
        map.put("longRetryDistance", longRetryDistance);
        map.put("randomZone", randomZone);
        map.put("controlFlags", controlFlags);
        map.put("primPort", primPort);
        map.put("secPort", secPort);
        map.put("gatePort", gatePort);
        map.put("maxShortRetries", maxShortRetries);
        map.put("maxLongRetries", maxLongRetries);
        return map;
    }

    public String getSecHost() {
        return secHost;
    }

    public String getGateHost() {
        return gateHost;
    }

    public long getInactiveTimeout() {
        return inactiveTimeout;
    }

    public long getSessionTimeout() {
        return sessionTimeout;
    }

    public long getPreferredTimeOfDay() {
        return preferredTimeOfDay;
    }

    public long getCallDistance() {
        return callDistance;
    }

    public long getShortRetryDistance() {
        return shortRetryDistance;
    }

    public long getLongRetryDistance() {
        return longRetryDistance;
    }

    public long getRandomZone() {
        return randomZone;
    }

    public long getControlFlags() {
        return controlFlags;
    }

    public int getSecPort() {
        return secPort;
    }

    public int getGatePort() {
        return gatePort;
    }

    public int getMaxShortRetries() {
        return maxShortRetries;
    }

    public int getMaxLongRetries() {
        return maxLongRetries;
    }
}
