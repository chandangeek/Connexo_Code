Ext.define('Bpm.view.task.ViewTask', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.bpm-task-view-task',
    requires: [
        'Uni.property.form.Property'
    ],
    taskRecord: null,
    initComponent: function () {
        var me = this;
        me.content = {
            xtype: 'container',
            layout: 'hbox',
            items: [
                {
                    ui: 'large',
                    title: me.taskRecord.get('name'),
                    flex: 1,
                    items: [
                        {
                            xtype: 'form',
                            title: Uni.I18n.translate('bpm.task.taskManagement', 'BPM', 'Task management'),
                            ui: 'medium',
                            tools: [
                                {
                                    xtype: 'uni-button-action',
                                    privileges: Bpm.privileges.BpmManagement.assignOrExecute,
                                    margin: '20 0 0 0',
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
                                                    fieldLabel: Uni.I18n.translate('bpm.task.assignee', 'BPM', 'Assignee'),
                                                    name: 'actualOwner',
                                                    itemId: 'bpm-task-view-assignee'
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
                                                    fieldLabel: Uni.I18n.translate('bpm.task.deploymentId', 'BPM', 'Deployment ID'),
                                                    name: 'deploymentId',
                                                    itemId: 'bpm-task-view-deployment-id'
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
                            }
                        }
                    ]
                }
            ]
        }

        ;

        me.callParent(arguments);
    }

});

