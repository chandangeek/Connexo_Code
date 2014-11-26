Ext.define('Dxp.view.datasources.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.data-sources-grid',
    store: 'Dxp.store.DataSources',
    taskId: null,
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.mrid', 'DES', 'MRID'),
                dataIndex: 'mRID',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.deviceStatus', 'DES', 'Device status'),
                dataIndex: 'active',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.serialNumber', 'DES', 'Serial number'),
                dataIndex: 'serialNumber',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.readingType', 'DES', 'Reading type'),
                dataIndex: 'readingType',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.lastRun', 'DES', 'Last run'),
                dataIndex: 'lastRun',
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute('administration/dataexporttasks/dataexporttask/history/occurrence').buildUrl({taskId: me.taskId, occurrenceId: record.get('occurrenceId')});
                    return '<a href="' + url + '">' + value + '</a>';
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.lastExportedDate', 'DES', 'Last exported date'),
                dataIndex: 'lastExportedDate',
                flex: 1
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('dataSources.pagingtoolbartop.displayMsg', 'DES', '{0} - {1} of {2} data sources per page'),
                displayMoreMsg: Uni.I18n.translate('dataSources.pagingtoolbartop.displayMoreMsg', 'DES', '{0} - {1} of more than {2} data sources'),
                emptyMsg: Uni.I18n.translate('dataSources.pagingtoolbartop.emptyMsg', 'DES', 'There are no data sources to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('dataSources.pagingtoolbarbottom.itemsPerPage', 'DES', 'Data sources per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});
