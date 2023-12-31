/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.issues.Preview', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.form.field.FilterDisplay',
        'Isu.view.issues.ActionMenu',
        'Isu.privileges.Issue',
        'Isu.privileges.Device'
    ],
    alias: 'widget.issues-preview',
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
                        fieldLabel: Uni.I18n.translate('general.title.issueId', 'ISU', 'ID'),
                        name: 'issueId'
                    },
                    {
                        xtype: 'filter-display',
                        itemId: 'issue-preview-reason',
                        fieldLabel: Uni.I18n.translate('general.title.reason', 'ISU', 'Reason'),
                        name: 'reason',
                        renderer: function (value) {
                            return value && value.name ? Ext.String.htmlEncode(value.name) : '-';
                        }
                    },
                    {
                        xtype: 'filter-display',
                        itemId: 'issue-type',
                        fieldLabel: Uni.I18n.translate('general.type', 'ISU', 'Type'),
                        name: 'issueType',
                        renderer: function (value) {
                            return value ? value.name : '-';
                        }
                    },
                    {
                        xtype: 'filter-display',
                        itemId: 'issue-preview-servicecall',
                        fieldLabel: Uni.I18n.translate('general.title.servicecall', 'ISU', 'Service call'),
                        name: 'serviceCall',
                        renderer: function (value) {
                            var url = '',
                                result = '-';

                            if (me.getRecord()) {
                                me.down('#issue-preview-servicecall').setVisible(me.getRecord().get('issueType').uid === 'servicecall');
                            }

                            if (value) {
                                if (value && Scs.privileges.ServiceCall.canView()) {
                                    url = me.router.getRoute('workspace/servicecalls/overview').buildUrl({serviceCallId: value.id});
                                    result = '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + '</a>';
                                } else {
                                    result = value.name;
                                }
                            }

                            return result;
                        }
                    },
                    {
                        xtype: 'filter-display',
                        itemId: 'issue-preview-webservice-endpoint',
                        fieldLabel: Uni.I18n.translate('general.title.webserviceEndpoint', 'ISU', 'Web service endpoint'),
                        name: 'webServiceEndpoint',
                        renderer: function (value) {
                            var url = '',
                                result = '-';

                            if (me.getRecord()) {
                                me.down('#issue-preview-webservice-endpoint').setVisible(me.getRecord().get('issueType').uid === 'webservice');
                            }

                            if (value) {
                                if (value && Wss.privileges.Webservices.canView()) {
                                    url = me.router.getRoute('workspace/webserviceendpoints/view').buildUrl({endpointId: value.id});
                                    result = '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + '</a>';
                                } else {
                                    result = value.name;
                                }
                            }

                            return result;
                        }
                    },
                    {
                        itemId: 'issue-preview-usage-point',
                        fieldLabel: Uni.I18n.translate('general.title.usagePoint', 'ISU', 'Usage point'),
                        name: 'usage_point',
                        renderer: function (value) {

                            if (me.getRecord()) {
                                if (me.getRecord().get('issueType').uid === 'webservice') {
                                    me.down('#issue-preview-usage-point').setVisible(false);
                                } else if (me.getRecord().get('issueType').uid === 'servicecall') {
                                    me.down('#issue-preview-usage-point').setVisible(false);
                                } else {
                                    me.down('#issue-preview-usage-point').setVisible(true);
                                }
                            }
                            return value && !Ext.isEmpty(value) ? value : '-';
                        }
                    },
                    {
                        xtype: 'filter-display',
                        itemId: 'issue-preview-device',
                        fieldLabel: Uni.I18n.translate('general.title.device', 'ISU', 'Device'),
                        name: 'device',
                        renderer: function (value) {
                            var url = '',
                                result = '-';

                                if (me.getRecord()) {
                                    if (me.getRecord().get('issueType').uid === 'webservice') {
                                        me.down('#issue-preview-device').setVisible(false);
                                    } else if (me.getRecord().get('issueType').uid === 'servicecall') {
                                        me.down('#issue-preview-device').setVisible(false);
                                    } else {
                                        me.down('#issue-preview-device').setVisible(true);
                                    }
                                }

                            if (value) {
                                if (value.name && Isu.privileges.Device.canViewDeviceCommunication()) {
                                    url = me.router.getRoute('devices/device').buildUrl({deviceId: value.name});
                                    result = '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + '</a>';
                                } else {
                                    result = value.name;
                                }
                            }

                            return result;
                        }
                    },
                    {
                        itemId: 'issue-preview-device-no-filter',
                        fieldLabel: Uni.I18n.translate('general.title.device', 'ISU', 'Device'),
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
                        fieldLabel: Uni.I18n.translate('general.title.status', 'ISU', 'Status'),
                        name: 'status',
                        afterSubTpl: '<span id="issue-status-field-sub-tpl" class="field-additional-info" style="color: #686868;"></span>',
                        renderer: function (value) {
                            return value.name ? value.name : '-';
                        }
                    },
                    {
                        itemId: 'issue-preview-due-date',
                        fieldLabel: Uni.I18n.translate('general.title.dueDate', 'ISU', 'Due date'),
                        name: 'dueDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateShort(value) : '-';
                        }
                    },
                    {
                        itemId: 'issue-preview-priority',
                        fieldLabel: Uni.I18n.translate('general.title.priority', 'ISU', 'Priority'),
                        name: 'priority'
                    },
                    {
                        xtype: 'filter-display',
                        itemId: 'issue-preview-workgroup',
                        fieldLabel: Uni.I18n.translate('general.workgroup', 'ISU', 'Workgroup'),
                        name: 'workGroupAssignee',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : Uni.I18n.translate('general.unassigned', 'ISU', 'Unassigned');
                        }
                    },
                    {
                        xtype: 'filter-display',
                        itemId: 'issue-preview-assignee',
                        fieldLabel: Uni.I18n.translate('general.title.USER', 'ISU', 'User'),
                        name: 'userAssignee',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : Uni.I18n.translate('general.unassigned', 'ISU', 'Unassigned');
                        }
                    },
                    {
                        itemId: 'issue-preview-creation-date',
                        fieldLabel: Uni.I18n.translate('general.title.creationDate', 'ISU', 'Creation date'),
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
