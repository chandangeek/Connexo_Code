/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.customtask.HistoryFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'ctk-view-historyfilter',

    store: 'Apr.store.CustomTaskHistory',

    filters: [
        {
            type: 'interval',
            dataIndex: 'startedBetween',
            dataIndexFrom: 'startedOnFrom',
            dataIndexTo: 'startedOnTo',
            text: Uni.I18n.translate('customTask.historyFilter.startedBetween', 'APR', 'Started between')
        },
        {
            type: 'interval',
            dataIndex: 'finishedBetween',
            dataIndexFrom: 'finishedOnFrom',
            dataIndexTo: 'finishedOnTo',
            text: Uni.I18n.translate('customTask.historyFilter.finishedBetween', 'APR', 'Finished between')
        },
        {
            type: 'combobox',
            dataIndex: 'status',
            itemId: 'cbo-status',
            emptyText: Uni.I18n.translate('general.status', 'APR', 'Status'),
            multiSelect: true,
            displayField: 'display',
            valueField: 'value',
            store: 'Apr.store.Status'
        }
    ]
});