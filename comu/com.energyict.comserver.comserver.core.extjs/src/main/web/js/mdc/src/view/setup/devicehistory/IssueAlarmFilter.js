/**
 * Created by H251853 on 8/28/2017.
 */

Ext.define('Mdc.view.setup.devicehistory.IssueAlarmFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'issues-alarm-filter',
    isOverviewFilter: false,
    store: 'Isu.store.Issues',

    requires: [
        'Isu.store.IssueTypes',
        'Isu.store.IssueStatuses',
        'Isu.store.IssueReasons'
    ],

    initComponent: function () {
        var me = this;

        me.filters = [
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
                itemId: 'issue-reason-filter',
                dataIndex: 'reason',
                emptyText: Uni.I18n.translate('general.reason', 'ISU', 'Reason'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Isu.store.IssueReasons'
            },
            {
                type: 'interval',

                dataIndex: 'startInterval',
                dataIndexFrom: 'startIntervalFrom',
                dataIndexTo: 'startIntervalTo',
                text: Uni.I18n.translate('general.period', 'ISU', 'Period'),
                itemId: 'issue-period-filter'
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
            }


        ];

        me.callParent(arguments);
    },

    onBeforeLoad1: function (store, options) {
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
        Ext.apply(options.params, params);
        me.down('button[action=clearAll]').setDisabled(!((options.params.filter && Ext.decode(options.params.filter).length)));
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
    }
});