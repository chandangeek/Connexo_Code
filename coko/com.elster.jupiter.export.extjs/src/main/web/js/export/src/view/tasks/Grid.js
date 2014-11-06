Ext.define('Dxp.view.tasks.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.tasks-grid',
    store: 'Dxp.store.DataExportTasks',
    router: null,
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'DES', 'Name'),
                dataIndex: 'name',
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute('administration/dataexporttasks/dataexporttask').buildUrl({taskId: record.get('id')});
                    return '<a href="' + url + '">' + value + '</a>';
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.lastRun', 'DES', 'Last run'),
                dataIndex: 'lastRun',
                renderer: function (value, metaData, record) {
                    if (value) {
                        return record.get('status') + ' ' + value;
                    } else {
                        return Uni.I18n.translate('general.notPerformedYet', 'DES', 'Not performed yet')
                    }
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.nextRun', 'DES', 'Next run'),
                dataIndex: 'nextRun_formatted',
                flex: 1
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
                emptyMsg: Uni.I18n.translate('dataExportTasks.pagingtoolbartop.emptyMsg', 'DES', 'There are no data export tasks to display'),
                items: [
                    '->',
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.addDataExportTask', 'DES', 'Add data export task'),
                        ui: 'action',
                        href: '#/administration/dataexporttasks/add'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('dataExportTasks.pagingtoolbarbottom.itemsPerPage', 'DES', 'Data export tasks per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});
