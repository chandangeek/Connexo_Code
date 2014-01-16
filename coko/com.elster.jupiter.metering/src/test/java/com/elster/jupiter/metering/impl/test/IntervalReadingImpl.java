package com.elster.jupiter.metering.impl.test;

import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.ProfileStatus;

import java.math.BigDecimal;
import java.util.Date;

public class IntervalReadingImpl extends BaseReadingImpl implements IntervalReading {
	private final ProfileStatus profileStatus;

    public IntervalReadingImpl(Date timeStamp, BigDecimal value) {
        this(timeStamp, value,ProfileStatus.of());
    }
    
    public IntervalReadingImpl(Date timeStamp, BigDecimal value, ProfileStatus profileStatus) {
        super(timeStamp, value);
        this.profileStatus = profileStatus;
    }

	@Override
	public ProfileStatus getProfileStatus() {
		return profileStatus;
	}
}
