/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.issue.view.Preview', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.form.field.FilterDisplay',
        'Isu.view.issues.ActionMenu',
        'Isu.privileges.Issue',
        'Isu.privileges.Device'
    ],
    alias: 'widget.issue-view-preview',
    layout: 'column',
    defaults: {
        xtype: 'container',
        layout: 'form',
        columnWidth: 0.5
    },
    frame: true,
    router: null,

    initComponent: function () {
        var me = this;

        me.tools = [
            {
                xtype: 'uni-button-action',
                itemId: 'issues-preview-actions-button',
                privileges: Ext.Array.merge(Isu.privileges.Issue.adminDevice, Isu.privileges.Device.viewDeviceCommunication),
                menu: {
                    xtype: 'issues-action-menu',
                    itemId: 'issues-overview-action-menu',
                    router: me.router
                },
                listeners: {
                    click: function () {
                        this.showMenu();
                    }
                }
            }
        ];

        me.items = [
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        itemId: 'issue-id',
                        fieldLabel: Uni.I18n.translate('general.title.issueId', 'IMT', 'ID'),
                        name: 'issueId'
                    },
                    {
                        xtype: 'filter-display',
                        itemId: 'issue-preview-reason',
                        fieldLabel: Uni.I18n.translate('general.title.reason', 'IMT', 'Reason'),
                        name: 'reason',
                        renderer: function (value) {
                            if (value && me.getRecord()) {
                                me.down('#issue-preview-reason').setVisible(me.getRecord().get('issueType').uid != 'datavalidation');
                                return Ext.String.htmlEncode(value.name);
                            }
                            return '-';
                        }
                    },
                    {
                        xtype: 'filter-display',
                        itemId: 'issue-type',
                        fieldLabel: Uni.I18n.translate('general.type', 'IMT', 'Type'),
                        name: 'issueType',
                        renderer: function (value) {
                            return value ? value.name : '-';
                        }
                    },
                    {
                        itemId: 'issue-preview-usage-point',
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
                    {
                        itemId: 'issue-preview-device-no-filter',
                        fieldLabel: Uni.I18n.translate('general.title.device', 'IMT', 'Device'),
                        name: 'deviceName',
                        hidden: true,
                        renderer: function (value, field) {
                            if (value && me.getRecord()) {
                                me.down('#issue-preview-device').setVisible(me.getRecord().get('reason').id != 'reason.unknown.inbound.device');
                                field.setVisible(me.getRecord().get('reason').id == 'reason.unknown.inbound.device');
                                return value ? Ext.String.htmlEncode(value) : '-';
                            }
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
                        xtype: 'filter-display',
                        itemId: 'issue-preview-status',
                        fieldLabel: Uni.I18n.translate('general.title.status', 'IMT', 'Status'),
                        name: 'status',
                        afterSubTpl: '<span id="issue-status-field-sub-tpl" class="field-additional-info" style="color: #686868;"></span>',
                        renderer: function (value) {
                            return value.name ? value.name : '-';
                        }
                    },
                    {
                        itemId: 'issue-preview-due-date',
                        fieldLabel: Uni.I18n.translate('general.title.dueDate', 'IMT', 'Due date'),
                        name: 'dueDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateShort(value) : '-';
                        }
                    },
                    {
                        itemId: 'issue-preview-priority',
                        fieldLabel: Uni.I18n.translate('general.title.priority', 'IMT', 'Priority'),
                        name: 'priority'
                    },
                    {
                        xtype: 'filter-display',
                        itemId: 'issue-preview-workgroup',
                        fieldLabel: Uni.I18n.translate('general.workgroup', 'IMT', 'Workgroup'),
                        name: 'workGroupAssignee',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : Uni.I18n.translate('general.unassigned', 'IMT', 'Unassigned');
                        }
                    },
                    {
                        xtype: 'filter-display',
                        itemId: 'issue-preview-assignee',
                        fieldLabel: Uni.I18n.translate('general.title.USER', 'IMT', 'User'),
                        name: 'userAssignee',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : Uni.I18n.translate('general.unassigned', 'IMT', 'Unassigned');
                        }
                    },
                    {
                        itemId: 'issue-preview-creation-date',
                        fieldLabel: Uni.I18n.translate('general.title.creationDate', 'IMT', 'Creation date'),
                        name: 'creationDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTimeLong(value) : '-';
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});