/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Idc.view.CommunicationIssueDetailsForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Mdc.privileges.Device',
        'Mdc.privileges.DeviceType'
    ],
    alias: 'widget.communication-issue-details-form',
    router: null,
    store: null,
    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'communication-issue-details-container',
                xtype: 'data-collection-details-container'
            },
            {
                itemId: 'communication-issue-details-panel-title',
                title: Uni.I18n.translate('general.communicationDetails', 'IDC', 'Communication details'),
                ui: 'medium'
            },
            {
                xtype: 'container',
                itemId: 'communication-issue-other-details-container',
                layout: 'column',
                items: [
                    {
                        xtype: 'container',
                        layout: 'form',
                        columnWidth: 0.5,
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 200
                        },
                        items: [
                            {
                                itemId: 'communication-issue-device',
                                fieldLabel: Uni.I18n.translate('general.title.device', 'IDC', 'Device'),
                                name: 'device',
                                renderer: function (value) {
                                    var url = '',
                                        result = '';

                                    if (value) {
                                        if (value.name && Mdc.privileges.Device.canViewDeviceCommunication()) {
                                            url = me.router.getRoute('devices/device').buildUrl({deviceId: value.name});
                                            result = '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + '</a>';
                                        } else {
                                            result = Ext.String.htmlEncode(value.name);
                                        }
                                    }

                                    return result;
                                }
                            },
                            {
                                itemId: 'communication-issue-usage-point',
                                fieldLabel: Uni.I18n.translate('general.title.usagePoint', 'IDC', 'Usage point'),
                                name: 'usage_point'
                            },
                            {
                                itemId: 'communication-issue-device-type',
                                fieldLabel: Uni.I18n.translate('general.title.deviceType', 'IDC', 'Device type'),
                                name: 'deviceType',
                                renderer: function (value) {
                                    var url = '',
                                        result = '';

                                    if (value) {
                                        if (Mdc.privileges.DeviceType.canView()) {
                                            url = me.router.getRoute('administration/devicetypes/view').buildUrl({deviceTypeId: value.id});
                                            result = '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + '</a>';
                                        } else {
                                            result = Ext.String.htmlEncode(value.name);
                                        }
                                    }

                                    return result;
                                }
                            },
                            {
                                itemId: 'communication-issue-device-configuration',
                                fieldLabel: Uni.I18n.translate('general.title.deviceConfiguration', 'IDC', 'Device configuration'),
                                name: 'deviceConfiguration',
                                renderer: function (value) {
                                    var url = '',
                                        result = '';

                                    if (value) {
                                        if (me.getRecord() && Mdc.privileges.DeviceType.canView()) {
                                            url = me.router.getRoute('administration/devicetypes/view/deviceconfigurations/view').buildUrl({deviceTypeId: me.getRecord().get('deviceType').id, deviceConfigurationId: value.id});
                                            result = '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + '</a>';
                                        } else {
                                            result = Ext.String.htmlEncode(value.name);
                                        }
                                    }

                                    return result;
                                }
                            },
                            {
                                itemId: 'communication-issue-device-state',
                                fieldLabel: Uni.I18n.translate('general.title.deviceState', 'IDC', 'Device state'),
                                name: 'deviceState_name'
                            }
                        ]
                    },
                    {
                        xtype: 'container',
                        layout: 'form',
                        columnWidth: 0.5,
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 200
                        },
                        items: [
                            {
                                itemId: 'communication-issue-comtask',
                                fieldLabel: Uni.I18n.translate('general.communicationTask', 'IDC', 'Communication task'),
                                name: 'communicationTask_name',
                                renderer: function (value) {
                                    return value ? Ext.String.htmlEncode(value) : '';
                                }
                            },
                            {
                                itemId: 'communication-issue-latest-connection-used',
                                fieldLabel: Uni.I18n.translate('general.latestConnectionUsed', 'IDC', 'Latest connection used'),
                                name: 'latestConnectionUsed_name',
                                renderer: function (value) {
                                    return value ? Ext.String.htmlEncode(value) : '';
                                }
                            },
                            {
                                itemId: 'communication-issue-latest-attempt',
                                fieldLabel: Uni.I18n.translate('general.latestAttempt', 'IDC', 'Latest attempt'),
                                name: 'communicationTask_latestAttempt',
                                renderer: function (value) {
                                    return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                                }
                            },
                            {
                                itemId: 'communication-issue-latest-status',
                                fieldLabel: Uni.I18n.translate('general.latestStatus', 'IDC', 'Latest status'),
                                name: 'communicationTask_latestStatus_name',
                                renderer: function (value) {
                                    return value ? Ext.String.htmlEncode(value) : '';
                                }
                            },
                            {
                                itemId: 'communication-issue-latest-result',
                                fieldLabel: Uni.I18n.translate('general.latestResult', 'IDC', 'Latest result'),
                                name: 'communicationTask_latestResult_name',
                                renderer: function (value) {
                                    return value ? Ext.String.htmlEncode(value) : '';
                                }
                            },
                            {
                                itemId: 'communication-issue-last-successful-attempt',
                                fieldLabel: Uni.I18n.translate('general.lastSuccessfulAttempt', 'IDC', 'Last successful attempt'),
                                name: 'communicationTask_lastSuccessfulAttempt',
                                renderer: function (value) {
                                    return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                                }
                            }
                        ]
                    }
                ]
            }
        ];
        if (me.store) {
            me.items.push({
                xtype: 'issue-details-log-grid',
                title: Uni.I18n.translate('general.mostRecentCommunicationLog', 'IDC', 'Most recent communication log'),
                itemId: 'communication-issue-log-grid',
                store: me.store
            });
        }

        me.callParent(arguments);
    }
});