Ext.define('Bpm.monitorprocesses.view.RunningProcessesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.bpm-running-processes-grid',
    store: 'Bpm.monitorprocesses.store.RunningProcesses',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Bpm.monitorprocesses.store.RunningProcesses'
    ],

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('bpm.process.processId', 'BPM', 'Process ID'),
                dataIndex: 'processId',
                flex: 1
            },
            {
                header: Uni.I18n.translate('bpm.process.name', 'BPM', 'Name'),
                dataIndex: 'name',
                flex: 2
            },
            {
                header: Uni.I18n.translate('bpm.process.startedOn', 'BPM', 'Started on'),
                dataIndex: 'startDateDisplay',
                flex: 2
            },
            {
                header: Uni.I18n.translate('bpm.process.status', 'BPM', 'Status'),
                dataIndex: 'statusDisplay'
            },
            {
                header: Uni.I18n.translate('bpm.process.startedBy', 'BPM', 'Started by'),
                dataIndex: 'startedBy',
                flex: 1
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('bpm.runnning.process.pagingtoolbartop.displayMsg', 'BPM', '{0} - {1} of {2} processes'),
                displayMoreMsg: Uni.I18n.translate('bpm.runnning.process.pagingtoolbartop.displayMoreMsg', 'BPM', '{0} - {1} of more than {2} processes'),
                emptyMsg: Uni.I18n.translate('bpm.runnning.process.pagingtoolbartop.emptyMsg', 'BPM', 'There are no process to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('bpm.runnning.process.pagingtoolbarbottom.itemsPerPage', 'BPM', 'Processes per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});
