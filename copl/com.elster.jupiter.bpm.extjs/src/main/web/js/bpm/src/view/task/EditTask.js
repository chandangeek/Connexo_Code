/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.view.task.EditTask', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.bpm-task-edit-task',
    requires: [
        'Bpm.store.task.Tasks',
        'Uni.view.toolbar.PreviousNextNavigation',
        'Uni.property.form.Property',
        'Uni.property.form.GroupedPropertyForm',
        'Bpm.store.task.TasksFilterAllUsers',
        'Bpm.store.task.TaskWorkgroupAssignees',
        'Bpm.view.task.AssigneeForm'
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
                                        layout: {
                                            type: 'vbox',
                                            align: 'left'
                                        },
                                        defaults: {
                                            labelWidth: 250,
                                            width: 564
                                        },
                                        items: [
                                            {
                                                xtype: 'assignee-form',
                                                itemId: 'task-assignee-form',
                                                workgroup: {
                                                    dataIndex: 'workgroupId',
                                                    name: 'workgroupId',
                                                    valueField: 'id',
                                                    displayField: 'name',
                                                    store: 'Bpm.store.task.TaskWorkgroupAssignees',
                                                    value: undefined
                                                },
                                                user: {
                                                    dataIndex: 'actualOwner',
                                                    name: 'userId',
                                                    valueField: 'id',
                                                    displayField: 'name',
                                                    store: 'Bpm.store.task.TasksFilterAllUsers'
                                                },
                                                defaults: {
                                                    labelWidth: 250,
                                                    width: 564
                                                },
                                                allUsersUrl: '/api/bpm/runtime/assignees',
                                                workgroupUsersUrl: '/api/bpm/workgroups/{0}/users',
                                                withCheckBox: false
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
                                                        allowBlank: false,
                                                        listeners: {
                                                            blur: me.fieldValidation
                                                        }
                                                    },
                                                    {
                                                        xtype: 'label',
                                                        itemId: 'priority-display',
                                                        margins: '0 0 0 10'
                                                    }
                                                ]
                                            },
                                            {
                                                xtype: 'displayfield',
                                                name: 'statusDisplay',
                                                itemId: 'task-status',
                                                margin: '9 0 0 0',
                                                labelWidth: 250,
                                                fieldLabel: Uni.I18n.translate('task.task.edit.status', 'BPM', 'Status')
                                            },
                                            {
                                                xtype: 'fieldcontainer',
                                                ui: 'actions',
                                                fieldLabel: '&nbsp',
                                                layout: 'hbox',
                                                margin: '10 0 0 150',
                                                items: [
                                                    {
                                                        text: Uni.I18n.translate('task.task.edit.save', 'BPM', 'Save'),
                                                        xtype: 'button',
                                                        ui: 'action',
                                                        itemId: 'btn-task-save',
                                                        action: 'saveTask',
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
                                        name: 'processName',
                                        itemId: 'task-process-name',
                                        labelWidth: 250,
                                        fieldLabel: Uni.I18n.translate('task.task.edit.processName', 'BPM', 'Process name')
                                    },
                                    {
                                        xtype: 'displayfield',
                                        fieldLabel: Uni.I18n.translate('bpm.task.processId', 'BPM', 'Process ID'),
                                        name: 'processInstancesId',
                                        itemId: 'bpm-task-view-process-id',
                                        labelWidth: 250
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'createdOnDisplay',
                                        itemId: 'task-createdOn',
                                        labelWidth: 250,
                                        fieldLabel: Uni.I18n.translate('task.task.edit.creationDate', 'BPM', 'Creation date')
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ]
        me.callParent(arguments);
    },
    fieldValidation: function (field) {
        var value = field.getValue();

        if (Ext.isEmpty(value) || value < field.minValue || value > field.maxValue) {
            field.setValue(field.minValue);
        }
    }

});

