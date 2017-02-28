/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.view.widget.ConnectionsTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'dsh-view-widget-connectionstopfilter',

    store: 'Dsh.store.ConnectionTasks',

    initComponent: function () {
        var me = this;

        me.filters = [
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
                    }
                }
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
                dataIndex: 'latestStates',
                emptyText: Uni.I18n.translate('general.lastResult', 'DSH', 'Last result'),
                multiSelect: true,
                displayField: 'localizedValue',
                valueField: 'successIndicator',
                store: 'Dsh.store.filter.LatestStatus',
                itemId: 'latest-state-filter'
            },
            {
                type: 'combobox',
                dataIndex: 'latestResults',
                emptyText: Uni.I18n.translate('general.lastConnection', 'DSH', 'Last connection'),
                multiSelect: true,
                displayField: 'localizedValue',
                valueField: 'successIndicator',
                store: 'Dsh.store.filter.LatestResult',
                itemId: 'latest-result-filter'
            },
            {
                type: 'combobox',
                dataIndex: 'comPortPools',
                emptyText: Uni.I18n.translate('general.commPortPool', 'DSH', 'Communication port pool'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Dsh.store.filter.CommPortPool',
                itemId: 'com-port-pools-filter'
            },
            {
                type: 'combobox',
                dataIndex: 'connectionTypes',
                emptyText: Uni.I18n.translate('general.connectionType', 'DSH', 'Connection type'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
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
    }

});