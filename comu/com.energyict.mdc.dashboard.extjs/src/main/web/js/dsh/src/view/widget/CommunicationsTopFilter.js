/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.view.widget.CommunicationsTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'dsh-view-widget-communicationstopfilter',

    store: 'Dsh.store.CommunicationTasks',

    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'combobox',
                dataIndex: 'comTasks',
                emptyText: Uni.I18n.translate('general.commTask', 'DSH', 'Communication task'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Dsh.store.filter.CommunicationTask',
                itemId: 'com-task-filter'
            },
            {
                type: 'combobox',
                itemId: 'dsh-device-filter',
                dataIndex: 'device',
                emptyText: Uni.I18n.translate('general.device', 'DSH', 'Device'),
                displayField: 'name',
                valueField: 'name',
                store: 'Dsh.store.filter.Devices',
                queryMode: 'remote',
                queryParam: 'name',
                queryCaching: false,
                minChars: 0,
                loadStore: false,
                forceSelection: false,
                listeners: {
                    expand: {
                        fn: me.comboLimitNotification
                    },
                    change: {
                        fn: me.updateConnectionTypeFilter
                    }
                }
            },
            {
                type: 'combobox',
                dataIndex: 'connectionTypes',
                emptyText: Uni.I18n.translate('general.connectionType', 'DSH', 'Connection type'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                disabled: true,
                store: 'Dsh.store.filter.ConnectionType',
                itemId: 'connection-type-filter'
            },
            {
                type: 'combobox',
                dataIndex: 'deviceTypes',
                emptyText: Uni.I18n.translate('general.deviceType', 'DSH', 'Device type'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Dsh.store.filter.DeviceType',
                itemId: 'device-type-filter'
            },
            {
                type: 'combobox',
                dataIndex: 'deviceGroups',
                emptyText: Uni.I18n.translate('general.deviceGroup', 'DSH', 'Device group'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Dsh.store.filter.DeviceGroup',
                itemId: 'device-group-filter'
            },
            {
                type: 'combobox',
                dataIndex: 'latestResults',
                emptyText: Uni.I18n.translate('general.lastResult', 'DSH', 'Last result'),
                multiSelect: true,
                displayField: 'localizedValue',
                valueField: 'completionCode',
                store: 'Dsh.store.filter.CompletionCodes',
                itemId: 'latest-result-filter'
            },
            {
                type: 'combobox',
                dataIndex: 'currentStates',
                emptyText: Uni.I18n.translate('general.Status', 'DSH', 'Status'),
                multiSelect: true,
                displayField: 'localizedValue',
                valueField: 'taskStatus',
                store: 'Dsh.store.filter.CurrentState',
                itemId: 'current-state-filter'
            },
            {
                type: 'combobox',
                dataIndex: 'comSchedules',
                emptyText: Uni.I18n.translate('connection.widget.filter.sharedCommunicationSchedule', 'DSH', 'Shared communication schedule'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Dsh.store.filter.CommunicationSchedule',
                itemId: 'com-schedule-filter'
            },
            {
                type: 'interval',
                dataIndex: 'startInterval',
                dataIndexFrom: 'startIntervalFrom',
                dataIndexTo: 'startIntervalTo',
                text: Uni.I18n.translate('communications.widget.topfilter.startedBetween', 'DSH', 'Started between'),
                itemId: 'start-interval-filter'
            },
            {
                type: 'interval',
                dataIndex: 'finishInterval',
                dataIndexFrom: 'finishIntervalFrom',
                dataIndexTo: 'finishIntervalTo',
                text: Uni.I18n.translate('communications.widget.topfilter.finishedBetween', 'DSH', 'Finished between'),
                itemId: 'finish-interval-filter'
            }
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
                        html: Uni.I18n.translate('issues.limitNotification', 'ISU', 'Keep typing to narrow down'),
                        cls: Ext.baseCSSPrefix + 'boundlist-item combo-limit-notification'
                    });
                }
            };

        picker.on('refresh', fn);
        picker.on('beforehide', function () {
            picker.un('refresh', fn);
        }, combo, {single: true});
    },

    updateConnectionTypeFilter: function(combo, newValue) {
        var connectionTypeFilter = combo.up('dsh-view-widget-communicationstopfilter').down('#connection-type-filter');

        if (Ext.isEmpty(newValue)) {
            connectionTypeFilter.setValue();
        }
        connectionTypeFilter.setDisabled(Ext.isEmpty(newValue));
    }

});