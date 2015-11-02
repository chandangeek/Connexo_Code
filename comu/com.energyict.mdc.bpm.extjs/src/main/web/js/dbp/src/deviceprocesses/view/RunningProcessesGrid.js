Ext.define('Dbp.deviceprocesses.view.RunningProcessesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.dbp-running-processes-grid',
    store: 'Dbp.deviceprocesses.store.RunningProcesses',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Dbp.deviceprocesses.store.RunningProcesses'
    ],

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('dbp.process.processId', 'DBP', 'Process ID'),
                dataIndex: 'processId',
                flex: 1
            },
            {
                header: Uni.I18n.translate('dbp.process.name', 'DBP', 'Name'),
                dataIndex: 'name',
                flex: 2
            },
            {
                header: Uni.I18n.translate('dbp.process.startDate', 'DBP', 'Start date'),
                dataIndex: 'startDateDisplay',
                flex: 2
            },
            {
                header: Uni.I18n.translate('dbp.process.status', 'DBP', 'Status'),
                dataIndex: 'statusDisplay'
            },
            {
                header: Uni.I18n.translate('dbp.process.startedBy', 'DBP', 'Started by'),
                dataIndex: 'startedBy',
                flex: 1
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('dbp.runnning.process.pagingtoolbartop.displayMsg', 'DBP', '{0} - {1} of {2} processes'),
                displayMoreMsg: Uni.I18n.translate('dbp.runnning.process.pagingtoolbartop.displayMoreMsg', 'DBP', '{0} - {1} of more than {2} processes'),
                emptyMsg: Uni.I18n.translate('dbp.runnning.process.pagingtoolbartop.emptyMsg', 'DBP', 'There are no process to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('dbp.runnning.process.pagingtoolbarbottom.itemsPerPage', 'DBP', 'Processes per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});
