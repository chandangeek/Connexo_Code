Ext.define('Cfg.view.validationtask.HistoryGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.tasks-history-grid',
    store: 'Cfg.store.ValidationTasksHistory',
    router: null,

    requires: [
        'Uni.grid.column.Action',
        'Uni.grid.column.Duration',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Cfg.view.validationtask.HistoryActionMenu'
    ],

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('validationTasks.general.startedOn', 'CFG', 'Started On'),
                dataIndex: 'startedOn',
                flex: 2,
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute('administration/validationtasks/datavalidationtask/history/occurrence').buildUrl({occurrenceId: record.get('id')}),
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
                header: Uni.I18n.translate('validationTasks.general.status', 'CFG', 'Status'),
                dataIndex: 'status',
                textAlign: 'center',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                width: 100,
                menu: {
                    xtype: 'tasks-history-action-menu'              
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('validationTasks.pagingtoolbartop.displayMsg', 'CFG', '{0} - {1} of {2} validation tasks'),
                displayMoreMsg: Uni.I18n.translate('validationTasks.pagingtoolbartop.displayMoreMsg', 'CFG', '{0} - {1} of more than {2} validation tasks'),
                emptyMsg: Uni.I18n.translate('validationTasks.pagingtoolbartop.emptyMsg', 'CFG', 'There are no validation tasks to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('validationTasks.pagingtoolbarbottom.itemsPerPage', 'CFG', 'Validation tasks per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});
