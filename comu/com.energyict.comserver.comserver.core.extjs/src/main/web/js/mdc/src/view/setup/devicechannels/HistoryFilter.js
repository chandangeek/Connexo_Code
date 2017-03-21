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

    initComponent: function () {
        var me = this;
        me.filters = [
            {
                type: 'duration',
                dataIndex: 'interval',
                dataIndexFrom: 'intervalStart',
                dataIndexTo: 'intervalEnd',
                text: Uni.I18n.translate('general.startDate', 'MDC', 'Start date'),
                durationStore: me.filterDefault.durationStore,
                loadStore: false,
                defaultFromDate: me.filterDefault.fromDate,
                defaultDuration: me.filterDefault.duration,
                itemId: 'devicechannels-topfilter-duration'
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
        ]

        me.callParent(arguments);
    }
});