Ext.define('Dxp.view.tasks.History', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-export-tasks-history',

    requires: [
        'Dxp.view.tasks.Menu',
        'Dxp.view.tasks.HistoryPreview',
        'Dxp.view.tasks.HistoryPreviewForm',
        'Dxp.view.tasks.PreviewForm',
        'Dxp.view.tasks.HistoryGrid',
        'Dxp.view.tasks.HistoryFilter'
    ],

    router: null,
    taskId: null,

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'dxp-tasks-menu',
                        itemId: 'tasks-view-menu',
                        taskId: me.taskId,
                        router: me.router
                    }
                ]
            }
        ];

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.history', 'DES', 'History'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'dxp-tasks-history-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('dataExportTasksHistory.empty.title', 'DES', 'No data export history found'),
                        reasons: [
                            Uni.I18n.translate('dataExportTasksHistory.empty.list.item1', 'DES', 'There is no history available for this data export task.')
                        ],
                        margin: '16 0 0 0'
                    },
                    previewComponent: {
                        xtype: 'dxp-tasks-history-preview'
                    }
                }
            ],
            dockedItems: [
                {
                    dock: 'top',
                    xtype: 'dxp-view-tasks-historyfilter'
                }
            ]
        };

        me.callParent(arguments);
    }
});

