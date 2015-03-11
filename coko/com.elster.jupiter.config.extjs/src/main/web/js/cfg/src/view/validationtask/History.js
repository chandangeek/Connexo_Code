Ext.define('Cfg.view.validationtask.History', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-validation-tasks-history',
    requires: [
        'Cfg.view.validationtask.Menu',
        'Cfg.view.validationtask.HistoryPreview',
        'Cfg.view.validationtask.HistoryPreviewForm',
        'Cfg.view.validationtask.PreviewForm',
        'Cfg.view.validationtask.HistoryGrid',
        'Cfg.view.validationtask.HistoryFilterForm',
        'Uni.component.filter.view.FilterTopPanel'
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
                        xtype: 'tasks-menu',
                        itemId: 'tasks-view-menu',
                        taskId: me.taskId,
                        router: me.router
                    },
                    {
                        xtype: 'history-filter-form',
                        itemId: 'side-filter',
                        router: me.router
                    }
                ]
            }
        ];

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('dataValidationTasks.general.history', 'CFG', 'History'),
            items: [
                {
                    xtype: 'filter-top-panel',
                    itemId: 'tasks-history-filter-top-panel'
                },
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'tasks-history-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('dataValidationTasks.dataValidationTasksHistory.empty.title', 'CFG', 'No data validation history found'),
                        reasons: [
                            Uni.I18n.translate('dataValidationTasks.dataValidationTasksHistory.empty.list.item1', 'CFG', 'There is no history available for this data validation task.')
                        ]
                    },
                    previewComponent: {
                        xtype: 'tasks-history-preview'
                    }
                }
            ]
        };
        me.callParent(arguments);
    }
});

