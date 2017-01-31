/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.LoggingGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.wss-logging-grid',
    store: 'Wss.store.Logs',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.timestamp', 'WSS', 'Timestamp'),
                dataIndex: 'timestampDisplay',
                flex: 1.5
            },
            {
                header: Uni.I18n.translate('general.logLevel', 'WSS', 'Log level'),
                dataIndex: 'logLevel',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.message', 'WSS', 'Message'),
                dataIndex: 'message',
                flex: 8
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('webservices.log.pagingtoolbartop.displayMsg', 'WSS', '{0} - {1} of {2} log lines'),
                displayMoreMsg: Uni.I18n.translate('webservices.log.pagingtoolbartop.displayMoreMsg', 'WSS', '{0} - {1} of more than {2} log lines'),
                emptyMsg: Uni.I18n.translate('webservices.log.pagingtoolbartop.emptyMsg', 'WSS', 'There are no log lines to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                defaultPageSize: 50,
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('log.pagingtoolbarbottom.itemsPerPage', 'WSS', 'Log lines per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});