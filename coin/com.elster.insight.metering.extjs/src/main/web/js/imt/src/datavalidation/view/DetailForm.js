/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.datavalidation.view.DetailForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Isu.privileges.Device'
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
                        fieldLabel: Uni.I18n.translate('general.title.issueId', 'IMT', 'ID'),
                        name: 'issueId'
                    },
                    {
                        itemId: 'data-validation-issue-type',
                        fieldLabel: Uni.I18n.translate('general.title.type', 'IMT', 'Type'),
                        name: 'issueType_name'
                    },
                    {
                        itemId: 'data-validation-issue-detail-usage-point',
                        fieldLabel: Uni.I18n.translate('general.title.usagePoint', 'IMT', 'Usage point'),
                        name: 'usagePointInfo',
                        renderer: function (value) {
                            var url = '',
                                result = '-';

                            if (value.info) {
                                if (value.info && Imt.privileges.UsagePoint.canView()) {
                                    url = me.router.getRoute('usagepoints/view').buildUrl({usagePointId: value.info});
                                    result = '<a href="' + url + '">' + Ext.String.htmlEncode(value.info) + '</a>';
                                } else {
                                    result = value.info;
                                }
                            }

                            return result;
                        }
                    },
                    /*{
                     itemId: 'data-validation-issue-detail-device',
                     fieldLabel: Uni.I18n.translate('general.title.device', 'IMT', 'Device'),
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
                     }*/
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
                        fieldLabel: Uni.I18n.translate('general.title.status', 'IMT', 'Status'),
                        name: 'status',
                        afterSubTpl: '<span id="issue-status-field-sub-tpl" class="field-additional-info" style="color: #686868;"></span>',
                        renderer: function (value) {
                            return value.name ? value.name : '';
                        }
                    },
                    {
                        itemId: 'data-validation-issue-detail-due-date',
                        fieldLabel: Uni.I18n.translate('general.title.dueDate', 'IMT', 'Due date'),
                        name: 'dueDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateLong(value) : '';
                        }
                    },
                    {
                        itemId: 'data-validation-issue-detail-priority',
                        fieldLabel: Uni.I18n.translate('general.title.priority', 'IMT', 'Priority'),
                        name: 'priority'
                    },
                    {
                        itemId: 'data-validation-issue-detail-workgroup',
                        fieldLabel: Uni.I18n.translate('general.title.workgroup', 'IMT', 'Workgroup'),
                        name: 'workGroupAssignee',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : Uni.I18n.translate('general.unassigned', 'IMT', 'Unassigned');
                        }
                    },
                    {
                        itemId: 'data-validation-issue-detail-assignee',
                        fieldLabel: Uni.I18n.translate('general.title.user', 'IMT', 'User'),
                        name: 'userAssignee',
                        renderer: function (value) {
                            return value.name ? value.name : Uni.I18n.translate('general.unassigned', 'IMT', 'Unassigned');
                        }
                    },
                    {
                        itemId: 'data-validation-issue-detail-creation-date',
                        fieldLabel: Uni.I18n.translate('general.title.creationDate', 'IMT', 'Creation date'),
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