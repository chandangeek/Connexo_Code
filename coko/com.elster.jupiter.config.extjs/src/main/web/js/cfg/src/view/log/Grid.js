Ext.define('Cfg.view.log.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.log-grid',
    store: 'Cfg.store.Logs',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('dataValidationTasks.general.timestamp', 'CFG', 'Timestamp'),
                dataIndex: 'timestamp',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                },
                flex: 2
            },
            {
                header: Uni.I18n.translate('dataValidationTasks.general.logLevel', 'CFG', 'Log level'),
                dataIndex: 'loglevel',
                flex: 1
            },
            {
                header: Uni.I18n.translate('dataValidationTasks.general.message', 'CFG', 'Message'),
                dataIndex: 'message',
                flex: 5
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('dataValidationTasks.log.pagingtoolbartop.displayMsg', 'CFG', '{0} - {1} of {2} log lines per page'),
                displayMoreMsg: Uni.I18n.translate('dataValidationTasks.log.pagingtoolbartop.displayMoreMsg', 'CFG', '{0} - {1} of more than {2} log lines'),
                emptyMsg: Uni.I18n.translate('dataValidationTasks.log.pagingtoolbartop.emptyMsg', 'CFG', 'There are no log lines to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('dataValidationTasks.log.pagingtoolbarbottom.itemsPerPage', 'CFG', 'Log lines per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});