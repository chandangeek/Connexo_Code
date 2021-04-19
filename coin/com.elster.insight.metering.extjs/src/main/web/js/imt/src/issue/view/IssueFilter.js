/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.issue.view.IssueFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'issue-view-filter',
    isOverviewFilter: false,
    store: undefined,

    requires: [
        'Isu.store.IssueTypes',
        'Isu.store.IssueStatuses',
        'Isu.store.IssueWorkgroupAssignees',
        'Isu.store.IssueAssignees',
        'Imt.issue.store.UsagePoints',
        'Isu.store.DueDate',
        'Isu.store.IssueReasons',
        'Imt.usagepointgroups.store.UsagePointGroups'
    ],

    initComponent: function () {
        var me = this,
            usagePointsStore = Ext.getStore('Imt.issue.store.UsagePoints') || Ext.create('Imt.issue.store.UsagePoints');

        usagePointsStore.getProxy().setExtraParam('nameOnly', true);

        me.filters = [
            {
                type: 'text',
                itemId: 'issue-id',
                dataIndex: 'id',
                width: 180,
                emptyText: Uni.I18n.translate('general.inputIssueId', 'IMT', 'Issue ID')
            },
            {
                type: 'combobox',
                itemId: 'issue-type-filter',
                dataIndex: 'issueType',
                emptyText: Uni.I18n.translate('general.type', 'IMT', 'Type'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'uid',
                store: 'Isu.store.IssueTypes'
            },
            {
                type: 'combobox',
                itemId: 'issue-status-filter',
                dataIndex: 'status',
                emptyText: Uni.I18n.translate('general.status', 'IMT', 'Status'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Isu.store.IssueStatuses'
            },
            {
                type: 'combobox',
                itemId: 'issue-workgroup-filter',
                dataIndex: 'workGroupAssignee',
                multiSelect: true,
                emptyText: Uni.I18n.translate('general.workgroup', 'IMT', 'Workgroup'),
                store: 'Isu.store.IssueWorkgroupAssignees',
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
                emptyText: Uni.I18n.translate('general.user', 'IMT', 'User'),
                store: 'Isu.store.IssueAssignees',
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
                emptyText: Uni.I18n.translate('general.reason', 'IMT', 'Reason'),
                displayField: 'name',
                valueField: 'id',
                store: 'Isu.store.IssueReasons',
                queryMode: 'remote',
                queryParam: 'like',
                loadStore: !me.isOverviewFilter,
                queryCaching: false,
                minChars: 0,
                forceSelection: true,
                hidden: me.isOverviewFilter
            },
            {
                type: 'dueDate',
                itemId: 'issue-dueDate-filter',
                dataIndex: 'dueDate',
                emptyText: Uni.I18n.translate('general.dueDate', 'IMT', 'Due date'),
                multiSelect: true,
                displayField: 'name',
                loadStore: false,
                valueField: 'id',
                store: 'Isu.store.DueDate'
            },
            {
                type: 'combobox',
                itemId: 'issue-meter-filter',
                dataIndex: 'usagePoint',
                emptyText: Uni.I18n.translate('general.title.usagepoint', 'IMT', 'Usage point'),
                displayField: 'name',
                valueField: 'name',
                store: usagePointsStore,
                queryMode: 'remote',
                queryParam: 'name',
                queryCaching: false,
                minChars: 0,
                loadStore: false,
                setFilterValue: me.comboSetFilterValue,
                getParamValue: me.comboGetParamValue,
                forceSelection: true,
                hidden: me.isOverviewFilter,
                listeners: {
                    expand: {
                        fn: me.comboLimitNotification
                    }
                }
            },
            {
                type: 'combobox',
                itemId: 'issue-usagePointGroup-filter',
                dataIndex: 'usagePointGroup',
                emptyText: Uni.I18n.translate('general.usagePointGroup', 'IMT', 'Usage point group'),
                displayField: 'name',
                valueField: 'id',
                store: 'Imt.usagepointgroups.store.UsagePointGroups',
                multiSelect: true,
            },
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
            tempParams.application = 'INS';

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
        store.filterParams = params;
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
                        html: Uni.I18n.translate('issues.limitNotification', 'IMT', 'Keep typing to narrow down'),
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
