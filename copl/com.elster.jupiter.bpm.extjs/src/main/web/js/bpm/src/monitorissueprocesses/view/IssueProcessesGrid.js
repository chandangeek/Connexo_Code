Ext.define('Bpm.monitorissueprocesses.view.IssueProcessesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.bpm-issue-processes-grid',
    store: 'Bpm.monitorissueprocesses.store.IssueProcesses',
    requires: [

        'Bpm.monitorissueprocesses.store.IssueProcesses'
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

        me.callParent(arguments);
    }
});
