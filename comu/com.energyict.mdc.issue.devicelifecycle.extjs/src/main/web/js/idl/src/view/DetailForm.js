/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Idl.view.DetailForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Mdc.privileges.Device'
    ],
    alias: 'widget.data-validation-issue-detail-form',
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
                        itemId: 'data-validation-issue-id',
                        fieldLabel: Uni.I18n.translate('general.title.issueId', 'IDL', 'ID'),
                        name: 'issueId'
                    },
                    {
                        itemId: 'data-validation-issue-detail-usage-point',
                        fieldLabel: Uni.I18n.translate('general.title.usagePoint', 'IDL', 'Usage point'),
                        name: 'usage_point'
                    },
                    {
                        itemId: 'data-validation-issue-detail-device',
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
                        itemId: 'data-validation-issue-detail-status',
                        fieldLabel: Uni.I18n.translate('general.title.status', 'IDL', 'Status'),
                        name: 'status',
                        afterSubTpl: '<span id="issue-status-field-sub-tpl" class="field-additional-info" style="color: #686868;"></span>',
                        renderer: function (value) {
                            return value.name ? value.name : '';
                        }
                    },
                    {
                        itemId: 'data-validation-issue-detail-due-date',
                        fieldLabel: Uni.I18n.translate('general.title.dueDate', 'IDL', 'Due date'),
                        name: 'dueDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateLong(value) : '';
                        }
                    },
                    {
                        itemId: 'data-validation-issue-detail-priority',
                        fieldLabel: Uni.I18n.translate('general.title.priority', 'IDL', 'Priority'),
                        name: 'priority'
                    },
                    {
                        itemId: 'data-validation-issue-detail-workgroup',
                        fieldLabel: Uni.I18n.translate('general.title.workgroup', 'IDL', 'Workgroup'),
                        name: 'workGroupAssignee',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : Uni.I18n.translate('general.unassigned', 'IDV', 'Unassigned');
                        }
                    },
                    {
                        itemId: 'data-validation-issue-detail-assignee',
                        fieldLabel: Uni.I18n.translate('general.title.user', 'IDL', 'User'),
                        name: 'userAssignee',
                        renderer: function (value) {
                            return value.name ? value.name : Uni.I18n.translate('general.unassigned', 'IDL', 'Unassigned');
                        }
                    },
                    {
                        itemId: 'data-validation-issue-detail-creation-date',
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