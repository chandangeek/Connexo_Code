/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isc.view.DetailForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.servicecall-isssue-detail-form',
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
                        itemId: 'servicecall-issue-id',
                        fieldLabel: Uni.I18n.translate('general.title.issueId', 'ISC', 'ID'),
                        name: 'issueId'
                    },
                    {
                        itemId: 'servicecall-issue-reason',
                        fieldLabel: Uni.I18n.translate('general.title.reason', 'ISC', 'Reason'),
                        name: 'reason',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : '';
                        }
                    },
                    {
                        itemId: 'servicecall-issue-type',
                        fieldLabel: Uni.I18n.translate('general.type', 'ISC', 'Type'),
                        name: 'issueType',
                        renderer: function (value) {
                            return value ? value.name : '';
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
                        itemId: 'servicecall-issue-status',
                        fieldLabel: Uni.I18n.translate('general.title.status', 'ISC', 'Status'),
                        name: 'status',
                        afterSubTpl: '<span id="issue-status-field-sub-tpl" class="field-additional-info" style="color: #686868;"></span>',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : '';
                        }
                    },
                    {
                        itemId: 'servicecall-issue-due-date',
                        fieldLabel: Uni.I18n.translate('general.title.dueDate', 'ISC', 'Due date'),
                        name: 'dueDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateLong(value) : '';
                        }
                    },
                    {
                        itemId: 'servicecall-issue-priority',
                        fieldLabel: Uni.I18n.translate('general.title.priority', 'ISC', 'Priority'),
                        name: 'priority'
                    },
                    {
                        itemId: 'servicecall-issue-workgroup',
                        fieldLabel: Uni.I18n.translate('general.title.workgroup', 'ISC', 'Workgroup'),
                        name: 'workGroupAssignee',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : Uni.I18n.translate('general.unassigned', 'IDC', 'Unassigned');
                        }
                    },
                    {
                        itemId: 'servicecall-issue-user',
                        fieldLabel: Uni.I18n.translate('general.title.user', 'ISC', 'User'),
                        name: 'userAssignee',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : Uni.I18n.translate('general.unassigned', 'ISC', 'Unassigned');
                        }
                    },
                    {
                        itemId: 'servicecall-issue-creation-date',
                        fieldLabel: Uni.I18n.translate('general.title.creationDate', 'ISC', 'Creation date'),
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
