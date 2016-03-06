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
    margin: '0 0 0 15',
    border: true,
    items: [
        {
            xtype: 'bpm-issue-processes',
            margin: '0 0 0 -15',
            itemId: 'issue-processes'
        },
    ],
    initComponent: function () {
        var me = this;
        me.fireEvent('initStores',this.properties);
        me.callParent(arguments);
    },
    /*listeners: {
        'afterrender': function () {
            this.fireEvent('initComponents', this);

        }
    }*/
});