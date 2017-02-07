/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.view.task.TaskPreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.bpm-task-preview-form',

    requires: [
    ],

    defaults: {
        labelWidth: 250
    },

    initComponent: function () {
        var me = this;
        me.items = [
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
                            labelWidth: 150
                        },
                        items: [
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('bpm.task.workgroupAssignee', 'BPM', 'Workgroup'),
                                name: 'workgroup',
                                itemId: 'bpm-preview-workgroup-assignee',
                                renderer: function (value) {
                                    return value ? Ext.String.htmlEncode(value) : Uni.I18n.translate('general.unassigned', 'BPM', 'Unassigned');
                                }
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('bpm.task.userAssignee', 'BPM', 'User'),
                                name: 'actualOwner',
                                itemId: 'bpm-preview-user-assignee',
                                renderer: function (value) {
                                    return value ? Ext.String.htmlEncode(value) : Uni.I18n.translate('general.unassigned', 'BPM', 'Unassigned');
                                }
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('bpm.task.dueDate', 'BPM', 'Due date'),
                                name: 'dueDateDisplay',
                                itemId: 'bpm-preview-due-date'
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
                                itemId: 'bpm-preview-status'
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
                            labelWidth: 150
                        },
                        items: [
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('bpm.task.process', 'BPM', 'Process'),
                                name: 'processName',
                                itemId: 'bpm-preview-process-name'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('bpm.task.processId', 'BPM', 'Process ID'),
                                name: 'processInstancesId',
                                itemId: 'bpm-preview-process-id'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('bpm.task.creationDate', 'BPM', 'Creation date'),
                                name: 'createdOnDisplay',
                                itemId: 'bpm-preview-createdOn'
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent();
    }
});
