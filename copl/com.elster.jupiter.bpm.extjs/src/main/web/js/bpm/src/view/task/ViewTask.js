/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.view.task.ViewTask', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.bpm-task-view-task',
    requires: [
        'Uni.property.form.Property',
        'Bpm.store.task.Tasks',
        'Uni.view.toolbar.PreviousNextNavigation',
        'Uni.util.FormEmptyMessage'
    ],
    taskRecord: null,
    router: null,
    listLink: null,
    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'container',
                layout: 'hbox',
                items: [
                    {
                        ui: 'large',
                        itemId: 'task-title',
                        flex: 1
                    },
                    {
                        xtype: 'previous-next-navigation-toolbar',
                        margin: '15 0 0 0',
                        itemId: 'bpm-tasks-previous-next-navigation-toolbar',
                        store: 'Bpm.store.task.Tasks',
                        router: me.router,
                        routerIdArgument: 'taskId',
                        itemsName: me.listLink
                    }
                ]
            },
            {

                xtype: 'form',
                layout: {
                    type: 'fit'
                },
                title: Uni.I18n.translate('bpm.task.taskAttributes', 'BPM', 'Task attributes'),
                ui: 'medium',
                margin: '-4 0 0 0',
                flex: 1,
                tools: [
                    {
                        xtype: 'uni-button-action',
                        privileges: Bpm.privileges.BpmManagement.assignOrExecute,
                        margin: '20 -15 0 0',
                        menu: {
                            xtype: 'bpm-task-action-menu',
                            record: me.taskRecord
                        }
                    }
                ],
                items: [
                    {
                        xtype: 'container',
                        layout: {
                            type: 'column',
                            align: 'stretch'
                        },
                        flex: 1,
                        items: [
                            {
                                xtype: 'container',
                                columnWidth: 0.5,
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                defaults: {
                                    labelWidth: 250
                                },
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        fieldLabel: Uni.I18n.translate('bpm.task.workgroupAssignee', 'BPM', 'Workgroup'),
                                        name: 'workgroup',
                                        itemId: 'bpm-task-view-workgroup-assignee',
                                        renderer: function (value) {
                                            return value ? Ext.String.htmlEncode(value) : Uni.I18n.translate('general.unassigned', 'BPM', 'Unassigned');
                                        }
                                    },
                                    {
                                        xtype: 'displayfield',
                                        fieldLabel: Uni.I18n.translate('bpm.task.userAssignee', 'BPM', 'User'),
                                        name: 'actualOwner',
                                        itemId: 'bpm-task-view-user-assignee',
                                        renderer: function (value) {
                                            return value ? Ext.String.htmlEncode(value) : Uni.I18n.translate('general.unassigned', 'BPM', 'Unassigned');
                                        }
                                    },
                                    {
                                        xtype: 'displayfield',
                                        fieldLabel: Uni.I18n.translate('bpm.task.dueDate', 'BPM', 'Due date'),
                                        name: 'dueDateDisplay',
                                        itemId: 'bpm-task-view-due-date'
                                    },
                                    {
                                        xtype: 'displayfield',
                                        fieldLabel: Uni.I18n.translate('bpm.task.priority', 'BPM', 'Priority'),
                                        name: 'priorityDisplay',
                                        itemId: 'bpm-task-view-priority'
                                    },
                                    {
                                        xtype: 'displayfield',
                                        fieldLabel: Uni.I18n.translate('bpm.task.status', 'BPM', 'Status'),
                                        name: 'statusDisplay',
                                        itemId: 'bpm-task-view-status'
                                    }
                                ]
                            },
                            {
                                xtype: 'container',
                                columnWidth: 0.5,
                                itemId: 'ctn-user-directory-properties2',
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                defaults: {
                                    labelWidth: 250
                                },
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        fieldLabel: Uni.I18n.translate('bpm.task.process', 'BPM', 'Process'),
                                        name: 'processName',
                                        itemId: 'bpm-task-view-process-name'
                                    },
                                    {
                                        xtype: 'displayfield',
                                        fieldLabel: Uni.I18n.translate('bpm.task.processId', 'BPM', 'Process ID'),
                                        name: 'processInstancesId',
                                        itemId: 'bpm-task-view-process-id'
                                    },
                                    {
                                        xtype: 'displayfield',
                                        fieldLabel: Uni.I18n.translate('bpm.task.creationDate', 'BPM', 'Creation date'),
                                        name: 'createdOnDisplay',
                                        itemId: 'bpm-task-view-createdOn'
                                    }
                                ]
                            }

                        ]
                    }
                ]
            },
            {
                title: Uni.I18n.translate('bpm.task.taskExecution', 'BPM', 'Task execution'),
                ui: 'medium',
                xtype: 'property-form',
                isEdit: false,
                isReadOnly: true,
                inheritedValues: true,
                defaults: {
                    resetButtonHidden: true,
                    labelWidth: 250
                },
                items: [
                    {
                        xtype: 'uni-form-empty-message',
                        text: Uni.I18n.translate('bpm.task.taskExecutionNoAttributes', 'BPM', 'This task has no task execution attributes.'),
                        hidden: true,
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }

});

