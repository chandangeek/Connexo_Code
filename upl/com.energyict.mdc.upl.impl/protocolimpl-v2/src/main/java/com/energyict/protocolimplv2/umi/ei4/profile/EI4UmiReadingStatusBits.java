package com.energyict.protocolimplv2.umi.ei4.profile;

public enum EI4UmiReadingStatusBits {
    /**
     * There was a clock sync (ie, adjustment less than 18s) during this interval.
     */
    CLOCK_SYNC(0),

    /**
     * There was a clock set (ie, adjustment more than 18s) during this interval.
     */
    CLOCK_SET(1),

    /**
     * This interval log entry was generated automatically because of a time advance as a result of a clock set.
     */
    ESTIMATED_VALUE(2),

    /**
     * There was an invalid volume increment in this.interval. The excess is rolled over to the next interval.
     */
    INVALID_INTERVAL_DATA(3),

    /**
     * The current tariff structure in force during this interval was invalid.
     */
    INVALID_TARIFF_STRUCTURE(4),

    /**
     * A software restart occurred during this interval.
     */
    SOFTWARE_RESTART(5),

    /**
     * The datetime of the interval may be incorrect (a clock sync or set is needed to initialise the clock).
     */
    INCORRECT_DATETIME_INTERVAL(6);

    private final int bitNumber;
    public static EI4UmiReadingStatusBits[] readingStatusBits = EI4UmiReadingStatusBits.values();

    EI4UmiReadingStatusBits(final int bitNumber) {
        this.bitNumber = bitNumber;
    }

    public static EI4UmiReadingStatusBits fromBitNumber(int bitNumber) throws NoSuchFieldException {
        for (int index = 0; index < readingStatusBits.length; ++index) {
            EI4UmiReadingStatusBits current = readingStatusBits[index];
            if (current.bitNumber == bitNumber) {
                return current;
            }
        }
        throw new NoSuchFieldException("EI4UmiReadingStatusBits: " + bitNumber + " not found.");
    }

    public int getBitNumber() {
        return bitNumber;
    }
}
