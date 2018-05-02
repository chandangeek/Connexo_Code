/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.customtask.History', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.ctk-custom-task-history',

    requires: [
        'Apr.view.customtask.Menu',
        'Apr.view.customtask.HistoryPreview',
        'Apr.view.customtask.HistoryPreviewForm',
        'Apr.view.customtask.PreviewForm',
        'Apr.view.customtask.HistoryGrid',
        'Apr.view.customtask.HistoryFilter'
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
                        xtype: 'ctk-tasks-menu',
                        itemId: 'tasks-view-menu',
                        taskId: me.taskId,
                        router: me.router,
                        detailRoute: me.detailRoute,
                        historyRoute: me.historyRoute,
                        canHistory: me.canHistory,
                        objectType: me.objectType
                    }
                ]
            }
        ];

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('customTask.general.history', 'APR', 'History'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'ctk-tasks-history-grid',
                        itemId: 'ctk-tasks-history-grid',
                        canHistoryLog: me.canHistoryLog,
                        historyActionItemId: me.historyActionItemId,
                        router: me.router,
                        viewLogRoute: me.viewLogRoute
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('customTask.history.empty.title', 'APR', 'No history found'),
                        reasons: [
                            Uni.I18n.translate('customTask.history.empty.list.item1', 'APR', 'There is no history available for this task.'),
                            Uni.I18n.translate('customTask.history.empty.list.item2', 'APR', 'No history items comply with the filter.')
                        ],
                        margin: '16 0 0 0'
                    },
                    previewComponent: {
                        xtype: 'ctk-tasks-history-preview',
                        canHistoryLog: me.canHistoryLog,
                        historyActionItemId: me.historyActionItemId
                    }
                }
            ],
            dockedItems: [
                {
                    dock: 'top',
                    xtype: 'ctk-view-historyfilter'
                }
            ]
        };

        me.callParent(arguments);
    }
});

