package com.energyict.protocolimplv2.umi.ei4.structures;

import com.energyict.protocolimplv2.umi.types.UmiCode;
import com.energyict.protocolimplv2.umi.util.LittleEndianData;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class UmiGsmStdStatus extends LittleEndianData {
    public static final int SIZE = 274;
    public static final UmiCode UNI_GSM_STD_STATUS = new UmiCode("umi.1.1.194.2");

    /**
     * Contains the International Mobile Subscriber Identity (IMSI), which is assigned to and stored in a SIM card
     */
    private String subscriberId;         // 32 bytes

    /**
     * Contains the identifier/name of the GSM/GPRS modem model, e.g. BGS2-E or WS6318
     * (provided by the modem-chip e.g. via the "AT+CGMM"-command)
     */
    private String modemModel;           // 16 bytes

    /**
     * Contains the firmware revision number of the GSM/GPRS modem, e.g. 01.301
     */

    private String modemRevision;       // 32 bytes

    /**
     * Contains the firmware version number of the GSM/GPRS modem, e.g. 1.02.13
     */
    private String modemFirmware;       // 32 bytes

    /**
     * Contains the International Mobile Equipment Identity (IMEI)
     */
    private String modemSerial;         // 32 bytes

    /**
     * Contains the identifier/name of the current provider, e.g. Telekom.de
     */
    private String provider;            // 32 bytes

    /**
     * Contains the local IP address of the GSM/GPRS modem in case of a successfully established TCP connection
     */
    private String localIpAddr;             // 16 bytes

    /**
     * Contains the time stamp of the last error detected, coded in UTC format
     */
    private Instant lastErrorTime;         // 4 bytes

    /**
     * Contains the message of the last error detected, shown in plain text. Not mapped, yet.
     */
    private String lastErrorMsg;        // 32 bytes

    /**
     * Contains the received signal strength indicator (RSSI):
     * '-113' dBm or less		:0
     * '-111' dBm			:1
     * '-109' ... '-53' dBm		:2...30
     * '-51' dBm or greater		:31
     * 'not known or not detectable'	:99
     */
    private int rssi;                       // 2 bytes

    /**
     * Contains the bit error rate (BER).
     * 0...7: as RXQUAL values in the table in GSM 05.08 section 8.2.4.;
     * 99: not known or not detectable.
     * Note: This attribute is usually 99 and only occasionally 0. Thus, this attribute can hardly be used
     * Not in use in this product.
     */
    private int ber;                        // 2 bytes

    /**
     * Not in use in this product.
     */
    private int batteryVoltage;             // 2 bytes

    /**
     * Not in use in this product.
     */
    private int statusFlags;                    // 2 bytes

    /**
     * Contains the code of the last error detected.
     */
    private int lastErrorCode;              // 2 bytes

    /**
     * Contains the last CME error code.
     */
    private int cmeError;                   // 2 bytes

    /**
     * Contains the last state reached during the last call try.
     * Not in use in this product.
     */
    private int lastState;                  // 2 bytes

    /**
     * Contains the Integrated Circuit Card Identifier (ICCID), which is assigned to and stored in a SIM card.
     */
    private String iccid;                   // 32 bytes

    public UmiGsmStdStatus(byte[] rawData) {
        super(rawData, SIZE, false);
        byte[] subscriberIdBytes = new byte[32];
        getRawBuffer().get(subscriberIdBytes);
        this.subscriberId = String.copyValueOf(UmiHelper.convertBytesToChars(subscriberIdBytes)).trim();

        byte[] modemModelBytes = new byte[16];
        getRawBuffer().get(modemModelBytes);
        this.modemModel = String.copyValueOf(UmiHelper.convertBytesToChars(modemModelBytes)).trim();

        byte[] modemRevisionBytes = new byte[32];
        getRawBuffer().get(modemRevisionBytes);
        this.modemRevision = String.copyValueOf(UmiHelper.convertBytesToChars(modemRevisionBytes)).trim();

        byte[] modemFirmwareBytes = new byte[32];
        getRawBuffer().get(modemFirmwareBytes);
        this.modemFirmware = String.copyValueOf(UmiHelper.convertBytesToChars(modemFirmwareBytes)).trim();

        byte[] modemSerialBytes = new byte[32];
        getRawBuffer().get(modemSerialBytes);
        this.modemSerial = String.copyValueOf(UmiHelper.convertBytesToChars(modemSerialBytes)).trim();

        byte[] providerBytes = new byte[32];
        getRawBuffer().get(providerBytes);
        this.provider = String.copyValueOf(UmiHelper.convertBytesToChars(providerBytes)).trim();

        byte[] localIpAddrBytes = new byte[16];
        getRawBuffer().get(localIpAddrBytes);
        this.localIpAddr = String.copyValueOf(UmiHelper.convertBytesToChars(localIpAddrBytes)).trim();

        this.lastErrorTime = UmiHelper.convertToInstantFromUmiFormat(Integer.toUnsignedLong(getRawBuffer().getInt()));

        byte[] lastErrorMsgBytes = new byte[32];
        getRawBuffer().get(lastErrorMsgBytes);
        this.lastErrorMsg = String.copyValueOf(UmiHelper.convertBytesToChars(lastErrorMsgBytes)).trim();

        this.rssi = Short.toUnsignedInt(getRawBuffer().getShort());
        this.ber = Short.toUnsignedInt(getRawBuffer().getShort());
        this.batteryVoltage = Short.toUnsignedInt(getRawBuffer().getShort());
        this.statusFlags = Short.toUnsignedInt(getRawBuffer().getShort());
        this.lastErrorCode = Short.toUnsignedInt(getRawBuffer().getShort());
        this.cmeError = Short.toUnsignedInt(getRawBuffer().getShort());
        this.lastState = Short.toUnsignedInt(getRawBuffer().getShort());

        byte[] iccidBytes = new byte[32];
        getRawBuffer().get(iccidBytes);
        this.iccid = String.copyValueOf(UmiHelper.convertBytesToChars(iccidBytes)).trim();
    }

    public UmiGsmStdStatus(String subscriberId, String modemModel, String modemRevision, String modemFirmware, String modemSerial, String provider,
                           String localIpAddr, Instant lastErrorTime, String lastErrorMsg, int rssi, int ber, int batteryVoltage,
                           int statusFlags, int lastErrorCode, int cmeError, int lastState, String iccid) {
        super(SIZE);
        this.subscriberId = subscriberId;
        this.modemModel = modemModel;
        this.modemRevision = modemRevision;
        this.modemFirmware = modemFirmware;
        this.modemSerial = modemSerial;
        this.provider = provider;
        this.localIpAddr = localIpAddr;
        this.lastErrorTime = lastErrorTime;
        this.lastErrorMsg = lastErrorMsg;
        this.rssi = rssi;
        this.ber = ber;
        this.batteryVoltage = batteryVoltage;
        this.statusFlags = statusFlags;
        this.lastErrorCode = lastErrorCode;
        this.cmeError = cmeError;
        this.lastState = lastState;
        this.iccid = iccid;

        getRawBuffer().put(this.subscriberId.getBytes(StandardCharsets.US_ASCII))
                .put(this.modemModel.getBytes(StandardCharsets.US_ASCII))
                .put(this.modemRevision.getBytes(StandardCharsets.US_ASCII))
                .put(this.modemFirmware.getBytes(StandardCharsets.US_ASCII))
                .put(this.modemSerial.getBytes(StandardCharsets.US_ASCII))
                .put(this.provider.getBytes(StandardCharsets.US_ASCII))
                .put(this.localIpAddr.getBytes(StandardCharsets.US_ASCII))
                .putInt((int) UmiHelper.convertToUmiFormatFromInstant(this.lastErrorTime))
                .put(this.lastErrorMsg.getBytes(StandardCharsets.US_ASCII))
                .putShort((short) this.rssi)
                .putShort((short) this.ber)
                .putShort((short) this.batteryVoltage)
                .putShort((short) this.statusFlags)
                .putShort((short) this.lastErrorCode)
                .putShort((short) this.cmeError)
                .putShort((short) this.lastState)
                .put(this.iccid.getBytes(StandardCharsets.US_ASCII));
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("subscriberId", subscriberId);
        map.put("modemModel", modemModel);
        map.put("modemRevision", modemRevision);
        map.put("modemFirmware", modemFirmware);
        map.put("modemSerial", modemSerial);
        map.put("provider", provider);
        map.put("localIpAddr", localIpAddr);
        map.put("lastErrorTime", lastErrorTime);
        map.put("lastErrorMsg", lastErrorMsg);
        map.put("rssi", rssi);
        map.put("ber", ber);
        map.put("batteryVoltage", batteryVoltage);
        map.put("statusFlags", statusFlags);
        map.put("lastErrorCode", lastErrorCode);
        map.put("cmeError", cmeError);
        map.put("lastState", lastState);
        map.put("iccid", iccid);

        return map;
    }

}
