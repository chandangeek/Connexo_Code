/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.common.device.config.ConnectionStrategy;
import com.energyict.mdc.common.device.config.DeviceType;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@ProviderType
public interface TimeOfUseCampaign {

    String getName();

    DeviceType getDeviceType();

    String getDeviceGroup();

    Instant getUploadPeriodStart();

    Instant getUploadPeriodEnd();

    Calendar getCalendar();

    String getActivationOption();

    Instant getActivationDate();

    String getUpdateType();

    ServiceCall getServiceCall();

    Map<DefaultState, Long> getNumbersOfChildrenWithStatuses();

    void update();

    void cancel();

    void delete();

    void setName(String name);

    void setUploadPeriodStart(Instant start);

    void setUploadPeriodEnd(Instant end);

    long getValidationTimeout();

    long getVersion();

    long getId();

    boolean isWithUniqueCalendarName();

    long getCalendarUploadComTaskId();

    long getValidationComTaskId();

    void setCalendarUploadComTaskId(long calendarUploadComTaskId);

    void setValidationComTaskId(long validationComTaskId);

    Optional<ConnectionStrategy> getCalendarUploadConnectionStrategy();

    Optional<ConnectionStrategy> getValidationConnectionStrategy();

    void setCalendarUploadConnectionStrategy(ConnectionStrategy calendarUploadConnectionStrategy);

    void setValidationConnectionStrategy(ConnectionStrategy validationConnectionStrategy);
}
