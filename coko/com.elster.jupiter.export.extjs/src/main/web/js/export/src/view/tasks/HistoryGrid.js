Ext.define('Dxp.view.tasks.HistoryGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.tasks-history-grid',
    store: 'Dxp.store.DataExportTasksHistory',
    router: null,

    requires: [
        'Uni.grid.column.Action',
        'Uni.grid.column.Duration',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.startedOn', 'DES', 'Started On'),
                dataIndex: 'startedOn',
                flex: 2,
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute('administration/dataexporttasks/dataexporttask/history/occurrence').buildUrl({occurrenceId: record.get('id')});
                    return '<a href="' + url + '">' + Ext.util.Format.date(new Date(value), 'D d M Y \\a\\t h:i A') + '</a>';
                }
            },
            {
                xtype: 'uni-grid-column-duration',
                dataIndex: 'duration',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.status', 'DES', 'Status'),
                dataIndex: 'status',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.exportPeriod', 'DES', 'Export period'),
                renderer: function (value, metaData, record) {
                    var exportPeriodFrom = new Date(record.get('exportPeriodFrom')),
                        exportPeriodTo = new Date(record.get('exportPeriodTo'));
                    return 'From ' + Ext.util.Format.date(exportPeriodFrom, 'D d M Y H:i:s') +
                        ' to ' + Ext.util.Format.date(exportPeriodTo, 'D d M Y H:i:s');
                },
                flex: 3
            },
            {
                xtype: 'uni-actioncolumn',
                menu: {
                    xtype: 'tasks-action-menu',
                    itemId: 'tasks-action-menu'
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
                deferLoading: true,
                itemsPerPageMsg: Uni.I18n.translate('dataExportTasks.pagingtoolbarbottom.itemsPerPage', 'DES', 'Data export tasks per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});
