/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.dataquality.impl;

import com.elster.jupiter.mdm.dataquality.DataQualityOverview;
import com.elster.jupiter.mdm.dataquality.DataQualityOverviews;

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