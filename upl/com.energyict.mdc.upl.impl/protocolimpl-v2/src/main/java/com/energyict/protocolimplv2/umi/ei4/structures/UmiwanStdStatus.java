package com.energyict.protocolimplv2.umi.ei4.structures;

import com.energyict.protocolimplv2.umi.types.UmiCode;
import com.energyict.protocolimplv2.umi.util.LittleEndianData;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class UmiwanStdStatus extends LittleEndianData {
    public static final int SIZE = 48;
    public static final UmiCode UMIWAN_STD_STATUS_UMI_CODE = new UmiCode("umi.1.1.194.13");

    /**
     * point in time for next scheduled call
     */
    private Instant nextCall;          // 4 bytes

    /**
     * point in time in the past of a call attempt
     */
    private Instant lastTry;           // 4 bytes

    /**
     * point in time of the last successful call
     */
    private Instant lastCall;          // 4 bytes

    /**
     * duration in seconds for the last call
     */
    private int lastDuration;       // 2 bytes

    /**
     * Actual Counter of immediate retries.
     * Max number is configured in 'maxShortRetries' in 1.1.194.11. (UmiwanConfiguration)
     * Distance to next immediate retry is configured in 'shortRetryDistance' in 1.1.194.11
     */
    private int shortRetryCtr;      // 2 bytes

    /**
     * Number of retry sequences with a longer distance in between.
     * Max number is configured in 'maxLongRetries' in 1.1.194.11 and
     * Distance is configured in 'longRetryDistance' in 1.1.194.11
     */
    private int longRetryCtr;       // 2 bytes

    /**
     * Cumulative counter for unsuccessful calls
     */
    private int allFailureCtr;      // 2 bytes

    /**
     * Cumulative number of call retries
     */
    private int allRetryCtr;        // 2 bytes

    /**
     * Cumulative number of successful calls
     */
    private int allSuccessCtr;      // 2 bytes

    /**
     * Cumulative number of all first call retries
     */
    private int retryCtr1;          // 2 bytes

    /**
     * Cumulative number of all second call retries
     */
    private int retryCtr2;          // 2 bytes

    /**
     * Cumulative number of all third call retries
     */
    private int retryCtr3;          // 2 bytes

    /**
     * Cumulative number of all fourth call retries
     */
    private int retryCtr4;          // 2 bytes

    /**
     * Not in use in this product.
     */
    private int errorCode;          // 2 bytes

    /**
     * Reason of ongoing call.
     * 1: manually triggered call
     * 4: cyclic call
     * 6: cyclic retry
     */
    private int retryType;          // 2 bytes

    /**
     * bit 8: autocommission active
     * bit 9: autocommission failed
     * bit10: autocommission done
     * To reset autocommission function, bits 8, 9, 10 must be set to 0
     */
    private long statusFlags;       // 4 bytes

    /**
     * For Elster internal use only
     */
    private long umiEmcReadFailCtr; // 4 bytes


    /**
     * For Elster internal use only
     */
    private long umiEmcReadPassCtr; // 4 bytes

    public UmiwanStdStatus(byte[] rawData) {
        super(rawData, SIZE, false);

        this.nextCall = UmiHelper.convertToInstantFromUmiFormat(Integer.toUnsignedLong(getRawBuffer().getInt()));
        this.lastTry = UmiHelper.convertToInstantFromUmiFormat(Integer.toUnsignedLong(getRawBuffer().getInt()));
        this.lastCall = UmiHelper.convertToInstantFromUmiFormat(Integer.toUnsignedLong(getRawBuffer().getInt()));
        this.lastDuration = Short.toUnsignedInt(getRawBuffer().getShort());
        this.shortRetryCtr = Short.toUnsignedInt(getRawBuffer().getShort());
        this.longRetryCtr = Short.toUnsignedInt(getRawBuffer().getShort());
        this.allFailureCtr = Short.toUnsignedInt(getRawBuffer().getShort());
        this.allRetryCtr = Short.toUnsignedInt(getRawBuffer().getShort());
        this.allSuccessCtr = Short.toUnsignedInt(getRawBuffer().getShort());
        this.retryCtr1 = Short.toUnsignedInt(getRawBuffer().getShort());
        this.retryCtr2 = Short.toUnsignedInt(getRawBuffer().getShort());
        this.retryCtr3 = Short.toUnsignedInt(getRawBuffer().getShort());
        this.retryCtr4 = Short.toUnsignedInt(getRawBuffer().getShort());
        this.errorCode = Short.toUnsignedInt(getRawBuffer().getShort());
        this.retryType = Short.toUnsignedInt(getRawBuffer().getShort());

        this.statusFlags = Integer.toUnsignedLong(getRawBuffer().getInt());
        this.umiEmcReadFailCtr = Integer.toUnsignedLong(getRawBuffer().getInt());
        this.umiEmcReadPassCtr = Integer.toUnsignedLong(getRawBuffer().getInt());
    }

    public UmiwanStdStatus(Instant nextCall, Instant lastTry, Instant lastCall, int lastDuration, int shortRetryCtr, int longRetryCtr,
                           int allFailureCtr, int allRetryCtr, int allSuccessCtr, int retryCtr1, int retryCtr2,
                           int retryCtr3, int retryCtr4, int errorCode, int retryType, long statusFlags,
                           long umiEmcReadFailCtr, long umiEmcReadPassCtr) {
        super(SIZE);
        this.nextCall = nextCall;
        this.lastTry = lastTry;
        this.lastCall = lastCall;
        this.lastDuration = lastDuration;
        this.shortRetryCtr = shortRetryCtr;
        this.longRetryCtr = longRetryCtr;
        this.allFailureCtr = allFailureCtr;
        this.allRetryCtr = allRetryCtr;
        this.allSuccessCtr = allSuccessCtr;
        this.retryCtr1 = retryCtr1;
        this.retryCtr2 = retryCtr2;
        this.retryCtr3 = retryCtr3;
        this.retryCtr4 = retryCtr4;
        this.errorCode = errorCode;
        this.retryType = retryType;

        this.statusFlags = statusFlags;
        this.umiEmcReadFailCtr = umiEmcReadFailCtr;
        this.umiEmcReadPassCtr = umiEmcReadPassCtr;

    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("nextCall", nextCall);
        map.put("lastTry", lastTry);
        map.put("lastCall", lastCall);
        map.put("lastDuration", lastDuration);
        map.put("shortRetryCtr", shortRetryCtr);
        map.put("longRetryCtr", longRetryCtr);
        map.put("allFailureCtr", allFailureCtr);
        map.put("allRetryCtr", allRetryCtr);
        map.put("allSuccessCtr", allSuccessCtr);
        map.put("retryCtr1", retryCtr1);
        map.put("retryCtr2", retryCtr2);
        map.put("retryCtr3", retryCtr3);
        map.put("retryCtr4", retryCtr4);
        map.put("errorCode", errorCode);
        map.put("retryType", retryType);
        map.put("statusFlags", statusFlags);
        map.put("umiEmcReadFailCtr", umiEmcReadFailCtr);
        map.put("umiEmcReadPassCtr", umiEmcReadPassCtr);
        return map;
    }

    public Instant getNextCall() {
        return nextCall;
    }

    public Instant getLastTry() {
        return lastTry;
    }

    public Instant getLastCall() {
        return lastCall;
    }

    public int getLastDuration() {
        return lastDuration;
    }

    public int getShortRetryCtr() {
        return shortRetryCtr;
    }

    public int getLongRetryCtr() {
        return longRetryCtr;
    }

    public int getAllFailureCtr() {
        return allFailureCtr;
    }

    public int getAllRetryCtr() {
        return allRetryCtr;
    }

    public int getAllSuccessCtr() {
        return allSuccessCtr;
    }

    public int getRetryCtr1() {
        return retryCtr1;
    }

    public int getRetryCtr2() {
        return retryCtr2;
    }

    public int getRetryCtr3() {
        return retryCtr3;
    }

    public int getRetryCtr4() {
        return retryCtr4;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public int getRetryType() {
        return retryType;
    }

    public long getStatusFlags() {
        return statusFlags;
    }

    public long getUmiEmcReadFailCtr() {
        return umiEmcReadFailCtr;
    }

    public long getUmiEmcReadPassCtr() {
        return umiEmcReadPassCtr;
    }


}
