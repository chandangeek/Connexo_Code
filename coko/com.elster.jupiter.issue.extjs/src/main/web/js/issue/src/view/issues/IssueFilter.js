/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.issues.IssueFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'isu-view-issues-issuefilter',
    isOverviewFilter: false,
    store: undefined,

    requires:[
        'Isu.store.IssueTypes',
        'Isu.store.IssueStatuses',
        'Isu.store.IssueWorkgroupAssignees',
        'Isu.store.IssueAssignees',
        'Isu.store.Devices',
        'Isu.store.DueDate',
        'Isu.store.IssueReasons',
        'Isu.store.IssueUsagePoints',
        'Isu.store.DeviceGroups',
        'Isu.store.IssueReasons',
        'Isu.store.Locations'
    ],

    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'text',
                itemId: 'issue-id',
                dataIndex: 'id',
                width: 180,
                emptyText: Uni.I18n.translate('general.inputIssueId', 'ISU', 'Issue ID')
            },
            {
                type: 'combobox',
                itemId: 'issue-type-filter',
                dataIndex: 'issueType',
                emptyText: Uni.I18n.translate('general.type', 'ISU', 'Type'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'uid',
                store: 'Isu.store.IssueTypes'
            },
            {
                type: 'combobox',
                itemId: 'issue-status-filter',
                dataIndex: 'status',
                emptyText: Uni.I18n.translate('general.status', 'ISU', 'Status'),
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
                emptyText: Uni.I18n.translate('general.workgroup', 'ISU', 'Workgroup'),
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
                emptyText: Uni.I18n.translate('general.user', 'ISU', 'User'),
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
                emptyText: Uni.I18n.translate('general.reason', 'ISU', 'Reason'),
                displayField: 'name',
                valueField: 'id',
                store: 'Isu.store.IssueReasons',
                queryMode: 'remote',
                queryParam: 'like',
                loadStore: !me.isOverviewFilter,
                queryCaching: false,
                minChars: 0,
                forceSelection: false,
                hidden: me.isOverviewFilter
            },
            {
                type: 'dueDate',
                itemId: 'issue-dueDate-filter',
                dataIndex: 'dueDate',
                emptyText: Uni.I18n.translate('general.dueDate', 'ISU', 'Due date'),
                multiSelect: true,
                displayField: 'name',
                loadStore: false,
                valueField: 'id',
                store: 'Isu.store.DueDate'
            },
            {
                type: 'combobox',
                itemId: 'issue-meter-filter',
                dataIndex: 'meter',
                emptyText: Uni.I18n.translate('general.title.device', 'ISU', 'Device'),
                displayField: 'name',
                valueField: 'name',
                store: 'Isu.store.Devices',
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
                    },
                    change: {
                        fn: me.onDeviceChange
                    }
                }
            },
            {
                type: 'combobox',
                itemId: 'issue-deviceGroup-filter',
                dataIndex: 'deviceGroup',
                emptyText: Uni.I18n.translate('general.deviceGroup', 'ISU', 'Device group'),
                displayField: 'name',
                valueField: 'id',
                store: 'Isu.store.DeviceGroups',
                multiSelect: true,
            },
            {
                emptyText: Uni.I18n.translate('general.title.usagePoint', 'ISU', 'Usage point'),
                type: 'combobox',
                itemId: 'issue-usagePoints-filter',
                dataIndex: 'usagePoint',
                displayField: 'name',
                valueField: 'name',
                store: 'Isu.store.IssueUsagePoints',
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
                type: 'interval',
                itemId: 'issue-creationDate-filter',
                dataIndex: 'startInterval',
                dataIndexFrom: 'startIntervalFrom',
                dataIndexTo: 'startIntervalTo',
                text: Uni.I18n.translate('general.title.creationDate', 'ISU', 'Creation date'),
                hidden: me.isOverviewFilter
            },
            {
                type: 'numeric',
                dataIndex: 'priority',
                itemId: 'isu-priority-filter',
                text: Uni.I18n.translate('general.title.priority', 'ISU', 'Priority')
            },
            {
                type: 'combobox',
                itemId: 'issue-location-filter',
                dataIndex: 'location',
                emptyText: Uni.I18n.translate('general.location', 'ISU', 'Location'),
                displayField: 'name',
                valueField: 'id',
                store: 'Isu.store.Locations',
                queryMode: 'remote',
                queryParam: 'like',
                loadStore: false,
                queryCaching: false,
                minChars: 0,
                forceSelection: false,
                matchFieldWidth: false,
                getParamValue: me.comboGetParamValue,
                width: 377,
                triggerAction: 'last',
                listeners: {
                    beforequery: {
                        fn: me.locationBeforeQuery
                    }
                }
            },
            {
                type: 'checkbox',
                dataIndex: 'showTopology',
                layout: 'hbox',
                defaults: {margin: '0 10 0 0'},
                hidden: me.isOverviewFilter,
                listeners: {
                    afterrender: function (field) {
                        if (Ext.isEmpty(this.up().down('combobox[itemId=issue-meter-filter]').getValue())) {
                            this.up().down('checkbox[itemId=showTopology-filter]').setValue(false);
                            this.up().down('checkbox[itemId=showTopology-filter]').setDisabled(true);
                        } else {
                            this.up().down('checkbox[itemId=showTopology-filter]').setDisabled(false);
                        }

                    }
                },
                options: [
                    {
                        display: Uni.I18n.translate('general.showTopology','ISU','Show Topology'),
                        value: 'true',
                        itemId: 'showTopology-filter'
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    locationBeforeQuery: function (qe) {
        if ( typeof qe.combo.lastQuery !== 'undefined' && qe.combo.lastQuery !== '' && qe.combo.getValue() === null ) {
            delete qe.combo.lastQuery;
            return false;
        }
    },

    onBeforeLoad: function (store, options) {
        var me = this,
            queryString = Uni.util.QueryString.getQueryStringValues(false),
            params = {};

        options.params = options.params || {};

        // Memory proxy.
        if (me.hasActiveFilter()) {
            var tempParams = me.checkGrouping(me.getFilterParams(false, !me.filterObjectEnabled));
            tempParams.application = Uni.util.Application.getAppName();

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
        me.down('button[action=clearAll]').setDisabled(!((options.params.filter && Ext.decode(options.params.filter).length>1)));
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

    onAssigneeBlur: function (field) {
        if (field.getRawValue()) {
            field.setValue(field.lastSelection);
        }
    },
    onDeviceChange: function (field) {
        var value = field.getValue();

        if (Ext.isEmpty(value)) {
            this.up().down('checkbox[itemId=showTopology-filter]').setValue(false);
            this.up().down('checkbox[itemId=showTopology-filter]').setDisabled(true);
        } else {
            this.up().down('checkbox[itemId=showTopology-filter]').setDisabled(false);
        }
    }
});