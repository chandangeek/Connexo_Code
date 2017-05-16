/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.history.HistoryFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    alias: 'widget.output-readings-history-filter',
    store: null,
    filterDefault: null,

    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'duration',
                dataIndex: 'interval',
                dataIndexFrom: 'intervalStart',
                dataIndexTo: 'intervalEnd',
                text: Uni.I18n.translate('general.startDate', 'IMT', 'Start date'),
                durationStore: me.filterDefault.durationStore,
                defaultFromDate: me.filterDefault.defaultFromDate,
                defaultDuration: me.filterDefault.duration,
                loadStore: false,
                itemId: 'output-readings-history-filter-duration'
            },
            {
                type: 'checkbox',
                dataIndex: 'changedDataOnly',
                itemId: 'output-readings-history-filter-changed-data',
                layout: 'hbox',
                defaults: {
                    margin: '0 10 0 0'
                },
                options: [
                    {
                        display: Uni.I18n.translate('general.changedData', 'IMT', 'Changed data'),
                        value: 'yes',
                        itemId: 'output-readings-history-filter-changed-data-checkbox'
                    }
                ]
            }
        ];

        me.callParent();
    }
});