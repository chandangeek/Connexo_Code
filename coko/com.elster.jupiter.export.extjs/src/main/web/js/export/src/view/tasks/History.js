/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.view.tasks.History', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-export-tasks-history',

    requires: [
        'Dxp.view.tasks.Menu',
        'Dxp.view.tasks.HistoryPreview',
        'Dxp.view.tasks.HistoryPreviewForm',
        'Dxp.view.tasks.PreviewForm',
        'Dxp.view.tasks.HistoryGrid',
        'Dxp.view.tasks.HistoryFilter',
    ],

    router: null,
    taskId: null,
    showExportTask: true,
    fromWorkspace: false,

    initComponent: function () {
        var me = this;

        if(me.showExportTask){
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
        }


        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.history', 'DES', 'History'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'dxp-tasks-history-grid',
                        itemId: 'data-export-history-grid',
                        showExportTask: me.showExportTask,
                        fromWorkspace: me.fromWorkspace,
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('exportTasksHistory.empty.title', 'DES', 'No export history found'),
                        reasons: [
                            Uni.I18n.translate('exportTasksHistory.empty.list.item1', 'DES', 'There is no history available for this export task.'),
                            Uni.I18n.translate('dataExportTasksHistory.empty.list.item2', 'DES', 'No history items comply with the filter.')
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
                    xtype: 'dxp-view-tasks-historyfilter',
                    itemId: 'dxp-view-tasks-historyfilter-id',
                    showExportTask: me.showExportTask
                }
            ]
        };

        me.callParent(arguments);
    }
});

