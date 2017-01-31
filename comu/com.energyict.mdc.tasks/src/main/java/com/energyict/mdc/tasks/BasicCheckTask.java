/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks;

import com.elster.jupiter.time.TimeDuration;

import java.util.Optional;

/**
 * Models the {@link com.energyict.mdc.tasks.ProtocolTask} which serves basic functionality.
 * <p>
 * If the serialNumber should be checked, then the Communication with the Device will stop if it does not match.
 * </p>
 *
 * @author gna
 * @since 19/04/12 - 16:12
 */
public interface BasicCheckTask extends ProtocolTask {

    /**
     * Indication whether to check the clockDifference
     *
     * @return true if the clockDifference should be checked, false otherwise
     */
    public boolean verifyClockDifference();
    public void setVerifyClockDifference(boolean verifyClockDifference);

    /**
     * Getter for the maximum allowed clock difference.
     *
     * @return a {@link TimeDuration} containing the maximum allowed clock difference
     */
    public Optional<TimeDuration> getMaximumClockDifference();
    public void setMaximumClockDifference(TimeDuration maximumClockDifference);

    /**
     * Indication whether to verify the serialNumber
     *
     * @return true if the serialNumber should be checked, false otherwise
     */
    public boolean verifySerialNumber();
    public void setVerifySerialNumber(boolean verifySerialNumber);


    interface BasicCheckTaskBuilder {
        public BasicCheckTaskBuilder verifyClockDifference(boolean verifyClockDifference);
        public BasicCheckTaskBuilder maximumClockDifference(TimeDuration maximumClockDifference);
        public BasicCheckTaskBuilder verifySerialNumber(boolean verifySerialNumber);
        public BasicCheckTask add();
    }
}