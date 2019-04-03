/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.view.IssueFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'view-issues-filter',
    isOverviewFilter: false,
    store: undefined,

    requires: [
        'Isu.store.Issues',
        'Itk.store.IssueAssignees',
        'Itk.store.IssueReasons',
        'Itk.store.IssueStatuses',
        'Itk.store.IssueWorkgroupAssignees',
        'Itk.store.ClearStatus',
        'Itk.store.DueDate',
        'Itk.store.Devices'
    ],

    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'text',
                itemId: 'issue-id',
                dataIndex: 'id',
                width: 180,
                emptyText: Uni.I18n.translate('general.inputIssueId', 'ITK', 'Input issue ID...')
            },
            {
                type: 'combobox',
                itemId: 'issue-meter-filter',
                dataIndex: 'meter',
                emptyText: Uni.I18n.translate('general.title.device', 'ITK', 'Device'),
                displayField: 'name',
                valueField: 'name',
                store: 'Itk.store.Devices',
                queryMode: 'remote',
                queryParam: 'like',
                queryCaching: false,
                minChars: 0,
                loadStore: false,
                setFilterValue: me.comboSetFilterValue,
                getParamValue: me.comboGetParamValue,
                forceSelection: false,
                hidden: me.isOverviewFilter,
                listeners: {
                    expand: {
                        fn: me.comboLimitNotification
                    }
                }
            },
            {
                type: 'dueDate',
                itemId: 'issue-dueDate-filter',
                dataIndex: 'dueDate',
                emptyText: Uni.I18n.translate('general.dueDate', 'ITK', 'Due date'),
                multiSelect: true,
                displayField: 'name',
                loadStore: false,
                valueField: 'id',
                store: 'Itk.store.DueDate'
            },
            {
                type: 'combobox',
                itemId: 'issue-status-filter',
                dataIndex: 'status',
                emptyText: Uni.I18n.translate('general.status', 'ITK', 'Status'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Itk.store.IssueStatuses'
            },
            {
                type: 'combobox',
                itemId: 'issue-cleared-filter',
                dataIndex: 'cleared',
                emptyText: Uni.I18n.translate('general.clear', 'ITK', 'Cleared'),
                multiSelect: true,
                displayField: 'name',
                loadStore: false,
                valueField: 'id',
                store: 'Itk.store.ClearStatus'
            },
            {
                type: 'combobox',
                itemId: 'issue-workgroup-filter',
                dataIndex: 'workGroupAssignee',
                multiSelect: true,
                emptyText: Uni.I18n.translate('general.workgroup', 'ITK', 'Workgroup'),
                store: 'Itk.store.IssueWorkgroupAssignees',
                displayField: 'name',
                valueField: 'id',
                queryMode: 'local',
                minChars: 1
            },
            {
                type: 'combobox',
                itemId: 'issue-assignee-filter',
                dataIndex: 'userAssignee',
                multiSelect: true,
                emptyText: Uni.I18n.translate('general.user', 'ITK', 'User'),
                store: 'Itk.store.IssueAssignees',
                displayField: 'name',
                valueField: 'id',
                anyMatch: true,
                queryMode: 'remote',
                queryParam: 'like',
                queryCaching: false,
                minChars: 1,
                loadStore: false,
                setFilterValue: me.comboSetFilterValue,
                getParamValue: me.comboGetParamValue,
                forceSelection: false,
                listeners: {
                    expand: {
                        fn: me.comboLimitNotification
                    },
                    blur: {
                        fn: me.onAssigneeBlur
                    }
                }
            },
            {
                type: 'combobox',
                itemId: 'issue-reason-filter',
                dataIndex: 'reason',
                emptyText: Uni.I18n.translate('general.reason', 'ITK', 'Reason'),
                displayField: 'name',
                valueField: 'id',
                store: 'Itk.store.IssueReasons',
                queryMode: 'local',
                loadStore: !me.isOverviewFilter,
                queryCaching: false,
                minChars: 0,
                forceSelection: false,
                hidden: me.isOverviewFilter
            },
            {
                type: 'interval',
                dataIndex: 'startInterval',
                dataIndexFrom: 'startIntervalFrom',
                dataIndexTo: 'startIntervalTo',
                text: Uni.I18n.translate('general.createdBetween', 'ITK', 'Created between'),
                itemId: 'created-between-filter',
                hidden: me.isOverviewFilter
            }
        ];

        me.callParent(arguments);
    },

    onBeforeLoad: function (store, options) {
        var me = this,
            queryString = Uni.util.QueryString.getQueryStringValues(false),
            params = {};

        options.params = options.params || {};

        // Memory proxy.
        if (me.hasActiveFilter()) {
            var tempParams = me.checkGrouping(me.getFilterParams(false, !me.filterObjectEnabled));

            if (me.filterObjectEnabled) {
                params[me.filterObjectParam] = me.createFiltersObject(tempParams);
            } else {
                params = tempParams;
            }
        }

        if (me.historyEnabled) {
            me.updateHistoryState();
        }

        if (queryString.sort) {
            params.sort = queryString.sort;
        }

        Ext.apply(options.params, params);
        me.down('button[action=clearAll]').setDisabled(!((options.params.filter && Ext.decode(options.params.filter).length)));
    },

    checkGrouping: function (params) {
        var queryString = Uni.util.QueryString.getQueryStringValues(false);

        if (Ext.isDefined(queryString.groupingType) && Ext.isDefined(queryString.groupingValue) && Ext.isEmpty(params[queryString.groupingType])) {
            params[queryString.groupingType] = queryString.groupingValue;
        }

        return params;
    },

    comboSetFilterValue: function (value) {
        var combo = this,
            store = combo.getStore();

        combo.value = value;
        combo.setHiddenValue(value);

        if (Ext.isArray(value)) {
            var arr = [];
            Ext.Array.each(value, function (v) {
                store.model.load(v, {
                    success: function (record) {
                        arr.push(record);
                        store.loadData(arr, false);
                        store.lastOptions = {};
                        store.fireEvent('load', store, arr, true);
                        combo.value = arr;
                    }
                });
            });
        } else {
            store.model.load(value, {
                success: function (record) {
                    combo.value = [record];
                    store.loadData([record], false);
                    store.lastOptions = {};
                    store.fireEvent('load', store, [record], true)
                }
            });
        }
    },

    comboGetParamValue: function () {
        var me = this;
        return Ext.isArray(me.value) && me.value.length > 0 && Ext.isObject(me.value[0]) ? me.value[0].get('id') : me.value;
    },

    comboLimitNotification: function (combo) {
        var picker = combo.getPicker(),
            fn = function (view) {
                var store = view.getStore(),
                    el = view.getEl().down('.' + Ext.baseCSSPrefix + 'list-plain');

                if (store.getTotalCount() > store.getCount()) {
                    el.appendChild({
                        tag: 'li',
                        html: Uni.I18n.translate('issues.limitNotification', 'ITK', 'Keep typing to narrow down'),
                        cls: Ext.baseCSSPrefix + 'boundlist-item combo-limit-notification'
                    });
                }
            };

        picker.on('refresh', fn);
        picker.on('beforehide', function () {
            picker.un('refresh', fn);
        }, combo, {single: true});
    },

    onAssigneeBlur: function (field) {
        if (field.getRawValue()) {
            field.setValue(field.lastSelection);
        }
    }
});