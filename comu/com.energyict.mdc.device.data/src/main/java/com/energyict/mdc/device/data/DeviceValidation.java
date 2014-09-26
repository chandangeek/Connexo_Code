package com.energyict.mdc.device.data;

import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
import com.google.common.base.Optional;

import java.util.Date;
import java.util.List;

/**
 * Created by tgr on 9/09/2014.
 */
public interface DeviceValidation {

    Device getDevice();

    boolean isValidationActive(Date when);

    boolean isValidationActive(Channel channel, Date when);

    boolean isValidationActive(Register<?> register, Date when);

    boolean allDataValidated(Channel channel, Date when);

    boolean allDataValidated(Register<?> register, Date when);

    Optional<Date> getLastChecked(Channel c);

    Optional<Date> getLastChecked(Register<?> c);

    List<DataValidationStatus> getValidationStatus(Channel channel, Interval interval);

    void validateLoadProfile(LoadProfile loadProfile, Date start, Date until);

    void validateChannel(Channel channel, Date start, Date until);

    boolean hasData(Channel c);

    boolean hasData(Register<?> register);
}
