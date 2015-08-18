Ext.define('Isu.view.issues.IssueFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'isu-view-issues-issuefilter',

    store: undefined,

    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'combobox',
                dataIndex: 'status',
                emptyText: Uni.I18n.translate('general.status', 'ISU', 'Status'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Isu.store.IssueStatuses'
            },
            {
                type: 'combobox',
                dataIndex: 'assignee',
                emptyText: Uni.I18n.translate('view.issues.issueFilter.assignee', 'ISU', 'Type for assignees'),
                store: 'Isu.store.IssueAssignees',
                displayField: 'name',
                valueField: 'idx',
                anyMatch: true,
                queryMode: 'remote',
                queryParam: 'like',
                queryCaching: false,
                minChars: 0,
                loadStore: false,
                setFilterValue: me.comboSetFilterValue,
                getParamValue: me.comboGetParamValue,
                listeners: {
                    expand: {
                        fn: me.comboLimitNotification
                    }
                }
            },
            {
                type: 'combobox',
                dataIndex: 'reason',
                emptyText: Uni.I18n.translate('general.reason', 'ISU', 'Reason'),
                displayField: 'name',
                valueField: 'id',
                store: 'Isu.store.IssueReasons',
                queryMode: 'remote',
                queryParam: 'like',
                queryCaching: false,
                minChars: 0
            },
            {
                type: 'combobox',
                dataIndex: 'meter',
                emptyText: Uni.I18n.translate('view.issues.issueFilter.meter', 'ISU', 'Type to search by MRID'),
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
                listeners: {
                    expand: {
                        fn: me.comboLimitNotification
                    }
                }
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
        store.model.load(value, {
            success: function (record) {
                store.loadData([record], false);
                store.lastOptions = {};
                store.fireEvent('load', store, [record], true)
            }
        });
    },

    comboGetParamValue: function () {
        return this.value;
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