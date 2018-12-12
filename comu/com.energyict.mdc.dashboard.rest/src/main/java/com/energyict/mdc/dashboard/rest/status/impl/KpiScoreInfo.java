/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bvn on 10/10/14.
 */
public class KpiScoreInfo {
    public String name;
    public List<BigDecimal> data;

    public KpiScoreInfo() {
    }

    public KpiScoreInfo(String name) {
        this.name = name;
        data = new ArrayList<>();
    }
}
