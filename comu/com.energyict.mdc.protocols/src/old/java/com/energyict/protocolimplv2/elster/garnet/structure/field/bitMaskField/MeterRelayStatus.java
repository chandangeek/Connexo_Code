package com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField;

import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractBitMaskField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

import java.util.BitSet;

/**
 * @author sva
 * @since 23/05/2014 - 15:56
 */
public class MeterRelayStatus extends AbstractBitMaskField<MeterRelayStatus> {

    public static final int LENGTH = 2; // The length expressed in nr of bits

    private BitSet relayStatusMask;
    private int relayStatusCode;
    private RelayStatus relayStatus;

    public MeterRelayStatus() {
        this.relayStatusMask = new BitSet(LENGTH);
        this.relayStatus = RelayStatus.UNKNOWN;
    }

    public MeterRelayStatus(RelayStatus relayStatus) {
        this.relayStatus = relayStatus;
    }

    public BitSet getBitMask() {
        return relayStatusMask;
    }

    @Override
    public MeterRelayStatus parse(BitSet bitSet, int posInMask) throws ParsingException {
        int startPos = posInMask * LENGTH;
        relayStatusMask = bitSet.get(startPos, startPos + LENGTH);
        relayStatusCode = convertBitSetToInt(relayStatusMask);
        relayStatus = RelayStatus.fromStatusCode(relayStatusCode);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getRelayStatusCode() {
        return relayStatusCode;
    }

    public String getRelayStatusInfo() {
        if (!this.relayStatus.equals(RelayStatus.UNKNOWN)) {
            return relayStatus.getRelayInfo();
        } else {
            return (relayStatus.getRelayInfo() + " " + relayStatus);
        }
    }

    private enum RelayStatus {
        SHUTDOWN_BY_COMMAND(0, "Shutdown by command"),
        ON(1, "On"),
        SHUTDOWN_BY_INTRUSION(2, "Shutdown by intrusion"),
        SHUTDOWN_BY_LACK_OF_VOLTAGE(3, "Shutdown by lack of voltage"),
        UNKNOWN(-1, "Unknown relay status");

        private final int statusCode;
        private final String statusInfo;

        private RelayStatus(int statusCode, String statusInfo) {
            this.statusCode = statusCode;
            this.statusInfo = statusInfo;
        }

        public String getRelayInfo() {
            return statusInfo;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public static RelayStatus fromStatusCode(int statusCode) {
            for (RelayStatus version : RelayStatus.values()) {
                if (version.getStatusCode() == statusCode) {
                    return version;
                }
            }
            return RelayStatus.UNKNOWN;
        }
    }
}