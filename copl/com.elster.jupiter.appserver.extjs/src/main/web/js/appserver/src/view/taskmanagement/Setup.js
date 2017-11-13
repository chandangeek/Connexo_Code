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
        'Apr.view.taskmanagement.TaskPreview',
        'Apr.view.taskmanagement.TaskFilter',
        'Apr.view.taskmanagement.TaskGrid',
        'Apr.TaskManagementApp'
    ],

    initComponent: function () {
        var me = this;

        me.content = [
            {
                ui: 'large',
                title: Uni.I18n.translate('general.taskManagement.title', 'APR', 'Tasks'),
                items: [
                    {
                        xtype: 'task-management-filter',
                        applicationKey: me.applicationKey
                    },
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'task-management-grid',
                            itemId: 'task-management-grid',
                            addTaskRoute: me.addTaskRoute
                        },
                        emptyComponent: {
                            xtype: 'uni-form-empty-message',
                            itemId: 'no-tasks-found',
                            text: Uni.I18n.translate('taskManagement.empty', 'APR', 'There are no tasks in the system')
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
