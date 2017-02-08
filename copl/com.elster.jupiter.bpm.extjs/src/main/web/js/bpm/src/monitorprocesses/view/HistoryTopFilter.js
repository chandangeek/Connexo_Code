/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.monitorprocesses.view.HistoryTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'bpm-view-history-processes-topfilter',
    store: 'Bpm.monitorprocesses.store.HistoryProcesses',
    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'combobox',
                dataIndex: 'process',
                emptyText: Uni.I18n.translate('bpm.process.filter.process', 'BPM', 'Process'),
                itemId: 'bpm-view-history-processes-topfilter-process',
                multiSelect: true,
                displayField: 'displayName',
                valueField: 'fullName',
                width: 240,
                store: 'Bpm.monitorprocesses.store.HistoryProcessesFilterProcesses'
            },
            {
                type: 'interval',
                dataIndex: 'startedBetween',
                dataIndexFrom: 'startedOnFrom',
                dataIndexTo: 'startedOnTo',
                text: Uni.I18n.translate('bpm.process.startedBetween', 'BPM', 'Started between'),
                itemId: 'bpm-view-history-processes-topfilter-started'
            },
            {
                type: 'combobox',
                dataIndex: 'status',
                emptyText: Uni.I18n.translate('bpm.process.filter.status', 'BPM', 'Status'),
                itemId: 'bpm-view-history-processes-topfilter-status',
                multiSelect: true,
                displayField: 'display',
                valueField: 'value',
                store: 'Bpm.monitorprocesses.store.HistoryProcessesFilterStatuses'
            },
            {
                type: 'combobox',
                dataIndex: 'user',
                emptyText: Uni.I18n.translate('bpm.process.filter.startedBy', 'BPM', 'Started by'),
                itemId: 'bpm-view-history-processes-topfilter-startedBy',
                multiSelect: true,
                displayField: 'name',
                valueField: 'name',
                store: 'Bpm.monitorprocesses.store.HistoryProcessesFilterUsers'
            }
        ];
        me.callParent(arguments);
    }
});