package com.energyict.mdc.device.data;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.validation.DataValidationStatus;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Created by bvn on 8/1/14.
 */
@ProviderType
public interface LoadProfileReading {

    Range<Instant> getRange();

    Map<Channel, IntervalReadingRecord> getChannelValues();

    Map<Channel, DataValidationStatus> getChannelValidationStates();

    Instant getReadingTime();

    void setFlags(List<ProfileStatus.Flag> flags);

    List<ProfileStatus.Flag> getFlags();

}