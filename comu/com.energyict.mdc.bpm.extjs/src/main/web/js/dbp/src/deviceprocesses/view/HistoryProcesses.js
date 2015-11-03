Ext.define('Dbp.deviceprocesses.view.HistoryProcesses', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.dbp-history-processes',
    requires: [
        'Dbp.deviceprocesses.view.HistoryProcessPreview',
        'Dbp.deviceprocesses.view.HistoryProcessesGrid',
        'Dbp.deviceprocesses.view.HistoryTopFilter'
    ],

    router: null,
    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'panel',
            itemId: 'dbp-history-processes-form',
            ui: 'large',
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'dbp-history-processes-grid',
                        itemId: 'history-processes-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'history-processes-empty-grid',
                        title: Uni.I18n.translate('dbp.process.empty.title', 'DBP', 'No processes found'),
                        reasons: [
                            Uni.I18n.translate('dbp.process.empty.list.item1', 'DBP', 'No processes have been defined yet.'),
                            Uni.I18n.translate('dbp.process.empty.list.item2', 'DBP', 'Processes exist, but you do not have permission to view them.'),
                            Uni.I18n.translate('dbp.process.empty.list.item3', 'DBP', 'The filter criteria are too narrow.')
                        ]
                    },
                    previewComponent: {
                        xtype: 'dbp-history-process-preview',
                        itemId: 'history-process-preview'
                    }
                }
            ],
            dockedItems: [
                {
                    dock: 'top',
                    xtype: 'dbp-view-history-processes-topfilter',
                    itemId: 'dbp-view-history-processes-topfilter'
                }
            ]
        };
        me.callParent(arguments);
    }
});

