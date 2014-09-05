package com.energyict.mdc.dashboard.rest.status.impl;

/**
 * The possible options to have data displayed in Heatmaps
 */
enum HeatMapBreakdownOption {
    connectionTypes(FilterOption.connectionTypes),
    deviceTypes(FilterOption.deviceTypes),
    comPortPools(FilterOption.comPortPools);

    private final FilterOption filterOption;

    HeatMapBreakdownOption(FilterOption connectionTypes) {
        filterOption = connectionTypes;
    }

    public FilterOption filterOption() {
        return filterOption;
    }
}
