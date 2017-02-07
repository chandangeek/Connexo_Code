Ext.define('Est.estimationtasks.view.LogGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.estimationtasks-log-grid',
    store: 'Est.estimationtasks.store.Logs',
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
                header: Uni.I18n.translate('estimationtasks.general.timestamp', 'EST', 'Timestamp'),
                dataIndex: 'timestamp',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTime(value, Uni.DateTime.SHORT, Uni.DateTime.LONG) : '-';
                },
                flex: 1
            },
            {
                xtype: 'log-level-column',
                dataIndex: 'loglevel',
                flex: 1
            },
            {
                header: Uni.I18n.translate('estimationtasks.general.message', 'EST', 'Message'),
                dataIndex: 'message',
                renderer: function (value) {
                    return value;
                },
                flex: 6
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('estimationtasks.log.pagingtoolbartop.displayMsg', 'EST', '{0} - {1} of {1} log lines'),
                displayMoreMsg: Uni.I18n.translate('estimationtasks.log.pagingtoolbartop.displayMoreMsg', 'EST', '{0} - {1} of more than {1} log lines'),
                emptyMsg: Uni.I18n.translate('estimationtasks.log.pagingtoolbartop.emptyMsg', 'EST', 'There are no log lines to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('estimationtasks.log.pagingtoolbarbottom.itemsPerPage', 'EST', 'Log lines per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});