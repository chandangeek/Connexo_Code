/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Scs.view.log.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.scs-log-grid',
    store: 'Scs.store.Logs',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.timestamp', 'SCS', 'Timestamp'),
                dataIndex: 'timestampDisplay',
                flex: 2
            },
            {
                header: Uni.I18n.translate('general.logLevel', 'SCS', 'Log level'),
                dataIndex: 'logLevel',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.message', 'SCS', 'Message'),
                dataIndex: 'message',
                flex: 5
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('serviceCalls.log.pagingtoolbartop.displayMsg', 'SCS', '{0} - {1} of {2} log lines'),
                displayMoreMsg: Uni.I18n.translate('serviceCalls.log.pagingtoolbartop.displayMoreMsg', 'SCS', '{0} - {1} of more than {2} log lines'),
                emptyMsg: Uni.I18n.translate('serviceCalls.log.pagingtoolbartop.emptyMsg', 'SCS', 'There are no log lines to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                defaultPageSize: 50,
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('log.pagingtoolbarbottom.itemsPerPage', 'SCS', 'Log lines per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});