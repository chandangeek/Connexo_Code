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
                text: Uni.I18n.translate('importService.history.started', 'WSS', 'Started between')
            },
            {
                type: 'interval',
                dataIndex: 'finished',
                dataIndexFrom: 'finishedOnFrom',
                dataIndexTo: 'finishedOnTo',
                text: Uni.I18n.translate('importService.history.finished', 'WSS', 'Finished between')
            },
            {
                type: 'combobox',
                dataIndex: 'webServiceEndPoint',
                emptyText: Uni.I18n.translate('general.webServiceEndpoint', 'WSS', 'Web service endpoint'),
                hidden: Boolean(me.endpoint),
                displayField: 'name',
                valueField: 'id',
                store: endpointStore
            },
            {
                type: 'combobox',
                dataIndex: 'status',
                emptyText: Uni.I18n.translate('general.status', 'WSS', 'Status'),
                multiSelect: true,
                displayField: 'display',
                valueField: 'value',
                store: 'Wss.store.endpoint.Status'
            },
            {
                type: 'combobox',
                dataIndex: 'type',
                emptyText: Uni.I18n.translate('general.type', 'WSS', 'Type'),
                hidden: Boolean(me.endpoint),
                multiSelect: true,
                displayField: 'display',
                valueField: 'value',
                store: 'Wss.store.endpoint.Type'
            }
        ];

        me.callParent(arguments);
    }
});