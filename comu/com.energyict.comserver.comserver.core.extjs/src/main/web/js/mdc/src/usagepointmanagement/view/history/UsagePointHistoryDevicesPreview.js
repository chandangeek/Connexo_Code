/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.view.history.UsagePointHistoryDevicesPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.usage-point-history-devices-preview',
    router: null,
    title: ' ',
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'form',
                itemId: 'usage-point-history-devices-preview-form',
                items: [
                    {
                        xtype: 'displayfield',
                        labelWidth: 200,
                        itemId: 'period-field',
                        name: 'start',
                        fieldLabel: Uni.I18n.translate('general.period', 'MDC', 'Period'),
                        renderer: function (value) {
                            if (value) {
                                var from = me.down('form').getRecord().get('start'),
                                    to = me.down('form').getRecord().get('end');

                                return to ? Uni.I18n.translate('general.period.fromUntil', 'MDC', 'From {0} until {1}', [
                                    Uni.DateTime.formatDateTimeShort(from),
                                    Uni.DateTime.formatDateTimeShort(to)
                                ])
                                    : Uni.I18n.translate('general.period.from', 'MDC', 'From {0}', [
                                    Uni.DateTime.formatDateTimeShort(from)
                                ]);
                            }
                        }
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'device-field-container',
                        fieldLabel: Uni.I18n.translate('general.device', 'MDC', 'Device'),
                        labelAlign: 'top',
                        layout: 'vbox',
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 200
                        },
                        items: [
                            {
                                itemId: 'name-field',
                                name: 'name',
                                fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                                renderer: function (value) {
                                    if (value) {
                                        var deviceUrl = me.router.getRoute('devices/device').buildUrl({
                                                deviceId: value
                                            }),
                                            meterActivationHistoryUrl = me.router.getRoute('devices/device/history').buildUrl({
                                                deviceId: value
                                            }, {activeTab: 'meterActivations'});
                                        if (Mdc.privileges.Device.canView()) {
                                            return '<a href="' + deviceUrl + '">' + value + '</a>&nbsp;&nbsp;&nbsp;&nbsp;(<a href="' + meterActivationHistoryUrl + '">' +
                                                Uni.I18n.translate('general.viewMeterActivationHistory', 'MDC', 'View meter activation history') + '</a>)';
                                        } else {
                                            return value;
                                        }
                                    }
                                }
                            },
                            {
                                itemId: 'state-field',
                                name: 'state',
                                fieldLabel: Uni.I18n.translate('general.state', 'MDC', 'State')
                            },
                            {
                                itemId: 'serial-number-field',
                                name: 'serialNumber',
                                fieldLabel: Uni.I18n.translate('deviceAdd.serialNumber', 'MDC', 'Serial number')
                            },
                            {
                                itemId: 'device-type-field',
                                name: 'deviceType',
                                fieldLabel: Uni.I18n.translate('general.deviceType', 'MDC', 'Device type'),
                                renderer: function (value) {
                                    if (value) {
                                        var url = me.router.getRoute('administration/devicetypes/view').buildUrl({
                                            deviceTypeId: value.id
                                        });
                                        return Mdc.privileges.DeviceType.canView() ? '<a href="' + url + '">' + value.name + '</a>' : value.name;
                                    }
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
