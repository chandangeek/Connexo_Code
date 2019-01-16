/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign;

import com.elster.jupiter.calendar.Calendar;
import com.energyict.mdc.device.config.DeviceType;

import java.time.Instant;

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

    long getTimeValidation();

    long getId();

}
