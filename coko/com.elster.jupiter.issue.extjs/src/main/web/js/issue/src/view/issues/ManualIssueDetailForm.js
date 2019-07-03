/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.issues.ManualIssueDetailForm', {
    extend: 'Ext.form.Panel',
    requires: [],
    alias: 'widget.manual-issue-detail-form',
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
                        itemId: 'manual-issue-id',
                        fieldLabel: Uni.I18n.translate('general.title.issueId', 'ISU', 'ID'),
                        name: 'issueId'
                    },
                    {
                        itemId: 'manual-issue-reason',
                        fieldLabel: Uni.I18n.translate('general.title.reason', 'ISU', 'Reason'),
                        name: 'reason',
                        renderer: function (value) {
                            return value && value.name ? value.name : '-'
                        }
                    },
                    {
                        itemId: 'manual-issue-type',
                        fieldLabel: Uni.I18n.translate('general.type', 'ISU', 'Type'),
                        name: 'issueType',
                        renderer: function (value) {
                            return value && value.name ? value.name : '-'
                        }
                    },
                    {
                        itemId: 'manual-issue-detail-device',
                        fieldLabel: Uni.I18n.translate('general.title.device', 'ISU', 'Device'),
                        name: 'device',
                        renderer: function (value) {
                            var url = '',
                                result = '';

                            if (value) {
                                if (value.name && Mdc.privileges.Device.canViewDeviceCommunication()) {
                                    url = me.router.getRoute('devices/device').buildUrl({deviceId: value.name});
                                    result = '<a href="' + url + '">' + value.name + '</a>';
                                } else {
                                    result = value.name;
                                }
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
                        itemId: 'manual-issue-detail-status',
                        fieldLabel: Uni.I18n.translate('general.title.status', 'ISU', 'Status'),
                        name: 'status',
                        afterSubTpl: '<span id="issue-status-field-sub-tpl" class="field-additional-info" style="color: #686868;"></span>',
                        renderer: function (value) {
                            return value.name ? value.name : '';
                        }
                    },
                    {
                        itemId: 'manual-issue-detail-due-date',
                        fieldLabel: Uni.I18n.translate('general.title.dueDate', 'ISU', 'Due date'),
                        name: 'dueDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateLong(value) : '';
                        }
                    },
                    {
                        itemId: 'manual-issue-detail-priority',
                        fieldLabel: Uni.I18n.translate('general.title.priority', 'ISU', 'Priority'),
                        name: 'priority'
                    },
                    {
                        itemId: 'manual-issue-detail-workgroup',
                        fieldLabel: Uni.I18n.translate('general.title.workgroup', 'ISU', 'Workgroup'),
                        name: 'workGroupAssignee',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : Uni.I18n.translate('general.unassigned', 'ISU', 'Unassigned');
                        }
                    },
                    {
                        itemId: 'manual-issue-detail-assignee',
                        fieldLabel: Uni.I18n.translate('general.title.user', 'ISU', 'User'),
                        name: 'userAssignee',
                        renderer: function (value) {
                            return value.name ? value.name : Uni.I18n.translate('general.unassigned', 'ISU', 'Unassigned');
                        }
                    },
                    {
                        itemId: 'manual-issue-detail-creation-date',
                        fieldLabel: Uni.I18n.translate('general.title.creationDate', 'ISU', 'Creation date'),
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