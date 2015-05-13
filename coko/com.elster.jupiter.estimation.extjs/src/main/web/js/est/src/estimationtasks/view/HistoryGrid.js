Ext.define('Est.estimationtasks.view.HistoryGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.estimationtasks-history-grid',
    store: 'Est.estimationtasks.store.EstimationTasksHistory',
    router: null,

    requires: [
        'Est.estimationtasks.view.HistoryActionMenu',
        'Uni.grid.column.Action',
        'Uni.grid.column.Duration',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('estimationtasks.general.startedOn', 'EST', 'Started On'),
                dataIndex: 'startedOn',
                flex: 2,
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute('administration/estimationtasks/estimationtask/history/occurrence').buildUrl({occurrenceId: record.get('id')}),
                        date = value ? Uni.DateTime.formatDateTimeShort(new Date(value)) : '-';
                    return '<a href="' + url + '">' + date + '</a>';
                }
            },
            {
                xtype: 'uni-grid-column-duration',
                dataIndex: 'duration',
                shortFormat: true,
                textAlign: 'center',
                flex: 1
            },
            {
                header: Uni.I18n.translate('estimationtasks.general.status', 'EST', 'Status'),
                dataIndex: 'status',
                textAlign: 'center',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                width: 100,
                menu: {
                    xtype: 'estimationtasks-history-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('estimationtasks.pagingtoolbartop.displayMsg', 'EST', '{0} - {1} of {2} history lines per page'),
                displayMoreMsg: Uni.I18n.translate('estimationtasks.pagingtoolbartop.displayMoreMsg', 'EST', '{0} - {1} of more than {2} history lines'),
                emptyMsg: Uni.I18n.translate('estimationtasks.pagingtoolbartop.emptyMsg', 'EST', 'There are no history lines to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('estimationtasks.history.pagingtoolbarbottom.itemsPerPage', 'EST', 'History lines per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});
