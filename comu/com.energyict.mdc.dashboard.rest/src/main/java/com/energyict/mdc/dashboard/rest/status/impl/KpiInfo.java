/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import java.util.List;

/**
 * Created by bvn on 10/16/14.
 */
public class KpiInfo {
    public List<Long> time;
    public List<KpiScoreInfo> series;

    public KpiInfo() {
    }
}
