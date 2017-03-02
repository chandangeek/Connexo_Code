/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.dataquality.impl;

import com.energyict.mdc.device.dataquality.DataQualityOverview;
import com.energyict.mdc.device.dataquality.DataQualityOverviews;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class DataQualityOverviewsImpl implements DataQualityOverviews {

    private List<DataQualityOverview> overviews = new ArrayList<>();

    void add(DataQualityOverviewImpl overview) {
        overviews.add(overview);
    }

    @Override
    public List<DataQualityOverview> allOverviews() {
        return Collections.unmodifiableList(overviews);
    }
}