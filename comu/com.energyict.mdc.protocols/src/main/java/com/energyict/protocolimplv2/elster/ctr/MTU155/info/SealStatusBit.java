/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.info;

import java.util.ArrayList;
import java.util.List;

public enum SealStatusBit {

    RESERVED_0(0, "Reserved [0]", false),
    EVENT_LOG_RESET(1, "Event log reset [1]"),
    FACTORY_CONDITIONS(2, "Factory conditions [2]"),
    DEFAULT_VALUES(3, "Default values [3]"),
    STATUS_CHANGE(4, "Status change [4]"),
    RESERVED_5(5, "Reserved [5]", false),
    RESERVED_6(6, "Reserved [6]", false),
    RESERVED_7(7, "Reserved [7]", false),
    RESERVED_8(8, "Reserved [8]", false),
    REMOTE_CONFIG_VOLUME(9, "Remote volume configuration seal [9]"),
    REMOTE_CONFIG_ANALYSIS(10, "Remote analysis configuration seal [10]"),
    DOWNLOAD_PROGRAM(11, "Download program [11]"),
    RESTORE_DEFAULT_PASSWORDS(12, "Restore default password [12]"),
    RESERVED_13(13, "Reserved [13]", false),
    RESERVED_14(14, "Reserved [14]", false),
    RESERVED_15(15, "Reserved [15]", false),
    INVALID(16, "Invalid seal!");

    private final int bitNumber;
    private final String description;
    private final boolean realStatusBit;

    /**
     * Private constructor for enum
     *
     * @param bitNumber
     * @param description
     */
    private SealStatusBit(int bitNumber, String description) {
        this(bitNumber, description, true);
    }

    private SealStatusBit(int bitNumber, String description, boolean realStatusBit) {
        this.bitNumber = bitNumber;
        this.description = description;
        this.realStatusBit = realStatusBit;
    }

    /**
     * Getter for the description
     *
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * Getter for the int value of the device status code
     *
     * @return
     */
    public int getBitNumber() {
        return bitNumber;
    }

    /**
     * Getter for the enabled field
     * @return
     */
    public boolean isRealStatusBit() {
        return realStatusBit;
    }

    /**
     * Get a DeviceStatus from a given statusCode. If the status code
     *
     * @param bitNumber
     * @return
     */
    public static SealStatusBit fromBitValue(int bitNumber) {
        for (SealStatusBit status : SealStatusBit.values()) {
            if ((bitNumber & 0x0FF) == status.getBitNumber()) {
                return status;
            }
        }
        return INVALID;
    }

    public static List<SealStatusBit> getIntegerSeals(int sealStatus) {
        List<SealStatusBit> statusBits = new ArrayList<SealStatusBit>();
        for (int i = 0; i <= 15; i++) {
            int bitValue = sealStatus & (1 << i);
            if (bitValue == 0) {
                SealStatusBit statusBit = fromBitValue(i);
                if (statusBit.isRealStatusBit()) {
                    statusBits.add(statusBit);
                }
            }
        }
        return statusBits;
    }

    /**
     * 
     * @param sealStatus
     * @return
     */
    public static String getIntegerSealsDescription(int sealStatus) {
        StringBuilder sb = new StringBuilder();
        List<SealStatusBit> statusBits = getIntegerSeals(sealStatus);
        for (SealStatusBit statusBit : statusBits) {
            sb.append(statusBit.getDescription()).append(", ");
        }
        return sb.toString();
    }

    public boolean isSealBroken(int sealStatus) {
        return ((0x01 << getBitNumber()) & sealStatus) != 0;
    }

    public boolean isSealInteger(int sealStatus) {
        return !isSealBroken(sealStatus);
    }

}
