/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.validation.impl;

import com.energyict.mdc.device.data.validation.ValidationOverview;
import com.energyict.mdc.device.data.validation.ValidationOverviews;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides an implementation for the {@link ValidationOverviews} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-08 (14:13)
 */
class ValidationOverviewsImpl implements ValidationOverviews {
    private List<ValidationOverview> overviews = new ArrayList<>();

    void add(ValidationOverviewImpl overview) {
        overviews.add(overview);
    }

    @Override
    public List<ValidationOverview> allOverviews() {
        return Collections.unmodifiableList(overviews);
    }

}