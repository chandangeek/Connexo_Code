/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.endpoint.OccurrenceGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.webservice-history-occurrence-grid',
    store: 'Wss.store.endpoint.OccurrenceLog',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                dataIndex: 'timestamp',
                header: Uni.I18n.translate('general.timestamp', 'WSS', 'Timestamp'),
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '-';
                },
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
            },
            {
                xtype: 'uni-actioncolumn',
                flex: 1,
                isDisabled: function(menu, item) {
                    var occurrenceLogStore = menu.getStore('Wss.store.endpoint.OccurrenceLog')
                    if(occurrenceLogStore){
                        occurrenceLogRecord = occurrenceLogStore.getAt(item);
                        var stackTrace = occurrenceLogRecord.get('stackTrace');
                        return !stackTrace;
                    }
                    return true;
                },
                menu: {
                    xtype: 'webservices-endpoint-occurrence-action-menu'
                }
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