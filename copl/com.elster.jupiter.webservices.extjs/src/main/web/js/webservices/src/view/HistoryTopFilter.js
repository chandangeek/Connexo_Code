/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.HistoryTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'mss-view-history-history-topfilter',

    initComponent: function () {
        var me = this;

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
                dataIndex: 'webservice',
                emptyText: Uni.I18n.translate('general.webService', 'WSS', 'Web service'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Wss.store.Webservices'
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
                multiSelect: true,
                displayField: 'display',
                valueField: 'value',
                store: 'Wss.store.endpoint.Type'
            }
        ];

        me.callParent(arguments);
    }
});