/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.view.log.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.dxp-log-grid',
    store: 'Dxp.store.Logs',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Uni.DateTime'
    ],
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.timestamp', 'DES', 'Timestamp'),
                dataIndex: 'timestamp',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTime(value, Uni.DateTime.SHORT, Uni.DateTime.LONG) : '-';
                },
                flex: 2
            },
            {
                header: Uni.I18n.translate('general.logLevel', 'DES', 'Log level'),
                dataIndex: 'loglevel',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.message', 'DES', 'Message'),
                dataIndex: 'message',
                flex: 5
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('log.pagingtoolbartop.displayMsg', 'DES', '{0} - {1} of {2} log lines'),
                displayMoreMsg: Uni.I18n.translate('log.pagingtoolbartop.displayMoreMsg', 'DES', '{0} - {1} of more than {2} log lines'),
                emptyMsg: Uni.I18n.translate('log.pagingtoolbartop.emptyMsg', 'DES', 'There are no log lines to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('log.pagingtoolbarbottom.itemsPerPage', 'DES', 'Log lines per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});