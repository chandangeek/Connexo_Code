Ext.define('Bpm.view.task.PerformTask', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.bpm-task-perform-task',
    requires: [
        'Bpm.store.task.Tasks',
        'Bpm.store.task.TasksFilterAllUsers',
        'Uni.view.toolbar.PreviousNextNavigation',
        'Uni.property.form.Property',
        'Uni.property.form.GroupedPropertyForm'
    ],
    taskRecord: null,
    showNavigation: true,
    router: null,
    itemNameLink: '',
    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'container',
                layout: 'hbox',
                items: [
                    {
                        itemId: 'detail-top-title',
                        ui: 'large',
                        flex: 1
                    }
                ]
            },
            {
                xtype: 'form',
                itemId: 'frm-task',
                ui: 'large',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'form',
                        title: Uni.I18n.translate('bpm.task.taskManagement', 'BPM', 'Task management'),
                        ui: 'medium',
                        layout: {
                            type: 'column',
                            align: 'stretch'
                        },
                        privileges: Bpm.privileges.BpmManagement.assign,
                        items: [
                            {
                                xtype: 'container',
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                columnWidth: 0.5,
                                items: [
                                    {
                                        xtype: 'form',
                                        itemId: 'frm-assignee-user',
                                        margin: '0 0 0 0',
                                        layout: {
                                            type: 'hbox',
                                            align: 'left'
                                        },
                                        items: [
                                            {
                                                xtype: 'displayfield',
                                                name: 'actualOwnerDisplay',
                                                fieldLabel: Uni.I18n.translate('bpm.task.assignee', 'BPM', 'Assignee'),
                                                itemId: 'cbo-assignee-user',
                                                width: 500,
                                                labelWidth: 250
                                            }
                                        ]
                                    },
                                    {
                                        xtype: 'form',
                                        itemId: 'frm-edit-task',
                                        margin: '10 0 0 0',
                                        layout: {
                                            type: 'vbox',
                                            align: 'left'
                                        },
                                        privileges: Bpm.privileges.BpmManagement.view,
                                        items: [
                                            {
                                                xtype: 'displayfield',
                                                fieldLabel: Uni.I18n.translate('task.task.edit.dueDate', 'BPM', 'Due date'),
                                                name: 'dueDateDisplay',
                                                width: 500,
                                                labelWidth: 250,
                                                itemId: 'due-date'
                                            },
                                            {
                                                xtype: 'displayfield',
                                                fieldLabel: Uni.I18n.translate('task.task.edit.priority', 'BPM', 'Priority'),
                                                name: 'priorityDisplay',
                                                width: 500,
                                                labelWidth: 250,
                                                itemId: 'priority'
                                            }
                                        ]
                                    }
                                ]
                            },
                            {
                                xtype: 'form',
                                itemId: 'frm-about-task',
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                columnWidth: 0.5,
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        name: 'statusDisplay',
                                        itemId: 'task-status',
                                        width: 500,
                                        labelWidth: 250,
                                        fieldLabel: Uni.I18n.translate('task.task.edit.status', 'BPM', 'Status')
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'processName',
                                        itemId: 'task-process-name',
                                        width: 500,
                                        labelWidth: 250,
                                        fieldLabel: Uni.I18n.translate('task.task.edit.processName', 'BPM', 'Process name')
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'createdOnDisplay',
                                        itemId: 'task-createdOn',
                                        width: 500,
                                        labelWidth: 250,
                                        fieldLabel: Uni.I18n.translate('task.task.edit.creationDate', 'BPM', 'Creation date')
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'form',
                        title: Uni.I18n.translate('bpm.task.taskExecution', 'BPM', 'Task execution'),
                        ui: 'medium',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        privileges: Bpm.privileges.BpmManagement.execute,
                        itemId: 'task-execution-form',
                        items: [
                            {
                                xtype: 'container',
                                margin: '20 0 0 0',
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                itemId: 'task-execution-content',
                                privileges: Bpm.privileges.BpmManagement.execute,
                                items: [
                                    {
                                        xtype: 'property-form',
                                        defaults: {
                                            labelWidth: 250,
                                            width: 268
                                        }
                                    },
                                    {
                                        xtype: 'container',
                                        margin: '10 0 0 265',
                                        layout: 'hbox',
                                        items: [
                                            {
                                                text: Uni.I18n.translate('task.action.save', 'BPM', 'Save'),
                                                xtype: 'button',
                                                hidden: true,
                                                ui: 'action',
                                                itemId: 'btn-save',
                                                action: 'saveTask',
                                                taskRecord: me.taskRecord
                                            },
                                            {
                                                text: Uni.I18n.translate('task.action', 'BPM', 'Start'),
                                                xtype: 'button',
                                                hidden: true,
                                                ui: 'action',
                                                itemId: 'btn-start',
                                                action: 'startTask',
                                                taskRecord: me.taskRecord
                                            },
                                            {
                                                text: Uni.I18n.translate('task.action.complete', 'BPM', 'Complete'),
                                                xtype: 'button',
                                                hidden: true,
                                                ui: 'action',
                                                itemId: 'btn-complete',
                                                action: 'completeTask',
                                                taskRecord: me.taskRecord
                                            },
                                            {
                                                xtype: 'button',
                                                itemId: 'btn-task-cancel-link',
                                                text: Uni.I18n.translate('general.cancel', 'BPM', 'Cancel'),
                                                ui: 'link'
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ]
        me.callParent(arguments);
    }
});

