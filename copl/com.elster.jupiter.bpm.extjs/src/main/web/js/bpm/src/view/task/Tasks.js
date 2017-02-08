/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.view.task.Tasks', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.bpm-tasks',
    requires: [
        'Bpm.view.task.TaskPreview',
        'Bpm.view.task.TasksTopFilter',
        'Bpm.view.task.TasksGrid',
        // 'Bpm.view.task.Menu',
        'Bpm.view.task.TaskPreviewForm',
        'Bpm.view.task.TasksSortMenu'
    ],

    router: null,
    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'panel',
            itemid: 'bpm-tasks-form',
            ui: 'large',
            title: Uni.I18n.translate('general.tasks', 'BPM', 'Tasks'),
            items: [
                {
                    xtype: 'filter-toolbar',
                    title: Uni.I18n.translate('bpm.filter.sort', 'BPM', 'Sort'),
                    name: 'sortitemspanel',
                    itemId: 'bpm-task-sort-toolbar',
                    emptyText: Uni.I18n.translate('general.none', 'BPM', 'None'),
                    tools: [
                        {
                            xtype: 'button',
                            action: 'addSort',
                            text: Uni.I18n.translate('general.task.addSort', 'BPM', 'Add sort'),
                            menu: {
                                xtype: 'bpm-tasks-sort-menu',
                                itemId: 'menu-tasks-sort',
                                name: 'addsortitemmenu'
                            }
                        }
                    ]
                },
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'bpm-tasks-grid',
                        itemId: 'bpm-tasks-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'bpm-tasks-empty-grid',
                        title: Uni.I18n.translate('bpm.task.empty.title', 'BPM', 'No tasks found'),
                        reasons: [
                            Uni.I18n.translate('bpm.task.empty.list.item1', 'BPM', 'No tasks have been created yet.'),
                            Uni.I18n.translate('bpm.task.empty.list.item2', 'BPM', 'Tasks exist, but you do not have permission to view them.'),
                            Uni.I18n.translate('bpm.task.empty.list.item3', 'BPM', 'No tasks comply with the filter.'),
                            Uni.I18n.translate('bpm.task.empty.list.item4', 'BPM', 'Connexo Flow is not available.')
                        ]
                    },
                    previewComponent: {
                        xtype: 'bpm-task-preview',
                        itemId: 'bpm-task-preview'
                    }
                }
            ],
            dockedItems: [
                {
                    dock: 'top',
                    xtype: 'bpm-view-tasks-topfilter',
                    itemId: 'bpm-view-tasks-topfilter'
                }
            ]
        };
        me.callParent(arguments);
    }
});

