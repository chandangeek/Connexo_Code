/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Idc.view.ConnectionIssueDetailsForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Mdc.privileges.Device',
        'Mdc.privileges.DeviceType'
    ],
    alias: 'widget.connection-issue-details-form',
    router: null,
    store: null,
    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'connection-issue-details-container',
                xtype: 'data-collection-details-container'
            },
            {
                itemId: 'connection-issue-details-panel-title',
                title: Uni.I18n.translate('general.connectionDetails', 'IDC', 'Connection details'),
                ui: 'medium'
            },
            {
                xtype: 'container',
                itemId: 'connection-issue-other-details-container',
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
                                itemId: 'connection-issue-device',
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
                                itemId: 'connection-issue-usage-point',
                                fieldLabel: Uni.I18n.translate('general.title.usagePoint', 'IDC', 'Usage point'),
                                name: 'usage_point'
                            },
                            {
                                itemId: 'connection-issue-device-type',
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
                                itemId: 'connection-issue-device-configuration',
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
                                itemId: 'connection-issue-device-state',
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
                                itemId: 'connection-issue-method',
                                fieldLabel: Uni.I18n.translate('general.connectionMethod', 'IDC', 'Connection method'),
                                name: 'connectionMethod_name',
                                renderer: function (value) {
                                    return value ? Ext.String.htmlEncode(value) : '';
                                }
                            },
                            {
                                itemId: 'connection-issue-latest-attempt',
                                fieldLabel: Uni.I18n.translate('general.latestAttempt', 'IDC', 'Latest attempt'),
                                name: 'connectionTask_latestAttempt',
                                renderer: function (value) {
                                    return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                                }
                            },
                            {
                                itemId: 'connection-issue-latest-status',
                                fieldLabel: Uni.I18n.translate('general.latestStatus', 'IDC', 'Latest status'),
                                name: 'connectionTask_latestStatus_name',
                                renderer: function (value) {
                                    return value ? Ext.String.htmlEncode(value) : '';
                                }
                            },
                            {
                                itemId: 'connection-issue-latest-result',
                                fieldLabel: Uni.I18n.translate('general.latestResult', 'IDC', 'Latest result'),
                                name: 'connectionTask_latestResult_name',
                                renderer: function (value) {
                                    return value ? Ext.String.htmlEncode(value) : '';
                                }
                            },
                            {
                                itemId: 'connection-issue-last-successful-attempt',
                                fieldLabel: Uni.I18n.translate('general.lastSuccessfulAttempt', 'IDC', 'Last successful attempt'),
                                name: 'connectionTask_lastSuccessfulAttempt',
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
                title: Uni.I18n.translate('general.mostRecentConnectionLog', 'IDC', 'Most recent connection log'),
                itemId: 'connection-issue-log-grid',
                store: me.store
            });
        }

        me.callParent(arguments);
    }
});
