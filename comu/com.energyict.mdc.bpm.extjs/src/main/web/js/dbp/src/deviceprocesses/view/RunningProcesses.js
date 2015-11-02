Ext.define('Dbp.deviceprocesses.view.RunningProcesses', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.dbp-running-processes',
    requires: [
        'Dbp.deviceprocesses.view.RunningProcessPreview',
        'Dbp.deviceprocesses.view.RunningProcessesGrid'
    ],

    router: null,
    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'panel',
            itemId: 'dbp-running-processes-form',
            ui: 'large',
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'dbp-running-processes-grid',
                        itemId: 'running-processes-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'running-processes-empty-grid',
                        title: Uni.I18n.translate('dbp.process.empty.title', 'DBP', 'No processes found'),
                        reasons: [
                            Uni.I18n.translate('dbp.process.empty.list.item1', 'DBP', 'No processes have been created yet.'),
                            Uni.I18n.translate('dbp.process.empty.list.item2', 'DBP', 'Processes exist, but you do not have permission to view them.'),
                            Uni.I18n.translate('dbp.process.empty.list.item3', 'DBP', 'No running proccess has been found.')
                        ]
                    },
                    previewComponent: {
                        xtype: 'dbp-running-process-preview',
                        itemId: 'running-process-preview'
                    }
                }
            ]
        };
        me.callParent(arguments);
    }
});

