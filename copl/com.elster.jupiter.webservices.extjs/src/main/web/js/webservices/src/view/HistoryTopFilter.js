/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.HistoryTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'mss-view-history-history-topfilter',
    requires: [
        'Wss.store.Endpoints'
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
            }
        ];

        me.callParent(arguments);
    }
});
