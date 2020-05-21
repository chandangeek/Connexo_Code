/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.HistoryTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'mss-view-history-history-topfilter',
    requires: [
        'Wss.store.Endpoints',
        'Wss.store.RelatedAttributeStore'
    ],
    endpoint: null,

    initComponent: function () {
        var me = this;
        var endpointStore = Ext.create('Wss.store.Endpoints', {
            pageSize: 1000,
        })

        me.filters = [
            {
                type: 'interval',
                dataIndex: 'started',
                dataIndexFrom: 'startedOnFrom',
                dataIndexTo: 'startedOnTo',
                itemId: 'history-topfilter-started',
                text: Uni.I18n.translate('importService.history.started', 'WSS', 'Started between')
            },
            {
                type: 'interval',
                dataIndex: 'finished',
                dataIndexFrom: 'finishedOnFrom',
                dataIndexTo: 'finishedOnTo',
                itemId: 'history-topfilter-finished',
                text: Uni.I18n.translate('importService.history.finished', 'WSS', 'Finished between')
            },
            {
                type: Boolean(me.endpoint) ? 'noui' : 'combobox',
                dataIndex: 'webServiceEndPoint',
                itemId: 'history-topfilter-webServiceEndPoint',
                emptyText: Uni.I18n.translate('general.webServiceEndpoint', 'WSS', 'Web service endpoint'),
                displayField: 'name',
                valueField: 'id',
                store: endpointStore,
                value: me.endpoint ? me.endpoint.getId() : undefined,
                valueToNumber: true,
                matchFieldWidth: false,
                multiSelect: true,
                listConfig: {
                    minWidth: 180,
                    maxWidth: 600
                },
                listeners: {
                    expand: function (combo) {
                        var me = this;
                        function setTooltips() {
                            var alltheItems = me.store.data.items
                            var picker = combo.getPicker();

                            alltheItems.forEach(function (value) {
                                picker.getNode(value).setAttribute("data-qtip", value.get(combo.displayField))
                            })
                        }
                        if (me.store && me.store.data && me.store.data.items){
                            setTooltips();
                        }else{
                            me.store.on('load', setTooltips);
                        }
                    }
                }
            },
            {
                type: 'combobox',
                dataIndex: 'type',
                itemId: 'history-topfilter-type',
                emptyText: Uni.I18n.translate('general.type', 'WSS', 'Type'),
                hidden: Boolean(me.endpoint),
                multiSelect: true,
                displayField: 'display',
                valueField: 'value',
                store: 'Wss.store.endpoint.Type'
            },
            {
                type: 'combobox',
                dataIndex: 'status',
                itemId: 'history-topfilter-status',
                emptyText: Uni.I18n.translate('general.status', 'WSS', 'Status'),
                multiSelect: true,
                displayField: 'display',
                valueField: 'value',
                store: 'Wss.store.endpoint.Status'
            },
            {

                type: 'combobox',
                itemId: 'history-topfilter-relatedobject',
                dataIndex: 'wsRelatedObjectId',
                emptyText: Uni.I18n.translate('mdc.processes.allprocessestopfilter.objects', 'WSS', 'Objects'),
                displayField: 'displayValue',
                valueField: "id",
                store: 'Wss.store.RelatedAttributeStore',
                queryMode: 'remote',
                queryParam: 'like',
                setFilterValue: me.comboSetFilterValue,
                queryCaching: false,
                minChars: 0,
                loadStore: false,
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
        ];

        me.callParent(arguments);
    },

    onAssigneeBlur: function (field) {
        if (field.getRawValue()) {
            field.setValue(field.lastSelection);
        }
    },

    comboSetFilterValue: function(value) {
        var combo = this,
        store = combo.getStore();

        combo.value = value;
        combo.setHiddenValue(value);

        store.model.load(value, {
            success: function (record) {
                combo.setValue(record);
                //combo.value = [record];
                store.loadData([record], false);
                store.lastOptions = {};
                store.fireEvent('load', store, [record], true)
            }
        });
    },

    comboLimitNotification: function (combo) {
            var picker = combo.getPicker(),
                fn = function (view) {
                var store = view.getStore(),
                    el = view.getEl().down('.' + Ext.baseCSSPrefix + 'list-plain');

                if (store.getTotalCount() > store.getCount()) {
                    el.appendChild({
                        tag: 'li',
                        html: Uni.I18n.translate('mdc.processes.limitNotification', 'WSS', 'Keep typing to narrow down'),
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
