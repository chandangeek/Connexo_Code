/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import com.elster.jupiter.calendar.EventSet;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface MetrologyConfigurationBuilder {

    MetrologyConfigurationBuilder withDescription(String description);

    MetrologyConfigurationBuilder withEventSet(EventSet eventSet);

    MetrologyConfigurationBuilder withCustomPropertySet(RegisteredCustomPropertySet customPropertySet);

    MetrologyConfiguration create();

}