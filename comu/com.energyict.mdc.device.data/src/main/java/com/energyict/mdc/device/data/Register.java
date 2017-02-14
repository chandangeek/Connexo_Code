package com.energyict.mdc.device.data;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.protocol.api.device.BaseRegister;

import aQute.bnd.annotation.ProviderType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

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
@ProviderType
public interface Register<R extends Reading, RS extends RegisterSpec> extends BaseRegister {

    Device getDevice();

    /**
     * Returns this Register's specification.
     *
     * @return the spec
     */
    RS getRegisterSpec();

    /**
     * Gets the list of {@link Reading}s whose timestamp is within the given interval.
     *
     * @param interval the Interval
     * @return The List of Reading
     * @see Reading#getTimeStamp()
     */
    List<R> getReadings(Interval interval);

    /**
     * Gets the {@link Reading} with the specified timestamp.
     *
     * @param timestamp The timestamp of the Reading
     * @return The Reading
     * @see Reading#getTimeStamp()
     */
    Optional<R> getReading(Instant timestamp);

    Optional<R> getLastReading();

    /**
     * Return the &quot;timestamp&quot of the last reading for this Register.
     *
     * @return the <code>RegisterReading</code>
     * @see #getLastReading()
     */
    Optional<Instant> getLastReadingDate();

    ReadingType getReadingType();

    RegisterDataUpdater startEditingData();

    /**
     * Returns the readingtype of the calculated value.
     * <ul>
     * <li>Either the delta if the readingType was a bulk and no multiplier was provided</li>
     * <li>Or the multiplied readingType if a multiplier was provided</li>
     * </ul>
     * Depending on the timeStamp a different ReadingType can be provided
     *
     * @param timeStamp the timeStamp for which we want the calculated readingType
     * @return the calculated ReadingType
     */
    Optional<ReadingType> getCalculatedReadingType(Instant timeStamp);

    /**
     * Provides the value of the multiplier of this register at the given timestamp. The value will only be present if
     * the multiplier is larger than one (1)
     *
     * @return the optional multiplier
     */
    Optional<BigDecimal> getMultiplier(Instant timeStamp);

    boolean hasData();

    /**
     * Indicates whether this register has an eventDate related to its value
     *
     * @return true if this register has an eventDate
     */
    boolean hasEventDate();

    /**
     * Indicates whether this register is cumulative
     *
     * @return true if this register contains cumulative values
     */
    boolean isCumulative();

    /**
     * Indicats whether this register has a Billing ReadingType
     *
     * @return true if the readingtype of this register has a macroperiod billing
     */
    boolean isBilling();

    interface RegisterUpdater {
        RegisterUpdater setNumberOfFractionDigits(Integer overruledNbrOfFractionDigits);

        RegisterUpdater setOverflowValue(BigDecimal overruledOverflowValue);

        RegisterUpdater setObisCode(ObisCode overruledObisCode);

        void update();
    }
}