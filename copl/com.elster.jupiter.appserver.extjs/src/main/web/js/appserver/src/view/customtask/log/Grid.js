/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.customtask.log.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.ctk-log-grid',
    store: 'Apr.store.Logs',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Uni.DateTime',
        'Uni.grid.column.LogLevel'
    ],
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.timestamp', 'APR', 'Timestamp'),
                dataIndex: 'timestamp',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTime(value, Uni.DateTime.SHORT, Uni.DateTime.LONG) : '-';
                },
                flex: 2
            },
            {
                xtype: 'log-level-column',
                dataIndex: 'loglevel',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.message', 'APR', 'Message'),
                dataIndex: 'message',
                flex: 5
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('log.pagingtoolbartop.displayMsg', 'APR', '{0} - {1} of {2} log lines'),
                displayMoreMsg: Uni.I18n.translate('log.pagingtoolbartop.displayMoreMsg', 'APR', '{0} - {1} of more than {2} log lines'),
                emptyMsg: Uni.I18n.translate('log.pagingtoolbartop.emptyMsg', 'APR', 'There are no log lines to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('log.pagingtoolbarbottom.itemsPerPage', 'APR', 'Log lines per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }

});