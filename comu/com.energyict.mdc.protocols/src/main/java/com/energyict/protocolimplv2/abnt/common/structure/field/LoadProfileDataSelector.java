package com.energyict.protocolimplv2.abnt.common.structure.field;

/**
 * @author sva
 * @since 1/09/2014 - 9:31
 */
public class LoadProfileDataSelector {

    public enum ReadSizeArgument {
        READ_ALL_BLOCK(0),
        HOURS_TO_READ(1),   // Warning: should not be used, cause doesn't handle time gaps correct
        DAYS_TO_READ(2),    // Warning: should not be used, cause doesn't handle time gaps correct
        UNKNOWN(-1);

        private final int statusCode;

        private ReadSizeArgument(int statusCode) {
            this.statusCode = statusCode;
        }

        public int getCode() {
            return statusCode;
        }
    }

    private final int blockCount;
    private final ReadSizeArgument readSizeArgument;

    public static LoadProfileDataSelector newHoursToReadDataSelector(int nrOfHours) {
        return new LoadProfileDataSelector(nrOfHours, ReadSizeArgument.HOURS_TO_READ);
    }

    public static LoadProfileDataSelector newDaysToReadDataSelector(int nrOfDays) {
        return new LoadProfileDataSelector(nrOfDays, ReadSizeArgument.DAYS_TO_READ);
    }

    public static LoadProfileDataSelector newFullProfileDataSelector() {
        return new LoadProfileDataSelector(0, ReadSizeArgument.READ_ALL_BLOCK);
    }

    /**
     * Utility class constructor is made private, objects should be created using the static methods;
     */
    private LoadProfileDataSelector(int blockCount, ReadSizeArgument readSizeArgument) {
        this.blockCount = blockCount;
        this.readSizeArgument = readSizeArgument;
    }

    public int getBlockCount() {
        return blockCount;
    }

    public int getReadSizeArgument() {
        return readSizeArgument.getCode();
    }
}