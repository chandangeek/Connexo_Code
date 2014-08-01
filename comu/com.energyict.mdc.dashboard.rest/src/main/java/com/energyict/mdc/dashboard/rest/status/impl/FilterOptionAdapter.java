package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;

/**
 * This Adapter will allow us to extract the filter option for a query object, detect errors if any and report those in a proper Extjs JSON format
 *
 * Created by bvn on 7/31/14.
 */
public class FilterOptionAdapter extends MapBasedXmlAdapter<FilterOption> {

    public FilterOptionAdapter() {
        for (FilterOption filterOption : FilterOption.values()) {
            register(filterOption.name(), filterOption);
        }
        register(null,null);
    }
}
