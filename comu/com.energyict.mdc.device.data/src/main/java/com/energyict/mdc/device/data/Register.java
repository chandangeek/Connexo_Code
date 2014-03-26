package com.energyict.mdc.device.data;

import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.protocol.api.device.BaseRegister;

import java.util.List;

/**
 * Defines the non-persistent representation of a Register.
 * A <i>Register</i> for a {@link Device} will be a wrapper around
 * the {@link com.energyict.mdc.device.config.RegisterSpec} of
 * the {@link com.energyict.mdc.device.config.DeviceConfiguration}
 * of the {@link Device}
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/03/14
 * Time: 10:32
 */
public interface Register extends BaseRegister {

    Device getDevice();

    /**
     * Returns the register's specification object
     *
     * @return the spec
     */
    RegisterSpec getRegisterSpec();

    /**
     * Gets a list of ReadingRecords, filterd by the given interval
     * @param interval
     * @return
     */
    List<ReadingRecord> getRegisterReadings(Interval interval);

}
