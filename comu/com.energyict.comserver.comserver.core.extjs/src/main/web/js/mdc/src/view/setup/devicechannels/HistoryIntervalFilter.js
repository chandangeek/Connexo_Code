/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicechannels.HistoryIntervalFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'device-channels-history-interval-filter',

    requires: [
        'Mdc.store.HistoryChannels'
    ],
    store: 'Mdc.store.HistoryChannels',

    initComponent: function () {
        var me = this;
        me.filters = [
            {
                type: 'interval',
                dataIndex: 'endInterval',
                dataIndexFrom: 'intervalStart',
                dataIndexTo: 'intervalEnd',
                text: Uni.I18n.translate('deviceregisterdata.history.filter.endOfIntervalBetween.emptytext', 'MDC', 'End of interval between'),
                itemId: 'end-interval-between-filter'
            },
            {
                type: 'checkbox',
                dataIndex: 'changedDataOnly',
                layout: 'hbox',
                defaults: {margin: '0 10 0 0'},
                options: [
                    {
                        display: Uni.I18n.translate('deviceChannelsHistoryy.filter.changedData', 'MDC', 'Changed data'),
                        value: 'yes',
                        itemId: 'changedData-filter'
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});