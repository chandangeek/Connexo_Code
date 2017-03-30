/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.elster.jupiter.orm.associations.Effectivity;
import com.energyict.mdc.device.config.AllowedCalendar;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface ActiveEffectiveCalendar extends Effectivity {

    void updateLastVerifiedDate(Instant lastVerifiedDate);

    Instant getLastVerifiedDate();

    AllowedCalendar getAllowedCalendar();

}