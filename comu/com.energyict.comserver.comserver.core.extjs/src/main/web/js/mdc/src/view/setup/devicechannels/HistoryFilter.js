/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicechannels.HistoryFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'device-channels-history-filter',

    requires: [
        'Mdc.store.HistoryChannels'
    ],
    store: 'Mdc.store.HistoryChannels',

    filters: [
        {
            type: 'interval',
            dataIndex: 'endInterval',
            dataIndexFrom: 'intervalStart',
            dataIndexTo: 'intervalEnd',
            text: Uni.I18n.translate('deviceChannelsHistory.filter.endOfIntervalBetween.emptytext', 'MDC', 'End of interval between'),
            itemId: 'end-interval--between-filter'
        },
        {
            type: 'interval',
            dataIndex: 'changedInterval',
            dataIndexFrom: 'changedStart',
            dataIndexTo: 'changedEnd',
            text: Uni.I18n.translate('deviceChannelsHistory.filter.changedBetween.emptytext', 'MDC', 'Changed between'),
            itemId: 'changed-between-filter'
        }
    ]
});