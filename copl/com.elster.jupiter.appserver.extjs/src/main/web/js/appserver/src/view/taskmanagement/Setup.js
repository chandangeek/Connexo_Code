/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.taskmanagement.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.task-management-setup',
    router: null,
    applicationKey: null,
    requires: [
        'Uni.util.FormEmptyMessage',
        'Uni.grid.commander.SortingPanel',
        'Apr.view.taskmanagement.TaskPreview',
        'Apr.view.taskmanagement.TaskFilter',
        'Apr.view.taskmanagement.TaskGrid',
        'Apr.view.taskmanagement.SortMenu',
        'Apr.TaskManagementApp'
    ],

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('general.taskManagement.title', 'APR', 'Tasks'),
                items: [
                    {
                        xtype: 'task-management-filter',
                        applicationKey: me.applicationKey,
                        queuesStore: me.queuesStore,
                        queueTypesStore: me.queueTypesStore
                    },
                    {
                        store: 'Apr.store.Tasks',
                        xtype: 'uni-grid-commander-sortingpanel',
                        itemId: 'taskmanagement-sortingpanel',
                        menu: 'taskmanagement-sort-menu',
                        items: [
                          {
                            property: 'nextRun',
                            direction: Uni.component.sort.model.Sort.ASC
                          },
                          {
                            property: 'queue',
                            direction: Uni.component.sort.model.Sort.ASC
                          },
                          {
                            property: 'priority',
                            direction: Uni.component.sort.model.Sort.DESC
                          }
                        ]
                    },
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'task-management-grid',
                            itemId: 'task-management-grid',
                            addTaskRoute: me.addTaskRoute,
                            router: me.router
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'no-tasks-found',
                            title: Uni.I18n.translate('taskManagement.empty.title', 'APR', 'No tasks found'),
                            reasons: [
                                Uni.I18n.translate('taskManagement.list.item1', 'APR', 'There are no tasks in the system.'),
                                Uni.I18n.translate('taskManagement.list.item2', 'APR', 'No tasks comply with the filter.')
                            ],
                            stepItems: [
                                {
                                    text: Uni.I18n.translate('taskManagement.general.addTask', 'APR', 'Add task'),
                                    privileges: function () {
                                        return Apr.TaskManagementApp.canAdministrate();
                                    },
                                    href: me.addTaskRoute
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'task-management-preview',
                            itemId: 'task-management-preview'
                        }
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});
