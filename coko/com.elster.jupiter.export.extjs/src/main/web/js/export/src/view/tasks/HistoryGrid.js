Ext.define('Dxp.view.tasks.HistoryGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.dxp-tasks-history-grid',
    store: 'Dxp.store.DataExportTasksHistory',
    router: null,
    showExportTask: true,
    fromWorkspace: false,

    requires: [
        'Uni.grid.column.Action',
        'Uni.grid.column.Duration',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Uni.DateTime'
    ],

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.startedOn', 'DES', 'Started on'),
                dataIndex: 'startedOn',
                flex: 2,
                renderer: function (value, metaData, record) {
                    var url = me.fromWorkspace ?
                            me.router.getRoute('workspace/exporthistory/occurrence').buildUrl({occurrenceId: record.get('id')}) :
                            me.router.getRoute('administration/dataexporttasks/dataexporttask/history/occurrence')
                                .buildUrl({taskId: record.get("taskId"), occurrenceId: record.get('id')}),
                        date = value ? Uni.DateTime.formatDateTimeShort(new Date(value)) : '-';
                    return '<a href="' + url + '">' + date + '</a>';
                }
            },
            {
                header: Uni.I18n.translate('general.exportTask', 'DES', 'Export task'),
                hidden: me.showExportTask,
                dataIndex: 'name',
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute('administration/dataexporttasks/dataexporttask').buildUrl({taskId: record.get("taskId")});
                    return Dxp.privileges.DataExport.canView()
                        ? '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>'
                        : Ext.String.htmlEncode(value);
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
                header: Uni.I18n.translate('general.exportPeriod', 'DES', 'Export period'),
                itemId: 'export-period-column',
                dataIndex: 'exportPeriod_range',
                textAlign: 'center',
                flex: 3
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('dataExportTasks.history.pagingtoolbartop.displayMsg', 'DES', '{0} - {1} of {2} history lines'),
                displayMoreMsg: Uni.I18n.translate('dataExportTasks.history.pagingtoolbartop.displayMoreMsg', 'DES', '{0} - {1} of more than {2} history lines'),
                emptyMsg: Uni.I18n.translate('dataExportTasks.history.pagingtoolbartop.emptyMsg', 'DES', 'There are no history lines to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('dataExportTasks.history.pagingtoolbarbottom.itemsPerPage', 'DES', 'History lines per page'),
                dock: 'bottom',
                deferLoading: true
            }
        ];

        me.callParent(arguments);
    }
});
