/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.config.DeviceType;

import java.time.Instant;
import java.util.Map;

public interface TimeOfUseCampaign {

    String getName();

    DeviceType getDeviceType();

    String getDeviceGroup();

    Instant getActivationStart();

    Instant getActivationEnd();

    Calendar getCalendar();

    String getActivationOption();

    Instant getActivationDate();

    String getUpdateType();

    ServiceCall getServiceCall();

    Map<DefaultState, Long> getNumbersOfChildrenWithStatuses();

    void cancel();

    void delete();

    void edit(String name, Instant start, Instant end);

    long getValidationTimeout();

    long getVersion();

    long getId();

}
