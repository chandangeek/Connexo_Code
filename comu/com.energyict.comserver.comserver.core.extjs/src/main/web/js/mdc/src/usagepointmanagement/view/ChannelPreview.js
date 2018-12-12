/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.view.ChannelPreview', {
    extend: 'Ext.form.Panel',
    alias: 'widget.usage-point-channel-preview',
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
                itemId: 'interval-field',
                fieldLabel: Uni.I18n.translate('general.interval', 'MDC', 'Interval'),
                name: 'interval',
                renderer: function (value) {
                    return Ext.isObject(value)
                        ? Uni.util.Common.translateTimeUnit(value.count, value.timeUnit)
                        : '-';
                }
            },
            {
                itemId: 'dataUntil-field',
                fieldLabel: Uni.I18n.translate('general.dataUntil', 'MDC', 'Data until'),
                name: 'dataUntil',
                renderer: function (value) {
                    return value
                        ? Uni.DateTime.formatDateTimeShort(new Date(value))
                        : '-';
                }
            },
            {
                itemId: 'deviceChannels-field',
                fieldLabel: Uni.I18n.translate('general.deviceChannels', 'MDC', 'Device channels'),
                name: 'deviceChannels',
                renderer: function (value) {
                    var result = '',
                        canViewDevices = Mdc.privileges.Device.canView(),
                        infoIcon = '<span style="margin-left: 10px; display:inline-block; color:#A9A9A9; font-size:16px;" class="icon-info" data-qtip="'
                            + Uni.I18n.translate('usagePointChannel.notAvailableChannel.qtip', 'MDC', 'This channel is not available on the device anymore because device configuration is changed')
                            + '"></span>';

                    if (Ext.isArray(value)) {
                        Ext.Array.each(value, function (deviceChannel, index) {
                            var device = canViewDevices
                                    ? '<a href="'
                                + me.router.getRoute('devices/device').buildUrl({
                                    deviceId: deviceChannel.device
                                })
                                + '">'
                                + deviceChannel.device
                                + '</a>'
                                    : deviceChannel.device,
                                channel = canViewDevices && deviceChannel.channel.id
                                    ? '<a href="'
                                + me.router.getRoute('devices/device/channels/channeldata').buildUrl({
                                    deviceId: deviceChannel.device,
                                    channelId: deviceChannel.channel.id
                                })
                                + '">'
                                + deviceChannel.channel.name
                                + '</a>'
                                    : deviceChannel.channel.name
                                + infoIcon,
                                period = !deviceChannel.until
                                    ? Uni.I18n.translate('general.fromX', 'MDC', 'from {0}', [Uni.DateTime.formatDateTimeShort(new Date(deviceChannel.from))], false)
                                    : Uni.I18n.translate('general.fromXuntilX', 'MDC', 'from {0} until {1}', [
                                    Uni.DateTime.formatDateTimeShort(new Date(deviceChannel.from)),
                                    Uni.DateTime.formatDateTimeShort(new Date(deviceChannel.until))
                                ], false);

                            if (index) {
                                result += '<br><br>';
                            }

                            result += device + ' &#62; ' + channel;
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