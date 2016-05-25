Ext.define('Bpm.monitorissueprocesses.view.IssueProcesses', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.bpm-issue-processes',
    requires: [
        'Bpm.monitorissueprocesses.view.IssueProcessesGrid',
        'Bpm.monitorissueprocesses.view.IssueProcessPreview'
    ],

    router: null,
    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'panel',
            itemId: 'bpm-issue-processes-form',
            ui: 'large',
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'bpm-issue-processes-grid',
                        itemId: 'issue-processes-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'issue-processes-empty-grid',
                        title: Uni.I18n.translate('bpm.process.empty.title', 'BPM', 'No processes found'),
                        reasons: [
                            Uni.I18n.translate('bpm.process.empty.runnninglist.item1', 'BPM', 'No processes have been defined yet.'),
                            Uni.I18n.translate('bpm.process.empty.runnninglist.item2', 'BPM', 'Processes exist, but you do not have permission to view them.')
                        ]
                    },
                    previewComponent: {
                        xtype: 'bpm-issue-process-preview',
                        itemId: 'issue-process-preview'

                    }
                }
            ]
        };
        me.callParent(arguments);
    }
});

