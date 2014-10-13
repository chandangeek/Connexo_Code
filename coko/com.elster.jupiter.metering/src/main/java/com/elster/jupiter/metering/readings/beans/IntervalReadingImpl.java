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

    public IntervalReadingImpl(Instant timeStamp, BigDecimal value) {
        super(timeStamp, value);
    }

    public IntervalReadingImpl(Instant timeStamp, BigDecimal value, ProfileStatus profileStatus) {
        super(timeStamp, value);
        this.profileStatus = profileStatus;
    }

    @Override
    public ProfileStatus getProfileStatus() {
        if(profileStatus != null){
            return profileStatus;
        } else {
            return ProfileStatus.of();
        }
    }

    public void setProfileStatus(ProfileStatus profileStatus) {
        this.profileStatus = profileStatus;
    }
}
