package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.upl.offline.DeviceOfflineFlags;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Implementation for a {@link com.energyict.mdc.tasks.BasicCheckTask}.
 *
 * @author gna
 * @since 23/04/12 - 14:08
 */
class BasicCheckTaskImpl extends ProtocolTaskImpl implements BasicCheckTask, PersistenceAware {

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
    BasicCheckTaskImpl(DataModel dataModel) {
        super(dataModel);
        setFlags(FLAGS);
    }

    @Override
    public void postLoad() {
        this.maximumClockDifference = this.postLoad(this.maximumClockDifference);
    }

    @Override
    void deleteDependents() {
        // currently no dependents to delete
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
    public Optional<TimeDuration> getMaximumClockDifference() {
        return Optional.ofNullable(this.maximumClockDifference);
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