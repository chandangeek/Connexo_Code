Ext.define('Fim.view.log.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.fim-history-log-grid',
    store: 'Fim.store.Logs',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('importService.log.timestamp', 'FIM', 'Timestamp'),
                dataIndex: 'timestamp',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                },
                flex: 2
            },
            {
                header: Uni.I18n.translate('importService.log.level', 'FIM', 'Log level'),
                dataIndex: 'loglevel',
                flex: 1
            },
            {
                header: Uni.I18n.translate('importService.log.message', 'FIM', 'Message'),
                dataIndex: 'message',
                flex: 5
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('importService.log.pagingtoolbartop.displayMsg', 'FIM', '{0} - {1} of {2} log lines per page'),
                displayMoreMsg: Uni.I18n.translate('importService.log.pagingtoolbartop.displayMoreMsg', 'FIM', '{0} - {1} of more than {2} log lines'),
                emptyMsg: Uni.I18n.translate('importService.log.pagingtoolbartop.emptyMsg', 'FIM', 'There are no log lines to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('importService.log.pagingtoolbarbottom.itemsPerPage', 'FIM', 'Log lines per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});