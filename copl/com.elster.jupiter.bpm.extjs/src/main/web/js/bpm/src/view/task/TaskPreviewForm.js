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
                                fieldLabel: Uni.I18n.translate('bpm.task.creationDate', 'BPM', 'Creation date'),
                                name: 'createdOnDisplay',
                                itemId: 'bpm-preview-createdOn'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('bpm.task.usagePoint', 'BPM', 'Usage point'),
                                name: 'usagePointId',
                                itemId: 'bpm-task-view-usagePointId',
                                usagePointLink: null,
                                renderer: function (value) {
                                    var appName = 'Insight',
                                        url;
                                    if (value) {
                                        this.show();
                                        if (Ext.isEmpty(value)) {
                                            return '-'
                                        } else {
                                            if (Uni.store.Apps.checkApp(appName)) {
                                                url = Ext.String.format('{0}/usagepoints/{1}', Uni.store.Apps.getAppUrl(appName), encodeURIComponent(value));
                                                return Ext.String.format('<a href="{0}">{1}</a>', url, Ext.String.htmlEncode(value));
                                            }
                                            return Ext.String.htmlEncode(value);
                                        }
                                    } else {
                                        this.hide();
                                        return null;
                                    }
                                }
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('bpm.task.device', 'BPM', 'Device'),
                                name: 'deviceId',
                                itemId: 'bpm-task-view-Device',
                                usagePointLink: null,
                                renderer: function (value) {
                                    var appName = 'Multisense',
                                        url;
                                    if (value) {
                                        this.show();
                                        if (Ext.isEmpty(value)) {
                                            return '-'
                                        } else {
                                            if (Uni.store.Apps.checkApp(appName)) {
                                                url = Ext.String.format('{0}/devices/{1}', Uni.store.Apps.getAppUrl(appName), encodeURIComponent(value));
                                                return Ext.String.format('<a href="{0}">{1}</a>', url, Ext.String.htmlEncode(value));
                                            }
                                            return Ext.String.htmlEncode(value);
                                        }
                                    } else {
                                        this.hide();
                                        return null;
                                    }
                                }
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent();
    }
});
