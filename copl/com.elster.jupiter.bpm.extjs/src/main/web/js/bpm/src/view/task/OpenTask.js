Ext.define('Bpm.view.task.OpenTask', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.bpm-task-open-task',
    requires: [
        'Bpm.store.task.Tasks',
        'Uni.view.toolbar.PreviousNextNavigation',
        'Uni.property.form.Property',
        'Uni.property.form.GroupedPropertyForm'
    ],
    taskRecord: null,
    edit: false,
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
                    },
                    {
                        xtype: 'previous-next-navigation-toolbar',
                        margin: '10 0 0 0',
                        store: 'Bpm.store.task.Tasks',
                        router: me.router,
                        routerIdArgument: 'taskId',
                        itemsName: me.itemNameLink,
                    }
                ]
            },
            {
                xtype: 'form',
                itemId: 'frm-open-task',
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
                                        privileges: Bpm.privileges.BpmManagement.assign,
                                        items: [
                                            {
                                                xtype: 'combobox',
                                                dataIndex: 'actualOwner',
                                                fieldLabel: Uni.I18n.translate('bpm.task.assignee', 'BPM', 'Assignee'),
                                                emptyText: Uni.I18n.translate('bpm.task.unassignee', 'BPM', 'Unassigned'),
                                                multiSelect: false,
                                                displayField: 'name',
                                                valueField: 'name',
                                                itemId: 'cbo-assignee-user',
                                                store: 'Bpm.store.task.TasksFilterUsers',
                                                width: 500,
                                                labelWidth: 250,
                                                editable: false,
                                                queryMode: 'local',
                                                name: 'assignee',
                                                required: true
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
                                                xtype: 'fieldcontainer',
                                                fieldLabel: Uni.I18n.translate('task.task.edit.dueDate', 'BPM', 'Due date'),
                                                layout: 'hbox',
                                                margin: '0 0 10 150',
                                                items: [
                                                    {
                                                        xtype: 'date-time',
                                                        itemId: 'due-date',
                                                        layout: 'hbox',
                                                        name: 'dueDateParsed',
                                                        allowBlank: true,
                                                        dateConfig: {

                                                            allowBlank: true,
                                                            format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                                                        },
                                                        hoursConfig: {
                                                            fieldLabel: Uni.I18n.translate('task.task.edit.dueDate.at', 'BPM', 'at'),
                                                            labelWidth: 10,
                                                            margin: '0 0 0 10'
                                                        },
                                                        minutesConfig: {
                                                            width: 55
                                                        }
                                                    }
                                                ]
                                            },
                                            {
                                                xtype: 'container',
                                                layout: {
                                                    type: 'hbox',
                                                    align: 'left'
                                                },
                                                items: [
                                                    {
                                                        xtype: 'numberfield',
                                                        itemId: 'num-priority',
                                                        minValue: 0,
                                                        maxValue: 10,
                                                        name: 'priority',
                                                        width: 330,
                                                        fieldLabel: Uni.I18n.translate('task.task.edit.priority', 'BPM', 'Priority'),
                                                        labelWidth: 250,
                                                        allowBlank: false
                                                    },
                                                    {
                                                        xtype: 'label',
                                                        itemId: 'priority-display',
                                                        margins: '0 0 0 10'
                                                    }
                                                ]
                                            },
                                            {
                                                text: Uni.I18n.translate('task.task.edit.save', 'BPM', 'Save'),
                                                xtype: 'button',
                                                ui: 'action',
                                                itemId: 'btn-task-save',
                                                action: 'saveTask',
                                                margin: '10 0 0 265',
                                                taskRecord: me.taskRecord
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
                                        xtype: 'grouped-property-form'
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
                                                text: Uni.I18n.translate('task.action.release', 'BPM', 'Release'),
                                                xtype: 'button',
                                                hidden: true,
                                                ui: 'action',
                                                itemId: 'btn-release',
                                                action: 'releaseTask',
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

