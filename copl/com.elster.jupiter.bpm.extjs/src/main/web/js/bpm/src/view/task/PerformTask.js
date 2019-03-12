/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
                xtype: 'container',
                itemId: 'frm-task',
                ui: 'large',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'uni-form-empty-message',
                        text: Uni.I18n.translate('bpm.task.notAllowedToStartTask', 'BPM', 'You are not allowed to start this task because you are not assigned.'),
                        hidden: true,
                        margin: '0 0 0 0'
                    },
                    {
                        xtype: 'form',
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

