/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyValue;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface UsagePointRequirement {

    SearchableProperty getSearchableProperty();

    SearchablePropertyValue.ValueBean toValueBean();

}