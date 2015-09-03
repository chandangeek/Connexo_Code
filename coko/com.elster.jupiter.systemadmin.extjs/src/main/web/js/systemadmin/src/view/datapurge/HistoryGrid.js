Ext.define('Sam.view.datapurge.HistoryGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.data-purge-history-grid',
    store: 'Sam.store.DataPurgeHistory',
    requires: [
        'Uni.grid.column.Duration',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Sam.view.datapurge.HistoryActionMenu'
    ],
    router: null,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                itemId: 'data-purge-history-grid-started-on-column',
                header: Uni.I18n.translate('datapurge.history.startedon', 'SAM', 'Started on'),
                dataIndex: 'startDate',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.I18n.translate('general.dateattime', 'SAM', '{0} At {1}',[Uni.DateTime.formatDateShort(value),Uni.DateTime.formatTimeShort(value)]).toLowerCase():'';
                }
            },
            {
                itemId: 'data-purge-history-grid-duration-column',
                xtype: 'uni-grid-column-duration',
                dataIndex: 'duration',
                flex: 1
            },
            {
                itemId: 'data-purge-history-grid-status-column',
                header: Uni.I18n.translate('general.status', 'SAM', 'Status'),
                dataIndex: 'status',
                flex: 1
            },
            {
                itemId: 'data-purge-history-grid-action-column',
                xtype: 'uni-actioncolumn',
                menu: {
                    itemId: 'data-purge-history-action-menu',
                    xtype: 'data-purge-history-action-menu',
                    router: me.router
                }
            }
        ];

        me.dockedItems = [
            {
                itemId: 'data-purge-history-grid-paging-toolbar-top',
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('datapurge.history.pagingtoolbartop.displayMsg', 'SAM', '{0} - {1} of {2} history lines'),
                displayMoreMsg: Uni.I18n.translate('datapurge.history.pagingtoolbartop.displayMoreMsg', 'SAM', '{0} - {1} of more than {2} history lines'),
                emptyMsg: Uni.I18n.translate('datapurge.history.pagingtoolbartop.emptyMsg', 'SAM', 'There are no history lines to display')
            },
            {
                itemId: 'data-purge-history-grid-paging-toolbar-bottom',
                xtype: 'pagingtoolbarbottom',
                dock: 'bottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('datapurge.history.pagingtoolbarbottom.itemsPerPage', 'SAM', 'History lines per page')
            }
        ];

        me.callParent(arguments);
    }
});