/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.validationtask.History', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.cfg-validation-tasks-history',

    requires: [
        'Cfg.view.validationtask.Menu',
        'Cfg.view.validationtask.HistoryPreview',
        'Cfg.view.validationtask.HistoryPreviewForm',
        'Cfg.view.validationtask.PreviewForm',
        'Cfg.view.validationtask.HistoryGrid',
        'Cfg.view.validationtask.HistoryFilter'
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
                        xtype: 'cfg-tasks-menu',
                        itemId: 'tasks-view-menu',
                        taskId: me.taskId,
                        router: me.router,
                        detailRoute: me.detailRoute,
                        historyRoute: me.historyRoute
                    }
                ]
            }
        ];

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('validationTasks.general.history', 'CFG', 'History'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'cfg-tasks-history-grid',
                        historyActionItemId: me.historyActionItemId,
                        router: me.router,
                        viewLogRoute: me.viewLogRoute
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('validationTasks.validationTasksHistory.empty.title', 'CFG', 'No validation history found'),
                        reasons: [
                            Uni.I18n.translate('validationTasks.validationTasksHistory.empty.list.item1', 'CFG', 'There is no history available for this validation task.'),
                            Uni.I18n.translate('validationTasks.validationTasksHistory.empty.list.item2', 'CFG', 'No history items comply with the filter.')
                        ],
                        margin: '16 0 0 0'
                    },
                    previewComponent: {
                        xtype: 'cfg-tasks-history-preview',
                        historyActionItemId: me.historyActionItemId
                    }
                }
            ],
            dockedItems: [
                {
                    dock: 'top',
                    xtype: 'cfg-view-validationtask-historyfilter'
                }
            ]
        };

        me.callParent(arguments);
    }
});

