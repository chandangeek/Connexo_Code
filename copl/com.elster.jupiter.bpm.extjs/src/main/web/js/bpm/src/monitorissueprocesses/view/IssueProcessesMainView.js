Ext.define('Bpm.monitorissueprocesses.view.IssueProcessesMainView', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bpm-issue-processes-main-view',
    requires: [
        'Bpm.monitorissueprocesses.view.IssueProcesses',
        'Bpm.monitorissueprocesses.view.IssueProcessPreview',
    ],
    properties: {},
    title: Uni.I18n.translate('processes.title', 'BPM', 'Processes'),
    ui: 'large',
    items: [
        {
            xtype: 'bpm-issue-processes',
            itemId: 'issue-processes'
        },
    ],
    initComponent: function () {
        var me = this;

        me.fireEvent('initStores',this.issueId);
        me.callParent(arguments);

    },
    listeners: {
        'afterrender': function () {
            this.fireEvent('initComponents', this);

        }
    }
});