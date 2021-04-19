/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.processes.view.AllProcessesTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'view-all-processes-topfilter',
    store: 'Mdc.processes.store.AllProcessesStore',

    requires: [
        'Mdc.processes.store.AllProcessesFilterStore',
        'Mdc.processes.store.AllProcessTypeStore',
        'Bpm.monitorprocesses.store.HistoryProcessesFilterUsers',
        'Mdc.processes.store.AllProcessesStatusStore',
        'Mdc.processes.store.AllProcessesStore'
    ],

    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'combobox',
                dataIndex: 'process',
                emptyText: Uni.I18n.translate('mdc.processes.filter.process', 'MDC', 'Process'),
                itemId: 'processes-topfilter-all-processes',
                multiSelect: true,
                displayField: 'displayName',
                valueField: 'fullName',
                width: 240,
                store: 'Mdc.processes.store.AllProcessesFilterStore'
            },
            {
                type: 'interval',
                dataIndex: 'startedBetween',
                dataIndexFrom: 'startedOnFrom',
                dataIndexTo: 'startedOnTo',
                text: Uni.I18n.translate('mdc.processes.startedBetween', 'MDC', 'Started between'),
                itemId: 'processes-topfilter-all-processes-started'
            },
            {
                type: 'combobox',
                dataIndex: 'variableId',
                emptyText: Uni.I18n.translate('mdc.processes.allprocessestopfilter.type', 'MDC', 'Type'),
                itemId: 'processes-topfilter-all-processes-type',
                multiSelect: false,
                displayField: 'displayType',
                valueField: 'valueType',
                store: 'Mdc.processes.store.AllProcessTypeStore',
                listeners: {
                    change: {
                            scope: me,
                            fn: me.onTypeChange
                            }
                }
            },
            {
                type: 'combobox',
                itemId: 'processes-topfilter-all-processes-object',
                dataIndex: 'value',
                emptyText: Uni.I18n.translate('mdc.processes.allprocessestopfilter.objects', 'MDC', 'Objects'),
                displayField: 'name',
                valueField: 'mrId',
                store: 'Mdc.processes.store.ObjectStoreExtended',
                queryMode: 'remote',
                queryParam: 'like',
                queryCaching: false,
                minChars: 0,
                disabled: true,
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
                dataIndex: 'status',
                emptyText: Uni.I18n.translate('mdc.processes.filter.status', 'MDC', 'Status'),
                itemId: 'processes-topfilter-all-processes-status',
                multiSelect: true,
                displayField: 'display',
                valueField: 'value',
                store: 'Mdc.processes.store.AllProcessesStatusStore'
            },
            {
                type: 'combobox',
                dataIndex: 'user',
                emptyText: Uni.I18n.translate('mdc.processes.filter.startedBy', 'MDC', 'Started by'),
                itemId: 'processes-topfilter-all-processes-startedBy',
                multiSelect: true,
                displayField: 'name',
                valueField: 'name',
                store: 'Bpm.monitorprocesses.store.HistoryProcessesFilterUsers'
            },
            {
        		type: 'noui',
        		itemId: 'processInstanceIdFilter',
        		dataIndex: 'processInstanceId'
        	},
        	{
        		type: 'noui',
        		itemId: 'searchInAllProcessesFilter',
        		dataIndex: 'searchInAllProcesses'
        	}
        ];

        me.callParent(arguments);
        me.initActions();
    },
    
    initActions: function () {
        var me = this,
            applyButton = me.down('button[action=applyAll]'),
            clearButton = me.down('button[action=clearAll]');

        applyButton.on('click', me.allProcessesApplyFilters, me);
        clearButton.on('click', me.clearFilters, me);
    },
    
    allProcessesApplyFilters: function() {
    	var me = this;
    	me.clearFilter('processInstanceId');
    	me.clearFilter('searchInAllProcesses');
    	me.applyFilters();
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
        filterValue = [(Ext.isArray(me.value) && me.value.length > 0 && Ext.isObject(me.value[0]) ? me.value[0].get('id') : me.value)];

        if(filterValue !== null && me.value !== null)
        {
            return  filterValue;
        }

        return undefined;
    },

    comboLimitNotification: function (combo) {
        var picker = combo.getPicker(),
            fn = function (view) {
            var store = view.getStore(),
                el = view.getEl().down('.' + Ext.baseCSSPrefix + 'list-plain');

            if (store.getTotalCount() > store.getCount()) {
                el.appendChild({
                    tag: 'li',
                    html: Uni.I18n.translate('mdc.processes.limitNotification', 'MDC', 'Keep typing to narrow down'),
                    cls: Ext.baseCSSPrefix + 'boundlist-item combo-limit-notification'
                });
            }
        };

        picker.on('refresh', fn);
        picker.on('beforehide', function () {
            picker.un('refresh', fn);
        }, combo, {single: true});
    },



    onTypeChange: function(typeCombo, newValue, oldValue) {
        var me = this;

        objectsCombo = me.down('#processes-topfilter-all-processes-object');

        if(newValue === undefined || newValue == undefined || newValue == "" || newValue == null){
            objectsCombo.setDisabled(true);
            return;
        }

        typeCombo = me.down('#processes-topfilter-all-processes-type');
        var selectedType = typeCombo.getValue();

        if (selectedType == "deviceId")
        {
            objectsCombo.getStore('Mdc.processes.store.ObjectStoreExtended').getProxy().setUrl("deviceobjects");
        }
        if (selectedType == "alarmId")
        {
            objectsCombo.getStore('Mdc.processes.store.ObjectStoreExtended').getProxy().setUrl("alarmobjects");
        }
        if (selectedType == "issueId")
        {
            objectsCombo.getStore('Mdc.processes.store.ObjectStoreExtended').getProxy().setUrl("issueobjects");
        }

        objectsCombo.setDisabled(false);
    }
});
