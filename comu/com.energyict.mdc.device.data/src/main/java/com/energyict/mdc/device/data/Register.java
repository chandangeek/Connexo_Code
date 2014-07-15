package com.energyict.mdc.device.data;

import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.protocol.api.device.BaseRegister;

import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;

import java.util.Date;
import java.util.List;

/**
 * Defines the non-persistent representation of a Register.
 * A <i>Register</i> for a {@link Device} will be a wrapper around
 * the {@link com.energyict.mdc.device.config.RegisterSpec} of
 * the {@link com.energyict.mdc.device.config.DeviceConfiguration}
 * of the {@link Device} and the actual storage area
 * provided by the Jupiter Kore metering bundle.
 * <p/>
 * The following types of registers are currently identified
 * and will have their proper interface:
 * <ul>
 * <li>Numerical: stores strictly numerical data</li>
 * <li>Event based: stores numerical data as a result of an event that occurred in the Device.</li>
 * <li>Text based: stores alphanumerical data (String)</li>
 * <li>Flags: stores a collection of bit flags</li>
 * </ul>
 * Copyrights EnergyICT
 * Date: 11/03/14
 * Time: 10:32
 */
public interface Register<R extends Reading> extends BaseRegister {

    public Device getDevice();

    /**
     * Returns this Register's specification.
     *
     * @return the spec
     */
    public RegisterSpec getRegisterSpec();

    /**
     * Gets the list of {@link Reading}s whose timestamp is within the given interval.
     *
     * @param interval the Interval
     * @return The List of Reading
     * @see Reading#getTimeStamp()
     */
    public List<R> getReadings(Interval interval);

    public Optional<R> getLastReading();

    /**
     * Return the &quot;timestamp&quot of the last reading for this Register.
     *
     * @return the <code>RegisterReading</code>
     * @see #getLastReading()
     */
    public Optional<Date> getLastReadingDate();

}