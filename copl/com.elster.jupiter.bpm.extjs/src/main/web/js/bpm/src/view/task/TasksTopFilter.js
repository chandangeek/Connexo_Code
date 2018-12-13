/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.view.task.TasksTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'bpm-view-tasks-topfilter',
    store: 'Bpm.store.task.Tasks',

    requires: [
        'Bpm.store.task.TasksFilterDueDates',
        'Bpm.store.task.TasksFilterProcesses',
        'Bpm.store.task.TasksFilterStatuses',
        'Bpm.store.task.TasksFilterWorkgroups',
        'Bpm.store.task.TasksFilterUsers',
        'Bpm.store.task.Devices'
    ],

    initComponent: function () {
        var me = this,
            objectStore = Ext.getStore('Bpm.store.task.Devices') || Ext.create('Bpm.store.task.Devices'),
            applicationName = Ext.Ajax.defaultHeaders['X-CONNEXO-APPLICATION-NAME'],
            objectType, dataIndex, emptyText;

        if (applicationName === 'MDC') {
            objectStore.getProxy().setExtraParam('nameOnly', true);
            objectStore.getProxy().setMdcUrl();
            objectType = 'deviceId';
            dataIndex = 'name';
            emptyText = Uni.I18n.translate('bpm.filter.device', 'BPM', 'Device');
        }
        else if (applicationName === 'INS') {
            objectStore.getProxy().setExtraParam('nameOnly', true);
            objectStore.getProxy().setMdmUrl();
            objectType = 'usagePointId';
            dataIndex = 'name';
            emptyText = Uni.I18n.translate('bpm.filter.usagePoint', 'BPM', 'Usage point');
        }

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
            },
            {
                type: 'combobox',
                itemId: 'object-filter',
                dataIndex: dataIndex,
                emptyText: emptyText,
                displayField: 'name',
                valueField: 'name',
                store: objectStore,
                queryMode: 'remote',
                queryParam: 'name',
                queryCaching: false,
                minChars: 0,
                loadStore: false,
                forceSelection: false,
                listeners: {
                    expand: {
                        fn: me.comboLimitNotification
                    }
                },
                applyParamValue: function (params, includeUndefined, flattenObjects) {
                    var me = this,
                        record = me.findRecord(me.valueField || me.displayField, me.getValue());
                    if (record) {
                        var mRID = record.get('mRID'),
                            queryString = Uni.util.QueryString.getQueryStringValues(false);

                        params[objectType] = mRID;
                        queryString[objectType] = mRID;
                        window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
                    }

                }
            },
        ];
        me.callParent(arguments);
    },

    comboLimitNotification: function (combo) {
        var picker = combo.getPicker(),
            fn = function (view) {
                var store = view.getStore(),
                    el = view.getEl().down('.' + Ext.baseCSSPrefix + 'list-plain');
                if (store.getTotalCount() > store.getCount()) {
                    el.appendChild({
                        tag: 'li',
                        html: Uni.I18n.translate('bpm.limitNotification', 'BPM', 'Keep typing to narrow down'),
                        cls: Ext.baseCSSPrefix + 'boundlist-item combo-limit-notification'
                    });
                }
            };

        picker.on('refresh', fn);
        picker.on('beforehide', function () {
            picker.un('refresh', fn);
        }, combo, {single: true});
    }
    });