package com.elster.jupiter.metering.readings.beans;

import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.ProfileStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Our default implementation of an {@link IntervalReading}.
 * An IntervalReading serves as a single <i>entry</i> for a series of readings
 * <p/>
 * Copyrights EnergyICT
 * Date: 25/11/13
 * Time: 15:18
 */
public class IntervalReadingImpl extends BaseReadingImpl implements IntervalReading {

    private ProfileStatus profileStatus;

    private IntervalReadingImpl(Instant timeStamp, BigDecimal value, ProfileStatus profileStatus) {
        super(timeStamp, value);
        this.profileStatus = profileStatus;
    }

    public static IntervalReadingImpl of(Instant timeStamp, BigDecimal value, ProfileStatus profileStatus) {
		return new IntervalReadingImpl(timeStamp, value, profileStatus);
	}

    /**
     * This method has been set as deprecated, whenever possible please use method
     * {@link #of(Instant, BigDecimal, ProfileStatus)} to ensure the ProfileStatus is passed on.
     */
    @Deprecated
    public static IntervalReadingImpl of(Instant timeStamp, BigDecimal value) {
    	return of(timeStamp, value, ProfileStatus.of());
    }
    
    @Override
    public ProfileStatus getProfileStatus() {
    	return profileStatus;
    }

    public void setProfileStatus(ProfileStatus profileStatus) {
        this.profileStatus = profileStatus;
    }
}
