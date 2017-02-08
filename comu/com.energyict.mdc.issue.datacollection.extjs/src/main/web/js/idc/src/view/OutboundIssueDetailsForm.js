/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Idc.view.OutboundIssueDetailsForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Mdc.privileges.Device'
    ],
    alias: 'widget.outbound-issue-details-form',
    router: null,
    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'outbound-issue-details-container',
                xtype: 'data-collection-details-container'
            },
            {
                itemId: 'outbound-issue-details-panel-title',
                title: Uni.I18n.translate('general.connectionDetails', 'IDC', 'Connection details'),
                ui: 'medium'
            },
            {
                xtype: 'container',
                itemId: 'outbound-issue-other-details-container',
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
                                itemId: 'outbound-issue-device',
                                fieldLabel: Uni.I18n.translate('general.masterDevice', 'IDC', 'Master device'),
                                name: 'deviceName',
                                renderer: function (value) {
                                    var url = '',
                                        result = '';

                                    if (value) {
                                        if (Mdc.privileges.Device.canViewDeviceCommunication()) {
                                            url = me.router.getRoute('devices/device').buildUrl({deviceId: value});
                                            result = '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                                        } else {
                                            result = Ext.String.htmlEncode(value);
                                        }
                                    }

                                    return result;
                                }
                            },
                            {
                                itemId: 'outbound-issue-serial-number',
                                fieldLabel: Uni.I18n.translate('general.serialNumberUnknownDevice', 'IDC', 'Serial number of unknown slave device'),
                                name: 'slaveDeviceId',
                                renderer: function (value) {
                                    return value ? Ext.String.htmlEncode(value) : '';
                                }
                            },
                            {
                                itemId: 'outbound-issue-number-of-connections',
                                fieldLabel: Uni.I18n.translate('general.numberOfConnections', 'IDC', 'Number of connections'),
                                name: 'connectionAttemptsNumber'
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
                                itemId: 'outbound-issue-first-connection',
                                fieldLabel: Uni.I18n.translate('general.firstConnection', 'IDC', 'First connection'),
                                name: 'firstConnectionAttempt',
                                renderer: function (value) {
                                    return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                                }
                            },
                            {
                                itemId: 'outbound-issue-last-connection',
                                fieldLabel: Uni.I18n.translate('general.lastConnection', 'IDC', 'Last connection'),
                                name: 'lastConnectionAttempt',
                                renderer: function (value) {
                                    return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                                }
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});

