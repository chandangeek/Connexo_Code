/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Idl.view.DetailForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Mdc.privileges.Device'
    ],
    alias: 'widget.device-lifecycle-issue-detail-form',
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
                        itemId: 'device-lifecycle-issue-id',
                        fieldLabel: Uni.I18n.translate('general.title.issueId', 'IDL', 'ID'),
                        name: 'issueId'
                    },
                    {
                        itemId: 'device-lifecycle-issue-reason',
                        fieldLabel: Uni.I18n.translate('general.title.reson', 'IDL', 'Reason'),
                        name: 'reason_name'
                    },
                    {
                        itemId: 'data-validation-issue-detail-usage-point',
                        fieldLabel: Uni.I18n.translate('general.title.usagePoint', 'IDL', 'Usage point'),
                        name: 'usage_point'
                    },
                    {
                        itemId: 'device-lifecycle-issue-detail-device',
                        fieldLabel: Uni.I18n.translate('general.title.device', 'IDL', 'Device'),
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
                    },
                    {
                        itemId: 'device-lifecycle-issue-detail-location',
                        fieldLabel: Uni.I18n.translate('general.title.location', 'IDL', 'Location'),
                        name: 'device_location'
                    },
                ]
            },
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        itemId: 'device-lifecycle-issue-detail-status',
                        fieldLabel: Uni.I18n.translate('general.title.status', 'IDL', 'Status'),
                        name: 'status',
                        afterSubTpl: '<span id="issue-status-field-sub-tpl" class="field-additional-info" style="color: #686868;"></span>',
                        renderer: function (value) {
                            return value.name ? value.name : '';
                        }
                    },
                    {
                        itemId: 'device-lifecycle-issue-detail-due-date',
                        fieldLabel: Uni.I18n.translate('general.title.dueDate', 'IDL', 'Due date'),
                        name: 'dueDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateLong(value) : '';
                        }
                    },
                    {
                        itemId: 'device-lifecycle-issue-detail-priority',
                        fieldLabel: Uni.I18n.translate('general.title.priority', 'IDL', 'Priority'),
                        name: 'priority'
                    },
                    {
                        itemId: 'device-lifecyclen-issue-detail-workgroup',
                        fieldLabel: Uni.I18n.translate('general.title.workgroup', 'IDL', 'Workgroup'),
                        name: 'workGroupAssignee',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : Uni.I18n.translate('general.unassigned', 'IDV', 'Unassigned');
                        }
                    },
                    {
                        itemId: 'device-lifecycle-issue-detail-assignee',
                        fieldLabel: Uni.I18n.translate('general.title.user', 'IDL', 'User'),
                        name: 'userAssignee',
                        renderer: function (value) {
                            return value.name ? value.name : Uni.I18n.translate('general.unassigned', 'IDL', 'Unassigned');
                        }
                    },
                    {
                        itemId: 'device-lifecycle-issue-detail-creation-date',
                        fieldLabel: Uni.I18n.translate('general.title.creationDate', 'IDL', 'Creation date'),
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