/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.view.datapurge.LogGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.data-purge-log-grid',
    store: 'Sam.store.DataPurgeLog',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Uni.DateTime'
    ],
    forceFit: true,

    columns: [
        {
            itemId: 'data-purge-history-log-timestamp-column',
            header: Uni.I18n.translate('datapurge.log.timestamp', 'SAM', 'Timestamp'),
            dataIndex: 'timestamp',
            width: 200,
            renderer: function (value) {
                return value ? Uni.DateTime.formatDateTime(value, Uni.DateTime.SHORT, Uni.DateTime.LONG) : '-';
            }
        },
        {
            itemId: 'data-purge-history-log-log-level-column',
            header: Uni.I18n.translate('datapurge.log.loglevel', 'SAM', 'Log level'),
            dataIndex: 'logLevel',
            width: 150
        },
        {
            itemId: 'data-purge-history-log-message-column',
            header: Uni.I18n.translate('datapurge.log.message', 'SAM', 'Message'),
            dataIndex: 'message',
            flex: 1
        }
    ],

    initComponent: function () {
        var me = this;

        me.dockedItems = [
            {
                itemId: 'data-purge-log-grid-paging-toolbar-top',
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('datapurge.log.pagingtoolbartop.displayMsg', 'SAM', '{0} - {1} of {2} log lines'),
                displayMoreMsg: Uni.I18n.translate('datapurge.log.pagingtoolbartop.displayMoreMsg', 'SAM', '{0} - {1} of more than {2} log lines'),
                emptyMsg: Uni.I18n.translate('datapurge.log.pagingtoolbartop.emptyMsg', 'SAM', 'There are no log lines to display')
            },
            {
                itemId: 'data-purge-log-grid-paging-toolbar-bottom',
                xtype: 'pagingtoolbarbottom',
                dock: 'bottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('datapurge.log.pagingtoolbarbottom.itemsPerPage', 'SAM', 'Log lines per page')
            }
        ];

        me.callParent(arguments);
    }
});