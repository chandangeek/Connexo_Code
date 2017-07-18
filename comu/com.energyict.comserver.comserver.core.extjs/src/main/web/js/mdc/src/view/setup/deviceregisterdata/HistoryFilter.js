/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceregisterdata.HistoryFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'device-register-history-filter',
    requires: [],
    store: 'Mdc.store.NumericalRegisterHistoryData',
    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'interval',
                dataIndex: 'endInterval',
                dataIndexFrom: 'intervalStart',
                dataIndexTo: 'intervalEnd',
                text: Uni.I18n.translate('deviceregisterdata.history.filter.measurementTime', 'MDC', 'Measurement time'),
                itemId: 'end-interval-between-filter',
                defaultFromDate: me.filterDefault.defaultFromDate,
                defaultToDate: me.filterDefault.defaultToDate,
            },
            {
                type: 'checkbox',
                dataIndex: 'changedDataOnly',
                layout: 'hbox',
                defaults: {margin: '0 10 0 0'},
                options: [
                    {
                        display: Uni.I18n.translate('deviceregisterdata.history.filter.changedData', 'MDC', 'Changed data'),
                        value: 'yes',
                        itemId: 'changedData-filter'
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});