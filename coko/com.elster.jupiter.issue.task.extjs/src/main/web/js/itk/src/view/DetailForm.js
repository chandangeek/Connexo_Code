/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.view.DetailForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.task-issue-detail-form',
    layout: 'column',
    defaults: {
        xtype: 'container',
        layout: 'form',
        columnWidth: 0.5
    },
    router: null,
    initComponent: function () {
        var me = this;

        me.items = [
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        itemId: 'task-issue-id',
                        fieldLabel: Uni.I18n.translate('general.title.issueId', 'ITK', 'ID'),
                        name: 'issueId'
                    },
                    {
                        itemId: 'task-issue-reason',
                        fieldLabel: Uni.I18n.translate('general.title.reason', 'ITK', 'Reason'),
                        name: 'reason_name'
                    },
                    {
                        itemId: 'task-issue-detail-recurrent-task',
                        fieldLabel: Uni.I18n.translate('general.title.task', 'ITK', 'Task'),
                        name: 'recurrentTask',
                        renderer: function (record) {
                            var url = '',
                                result = '';
                            if (record && record.name) {
                                var url = me.router.getRoute('administration/taskmanagement/viewTaskManagement').buildUrl({
                                    type: record.queue,
                                    taskManagementId: record.id
                                });
                                result = '<a href="' + url + '">' + record.name + '</a>';
                            }
                            return result;
                        }
                    }
                ]
            },
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        itemId: 'task-issue-detail-status',
                        fieldLabel: Uni.I18n.translate('general.title.status', 'ITK', 'Status'),
                        name: 'status',
                        afterSubTpl: '<span id="issue-status-field-sub-tpl" class="field-additional-info" style="color: #686868;"></span>',
                        renderer: function (value) {
                            return value.name ? value.name : '';
                        }
                    },
                    {
                        itemId: 'task-issue-detail-due-date',
                        fieldLabel: Uni.I18n.translate('general.title.dueDate', 'ITK', 'Due date'),
                        name: 'dueDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateLong(value) : '';
                        }
                    },
                    {
                        itemId: 'task-issue-detail-priority',
                        fieldLabel: Uni.I18n.translate('general.title.priority', 'ITK', 'Priority'),
                        name: 'priority'
                    },
                    {
                        itemId: 'task-issue-detail-workgroup',
                        fieldLabel: Uni.I18n.translate('general.title.workgroup', 'ITK', 'Workgroup'),
                        name: 'workGroupAssignee',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : Uni.I18n.translate('general.unassigned', 'IDV', 'Unassigned');
                        }
                    },
                    {
                        itemId: 'task-issue-detail-assignee',
                        fieldLabel: Uni.I18n.translate('general.title.user', 'ITK', 'User'),
                        name: 'userAssignee',
                        renderer: function (value) {
                            return value.name ? value.name : Uni.I18n.translate('general.unassigned', 'ITK', 'Unassigned');
                        }
                    },
                    {
                        itemId: 'task-issue-detail-creation-date',
                        fieldLabel: Uni.I18n.translate('general.title.creationDate', 'ITK', 'Creation date'),
                        name: 'creationDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});