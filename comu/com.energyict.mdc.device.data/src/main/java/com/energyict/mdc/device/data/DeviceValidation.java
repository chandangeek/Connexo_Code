package com.energyict.mdc.device.data;

import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationResult;
import com.google.common.base.Optional;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by tgr on 9/09/2014.
 */
public interface DeviceValidation {

    Device getDevice();

    public ValidationResult getValidationResult(Collection<? extends ReadingQuality> qualities);

    boolean isValidationActive();

    boolean isValidationActive(Channel channel, Date when);

    boolean isValidationActive(Register<?> register, Date when);

    boolean allDataValidated(Channel channel, Date when);

    boolean allDataValidated(Register<?> register, Date when);

    Optional<Date> getLastChecked();

    Optional<Date> getLastChecked(Channel channel);

    Optional<Date> getLastChecked(Register<?> register);

    List<DataValidationStatus> getValidationStatus(Channel channel, List<? extends BaseReading> readings, Interval interval);

    List<DataValidationStatus> getValidationStatus(Register<?> register, List<? extends BaseReading> readings, Interval interval);

    List<DataValidationStatus> getValidationStatus(Channel channel, List<? extends BaseReading> readings);

    List<DataValidationStatus> getValidationStatus(Register<?> register, List<? extends BaseReading> readings);

    void validateLoadProfile(LoadProfile loadProfile, Date start, Date until); // TODO : interval

    void validateChannel(Channel channel, Date start, Date until);

    void validateRegister(Register<?> register, Date start, Date until);

    boolean hasData(Channel c);

    boolean hasData(Register<?> register);

    void setLastChecked(Channel c, Date start);

    void setLastChecked(Register<?> c, Date start);
}
