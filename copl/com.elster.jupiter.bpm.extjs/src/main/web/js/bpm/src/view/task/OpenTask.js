Ext.define('Bpm.view.task.OpenTask', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.bpm-task-open-task',
    requires: [
        'Bpm.store.task.Tasks',
        'Bpm.store.task.Priorities'
    ],
    taskRecord: null,
    edit: false,

    initComponent: function () {
        var me = this;
        me.content = [
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
                                emptyText: Uni.I18n.translate('bpm.task.assignee', 'BPM', 'Assignee'),
                                multiSelect: false,
                                displayField: 'name',
                                valueField: 'name',
                                itemId: 'cbo-assignee-user',
                                store: 'Bpm.store.task.TasksFilterUsers',
                                width: 500,
                                labelWidth: 250,
                                editable: false ,
                                queryMode: 'local',
                                name: 'assignee'
                            },
                            {
                                text: Uni.I18n.translate('task.task.assignee.save', 'BPM', 'Save'),
                                xtype: 'button',
                                ui: 'action',
                                itemId: 'btn-assignee-user-save',
                                action: 'saveAssigneeUser',
                                margin: '0 0 0 80',
                                taskRecord: me.taskRecord
                            }
                        ]
                    },
                    {
                        xtype: 'form',
                        itemId: 'frm-edit-task',
                        margin: '20 0 0 0',
                        layout: {
                            type: 'vbox',
                            align: 'left'
                        },
                        privileges: Bpm.privileges.BpmManagement.view,
                        items: [
                            {
                                xtype: 'textfield',
                                name: 'statusDisplay',
                                itemId: 'task-status',
                                disabled: true,
                                width: 500,
                                labelWidth: 250,
                                fieldLabel: Uni.I18n.translate('task.task.edit.status', 'BPM', 'Status')
                            },
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
                                margin: '0 0 0 0',
                                layout: {
                                    type: 'hbox',
                                    align: 'left'
                                },
                                items: [
                                    {
                                        xtype: 'combobox',
                                        itemId: 'cbo-priority',
                                        name: 'priorityTranslation',
                                        width: 500,
                                        fieldLabel: Uni.I18n.translate('task.task.edit.priority', 'BPM', 'Priority'),
                                        labelWidth: 250,
                                        store: 'Bpm.store.task.Priorities',
                                        editable: false,
                                        emptyText: Uni.I18n.translate('task.task.edit.priority', 'BPM', 'Priority'),
                                        allowBlank: false,
                                        queryMode: 'local',
                                        displayField: 'name',
                                        valueField: 'value'
                                    },
                                    {
                                        text: Uni.I18n.translate('task.task.edit.save', 'BPM', 'Save'),
                                        xtype: 'button',
                                        ui: 'action',
                                        itemId: 'btn-task-save',
                                        action: 'saveTask',
                                        margin: '0 0 0 80',
                                        taskRecord: me.taskRecord
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'container',
                        itemId: 'frm-form-container',
                        margin: '20 0 0 0',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        privileges: Bpm.privileges.BpmManagement.execute,
                        items:[
                            {
                                xtype: 'container',
                                margin: 10,
                                padding: 10,
                                border: 1,
                                //itemId: 'frm-open-task-container',
                                style: {
                                    borderColor: 'lightgray',
                                    borderStyle: 'solid'
                                },
                                height: 320,
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                items: [
                                    {
                                        xtype: 'container',
                                        itemId: 'formContent',
                                        height: 300
                                    }
                                ]
                            },
                            {
                                xtype: 'container',
                                margin: '10 0 0 0',
                                layout: 'hbox',
                                items: [
                                    {
                                        text: Uni.I18n.translate('task.action.claim', 'BPM', 'Claim'),
                                        xtype: 'button',
                                        hidden: true,
                                        ui: 'action',
                                        itemId: 'btn-claim',
                                        action: 'claimTask',
                                        taskRecord: me.taskRecord
                                    },
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
                                    },
                                    {
                                        text: Uni.I18n.translate('task.action.taskactions', 'BPM', 'Task actions'),
                                        xtype: 'button',
                                        hidden: true,
                                        ui: 'action',
                                        itemId: 'btn-taskactions',
                                        action: 'taskaction',
                                        taskRecord: me.taskRecord
                                    }/*,
                                     {
                                     xtype: 'button',
                                     text: Uni.I18n.translate('general.cancel', 'BPM', 'Cancel'),
                                     href: '#/administration/taksmanagementtasks',
                                     itemId: 'btn-cancel-link',
                                     ui: 'link'
                                     }*/
                                ]
                            }

                        ]
                    },

                ]
            }
        ];
        me.callParent(arguments);
    //    me.setEdit(me.edit, me.returnLink);
    }
});

