/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import java.util.List;

/**
 * Created by bvn on 8/19/14.
 */
class TaskSummaryCounterInfo {
    public List<String> id; // Id to be used in a filter
    public String displayName; // Localized name to display in the UI
    public String name; // Untranslated name of this counter
    public long count;
}
