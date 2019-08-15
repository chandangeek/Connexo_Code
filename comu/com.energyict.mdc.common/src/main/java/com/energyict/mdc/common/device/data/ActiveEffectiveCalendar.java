/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.data;

import com.elster.jupiter.orm.associations.Effectivity;
import com.energyict.mdc.common.device.config.AllowedCalendar;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface ActiveEffectiveCalendar extends Effectivity {

    void updateLastVerifiedDate(Instant lastVerifiedDate);

    Instant getLastVerifiedDate();

    AllowedCalendar getAllowedCalendar();

}