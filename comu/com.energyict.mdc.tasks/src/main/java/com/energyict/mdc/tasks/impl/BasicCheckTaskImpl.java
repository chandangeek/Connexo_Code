package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags;
import com.energyict.mdc.tasks.BasicCheckTask;
import javax.inject.Inject;

/**
 * Implementation for a {@link com.energyict.mdc.tasks.BasicCheckTask}.
 *
 * @author gna
 * @since 23/04/12 - 14:08
 */
class BasicCheckTaskImpl extends ProtocolTaskImpl implements BasicCheckTask {

    private static final DeviceOfflineFlags FLAGS = new DeviceOfflineFlags();

    enum Fields {
        VERIFY_CLOCK_DIFFERENCE("verifyClockDifference"),
        VERIFY_SERIAL_NUMBER("verifySerialNumber"),
        MAXIMUM_CLOCK_DIFFERENCE("maximumClockDifference"),;
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    private boolean verifyClockDifference;
    private boolean verifySerialNumber;
    private TimeDuration maximumClockDifference;

    @Inject
    public BasicCheckTaskImpl(DataModel dataModel) {
        super(dataModel);
        setFlags(FLAGS);
    }

    @Override
    public boolean verifyClockDifference() {
        return verifyClockDifference;
    }

    @Override
    public boolean verifySerialNumber() {
        return verifySerialNumber;
    }

    @Override
    public TimeDuration getMaximumClockDifference() {
        return maximumClockDifference;
    }

    @Override
    public void setVerifyClockDifference(boolean verifyClockDifference) {
        this.verifyClockDifference = verifyClockDifference;
    }

    @Override
    public void setVerifySerialNumber(boolean verifySerialNumber) {
        this.verifySerialNumber = verifySerialNumber;
    }

    @Override
    public void setMaximumClockDifference(TimeDuration maximumClockDifference) {
        this.maximumClockDifference = maximumClockDifference;
    }
}