/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.view.RegisterPreview', {
    extend: 'Ext.form.Panel',
    alias: 'widget.usage-point-register-preview',
    requires: [
        'Uni.form.field.ReadingTypeDisplay'
    ],
    defaults: {
        xtype: 'displayfield',
        labelWidth: 200
    },

    router: null,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'reading-type-displayfield',
                itemId: 'readingType-field',
                fieldLabel: Uni.I18n.translate('general.readingType', 'MDC', 'Reading type'),
                name: 'readingType'
            },
            {
                itemId: 'dataUntil-field',
                fieldLabel: Uni.I18n.translate('general.dataUntil', 'MDC', 'Data until'),
                name: 'dataUntil',
                renderer: function (value) {
                    return value
                        ? Uni.DateTime.formatDateTimeShort(value)
                        : '-';
                }
            },
            {
                itemId: 'deviceRegisters-field',
                fieldLabel: Uni.I18n.translate('general.deviceRegisters', 'MDC', 'Device registers'),
                name: 'deviceRegisters',
                renderer: function (value) {
                    var result = '',
                        canViewDevices = Mdc.privileges.Device.canView(),
                        infoIcon = '<span style="margin-left: 10px; display:inline-block; color:#A9A9A9; font-size:16px;" class="icon-info" data-qtip="'
                            + Uni.I18n.translate('usagePointRegister.notAvailableRegisters.qtip', 'MDC', 'This register is not available on the device anymore because device configuration is registers')
                            + '"></span>';

                    if (Ext.isArray(value)) {
                        Ext.Array.each(value, function (deviceRegister, index) {
                            var device = canViewDevices
                                    ? '<a href="'
                                + me.router.getRoute('devices/device').buildUrl({
                                    deviceId: deviceRegister.device
                                })
                                + '">'
                                + deviceRegister.device
                                + '</a>'
                                    : deviceRegister.device,
                                register = canViewDevices && deviceRegister.channel
                                    ? '<a href="'
                                // + me.router.getRoute('devices/device/registers/registerdata').buildUrl({
                                //     deviceId: deviceRegister.device,
                                //     registerId: deviceRegister.channel.id
                                // })
                                + '">'
                                + deviceRegister.channel.name
                                + '</a>'
                                    : deviceRegister.channel.name
                                + infoIcon,
                                period = Uni.I18n.translate('general.fromX', 'MDC', 'from {0}', [Uni.DateTime.formatDateTimeShort(new Date(deviceRegister.from))], false);


                            if (index) {
                                result += '<br><br>';
                            }

                            result += device + ' &#62; ' + register;
                            result += '<br><span style="font-size: 13px;font-style: italic;color: #686868;">' + period + '</span>';
                        });
                    }

                    return result || '-';
                }
            }
        ];

        me.callParent(arguments);
    }
});