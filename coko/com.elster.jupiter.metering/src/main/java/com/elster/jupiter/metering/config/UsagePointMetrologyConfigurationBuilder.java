/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import com.elster.jupiter.calendar.EventSet;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.search.SearchablePropertyValue;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface UsagePointMetrologyConfigurationBuilder {

    UsagePointMetrologyConfigurationBuilder withDescription(String description);

    UsagePointMetrologyConfigurationBuilder withEventSet(EventSet eventSet);

    UsagePointMetrologyConfigurationBuilder withCustomPropertySet(RegisteredCustomPropertySet customPropertySet);

    UsagePointMetrologyConfigurationBuilder withUsagePointRequirement(SearchablePropertyValue.ValueBean requirementSpecs);

    UsagePointMetrologyConfigurationBuilder withGapsAllowed(boolean gapsAllowed);

    UsagePointMetrologyConfiguration create();

}