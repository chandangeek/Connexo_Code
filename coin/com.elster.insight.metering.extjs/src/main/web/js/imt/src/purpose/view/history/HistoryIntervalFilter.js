/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.history.HistoryIntervalFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    alias: 'widget.output-readings-history-interval-filter',
    store: null,
    filterDefault: null,

    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'interval',
                dataIndex: 'endInterval',
                dataIndexFrom: 'intervalStart',
                dataIndexTo: 'intervalEnd',
                text: Uni.I18n.translate('general.endOfIntervalBetween', 'IMT', 'End of interval between'),
                itemId: 'output-readings-history-filter-interval',
                defaultFromDate: me.filterDefault.defaultFromDate,
                defaultToDate: me.filterDefault.defaultToDate
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