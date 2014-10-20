package com.energyict.mdc.device.data;

import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationResult;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Created by tgr on 9/09/2014.
 */
public interface DeviceValidation {

    Device getDevice();

    public ValidationResult getValidationResult(Collection<? extends ReadingQuality> qualities);

    boolean isValidationActive();

    boolean isValidationActive(Channel channel, Instant when);

    boolean isValidationActive(Register<?> register, Instant when);

    boolean allDataValidated(Channel channel, Instant when);

    boolean allDataValidated(Register<?> register, Instant when);

    Optional<Instant> getLastChecked();

    Optional<Instant> getLastChecked(Channel channel);

    Optional<Instant> getLastChecked(Register<?> register);

    List<DataValidationStatus> getValidationStatus(Channel channel, List<? extends BaseReading> readings, Range<Instant> interval);

    List<DataValidationStatus> getValidationStatus(Register<?> register, List<? extends BaseReading> readings, Range<Instant> interval);

    List<DataValidationStatus> getValidationStatus(Channel channel, List<? extends BaseReading> readings);

    List<DataValidationStatus> getValidationStatus(Register<?> register, List<? extends BaseReading> readings);

    void validateLoadProfile(LoadProfile loadProfile, Instant start, Instant until); // TODO : interval

    void validateChannel(Channel channel, Instant start, Instant until);

    void validateRegister(Register<?> register, Instant start, Instant until);

    boolean hasData(Channel c);

    boolean hasData(Register<?> register);

    void setLastChecked(Channel c, Instant start);

    void setLastChecked(Register<?> c, Instant start);
}
