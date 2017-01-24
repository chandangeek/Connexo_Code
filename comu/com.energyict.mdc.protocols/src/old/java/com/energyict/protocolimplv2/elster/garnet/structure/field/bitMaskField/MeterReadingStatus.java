package com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField;

import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractBitMaskField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

import java.util.BitSet;

/**
 * @author sva
 * @since 23/05/2014 - 15:56
 */
public class MeterReadingStatus extends AbstractBitMaskField<MeterReadingStatus> {

    public static final int LENGTH = 1; // The length expressed in nr of bits

    private BitSet readingStatusMask;
    private int readingStatusCode;
    private ReadingStatus readingStatus;

    public MeterReadingStatus() {
        this.readingStatusMask = new BitSet(LENGTH);
        this.readingStatus = ReadingStatus.UNKNOWN;
    }

    public MeterReadingStatus(ReadingStatus readingStatus) {
        this.readingStatus = readingStatus;
    }

    public BitSet getBitMask() {
        return readingStatusMask;
    }

    @Override
    public MeterReadingStatus parse(BitSet bitSet, int posInMask) throws ParsingException {
        int startPos = posInMask * LENGTH;
        readingStatusMask = bitSet.get(startPos, startPos + LENGTH);
        readingStatusCode = convertBitSetToInt(readingStatusMask);
        readingStatus = ReadingStatus.fromStatusCode(readingStatusCode);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getReadingStatusCode() {
        return readingStatusCode;
    }

    public String getReadingStatusInfo() {
        if (!this.readingStatus.equals(ReadingStatus.UNKNOWN)) {
            return readingStatus.getReadingStatus();
        } else {
            return (readingStatus.getReadingStatus() + " " + readingStatus);
        }
    }

    private enum ReadingStatus {
        NO_COMMUNICATION(0, "No communication"),
        COMMUNICATING(1, "Communicating"),
        UNKNOWN(-1, "Unknown reading status");

        private final int statusCode;
        private final String statusInfo;

        private ReadingStatus(int statusCode, String statusInfo) {
            this.statusCode = statusCode;
            this.statusInfo = statusInfo;
        }

        public String getReadingStatus() {
            return statusInfo;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public static ReadingStatus fromStatusCode(int statusCode) {
            for (ReadingStatus version : ReadingStatus.values()) {
                if (version.getStatusCode() == statusCode) {
                    return version;
                }
            }
            return ReadingStatus.UNKNOWN;
        }
    }
}