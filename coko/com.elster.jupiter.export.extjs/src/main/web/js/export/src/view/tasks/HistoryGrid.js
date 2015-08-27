Ext.define('Dxp.view.tasks.HistoryGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.dxp-tasks-history-grid',
    store: 'Dxp.store.DataExportTasksHistory',
    router: null,

    requires: [
        'Uni.grid.column.Action',
        'Uni.grid.column.Duration',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Dxp.view.tasks.HistoryActionMenu'
    ],

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.startedOn', 'DES', 'Started On'),
                dataIndex: 'startedOn',
                flex: 2,
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute('administration/dataexporttasks/dataexporttask/history/occurrence').buildUrl({occurrenceId: record.get('id')}),
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
                header: Uni.I18n.translate('general.status', 'DES', 'Status'),
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
                displayMsg: Uni.I18n.translate('dataExportTasks.pagingtoolbartop.displayMsg', 'DES', '{0} - {1} of {2} data export tasks'),
                displayMoreMsg: Uni.I18n.translate('dataExportTasks.pagingtoolbartop.displayMoreMsg', 'DES', '{0} - {1} of more than {2} data export tasks'),
                emptyMsg: Uni.I18n.translate('dataExportTasks.pagingtoolbartop.emptyMsg', 'DES', 'There are no data export tasks to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('dataExportTasks.pagingtoolbarbottom.itemsPerPage', 'DES', 'Data export tasks per page'),
                dock: 'bottom',
                deferLoading: true
            }
        ];

        me.callParent(arguments);
    }
});
