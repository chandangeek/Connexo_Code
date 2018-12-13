/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.elster.jupiter.search.SearchablePropertyValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a serializable search filter for Devices
 */
public class DevicesForConfigChangeSearch {
    @SuppressWarnings("unused")
    public DevicesForConfigChangeSearch() {}

    public Map<String, SearchablePropertyValue.ValueBean> searchItems = new HashMap<>();
}
