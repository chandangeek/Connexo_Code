/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.log.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.cfg-log-grid',
    store: 'Cfg.store.Logs',
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
                header: Uni.I18n.translate('validationTasks.general.timestamp', 'CFG', 'Timestamp'),
                dataIndex: 'timestamp',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTime(value, Uni.DateTime.SHORT, Uni.DateTime.LONG) : '-';
                },
                flex: 2
            },
            {
                xtype: 'log-level-column',
                dataIndex: 'loglevel'
            },
            {
                header: Uni.I18n.translate('validationTasks.general.message', 'CFG', 'Message'),
                dataIndex: 'message',
                flex: 5
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('validationTasks.log.pagingtoolbartop.displayMsg', 'CFG', '{0} - {1} of {2} log lines'),
                displayMoreMsg: Uni.I18n.translate('validationTasks.log.pagingtoolbartop.displayMoreMsg', 'CFG', '{0} - {1} of more than {2} log lines'),
                emptyMsg: Uni.I18n.translate('validationTasks.log.pagingtoolbartop.emptyMsg', 'CFG', 'There are no log lines to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('validationTasks.log.pagingtoolbarbottom.itemsPerPage', 'CFG', 'Log lines per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});