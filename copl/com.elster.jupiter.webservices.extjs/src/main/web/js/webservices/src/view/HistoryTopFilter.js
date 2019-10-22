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
                valueToNumber: true
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
                emptyText: Uni.I18n.translate('mdc.processes.allprocessestopfilter.objects', 'MDC', 'Objects'),
                displayField: 'displayValue',
                valueField: "id",
                store: 'Wss.store.RelatedAttributeStore',
                queryMode: 'remote',
                queryParam: 'like',
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
                        html: Uni.I18n.translate('mdc.processes.limitNotification', 'MDC', 'Keep typing to narrow down'),
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
