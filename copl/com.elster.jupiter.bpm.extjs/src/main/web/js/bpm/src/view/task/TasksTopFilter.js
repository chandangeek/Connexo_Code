/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.view.task.TasksTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'bpm-view-tasks-topfilter',
    store: 'Bpm.store.task.Tasks',
    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'combobox',
                dataIndex: 'dueDate',
                emptyText: Uni.I18n.translate('bpm.filter.dueDate', 'BPM', 'Due date'),
                itemId: 'bpm-view-tasks-topfilter-dueDate',
                multiSelect: true,
                displayField: 'display',
                valueField: 'value',
                store: 'Bpm.store.task.TasksFilterDueDates'
            },
            {
                type: 'combobox',
                dataIndex: 'process',
                emptyText: Uni.I18n.translate('bpm.filter.process', 'BPM', 'Process'),
                itemId: 'bpm-view-tasks-topfilter-process',
                multiSelect: true,
                displayField: 'displayName',
                valueField: 'fullName',
                width: 240,
                store: 'Bpm.store.task.TasksFilterProcesses'
            },
            {
                type: 'combobox',
                dataIndex: 'status',
                emptyText: Uni.I18n.translate('bpm.filter.status', 'BPM', 'Status'),
                itemId: 'bpm-view-tasks-topfilter-status',
                multiSelect: true,
                displayField: 'display',
                valueField: 'value',
                store: 'Bpm.store.task.TasksFilterStatuses'
            },
            {
                type: 'combobox',
                dataIndex: 'workgroup',
                emptyText: Uni.I18n.translate('bpm.filter.workgroup', 'BPM', 'Workgroup'),
                itemId: 'bpm-view-tasks-topfilter-workgroup-assignee',
                multiSelect: true,
                displayField: 'name',
                valueField: 'name',
                store: 'Bpm.store.task.TasksFilterWorkgroups'
            },
            {
                type: 'combobox',
                dataIndex: 'user',
                emptyText: Uni.I18n.translate('bpm.filter.user', 'BPM', 'User'),
                itemId: 'bpm-view-tasks-topfilter-user-assignee',
                multiSelect: true,
                displayField: 'name',
                valueField: 'name',
                store: 'Bpm.store.task.TasksFilterUsers'
            }
        ]
        me.callParent(arguments);
    }
    });